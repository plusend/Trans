//RecordAdapter

package com.plusend.components.records;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lenovo.channel.base.ShareCollection;
import com.lenovo.channel.base.ShareRecord;
import com.lenovo.channel.base.ShareRecord.RecordType;
import com.lenovo.channel.base.ShareRecord.ShareType;
import com.lenovo.channel.base.ShareRecord.Status;
import com.lenovo.channel.exception.TransmitException;
import com.lenovo.content.base.ContentItem;
import com.lenovo.content.base.ContentType;
import com.lenovo.sharesdk.ShareWrapper;
import com.plusend.components.records.ShareItem.EventElement;
import com.plusend.utils.FileUtils;
import com.plusend.utils.OpenFile;
import com.plusend.utils.ThumbnailUtils;
import com.plusend.trans.R;

public class RecordView extends LinearLayout implements Observer {
	private static final String TAG = "ItemView";
	private static final int PROGRESS_BAR_MAX = 100;
	private static final int MSG_UPDATE_UI = 0x11;
	private static final int MSG_FIT_THUMBNAIL = 0x12;

	private Context mContext;

	private View mRootView;
	private ImageView mThumbnail;
	private TextView mTitle;
	private TextView mType;
	private TextView mFailedInfo;
	private TextView mProgressInfo;
	private ProgressBar mProgress;
	private Button mButtonCancel;
	public Button mButtonOpen;

	private ShareItem mItem;

	public RecordView(Context context) {
		super(context);
		inflateLayout(context);
	}

	private void inflateLayout(Context context) {
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRootView = inflater.inflate(R.layout.record, this, true);
		mThumbnail = (ImageView) mRootView.findViewById(R.id.thumbnail);
		mTitle = (TextView) mRootView.findViewById(R.id.title);
		mType = (TextView) mRootView.findViewById(R.id.type);
		mFailedInfo = (TextView) mRootView.findViewById(R.id.failed);
		mProgressInfo = (TextView) mRootView.findViewById(R.id.progress_info);
		mProgress = (ProgressBar) mRootView.findViewById(R.id.progress);
		mButtonCancel = (Button) mRootView.findViewById(R.id.button_operation);
		mButtonCancel.setOnClickListener(mBtnCancelClickListener);
		mButtonOpen = (Button) mRootView.findViewById(R.id.button_open);
		mButtonOpen.setOnClickListener(mBtnOpenClickListener);
		mButtonOpen.setVisibility(View.GONE);
	}

	// 中断传输
	private OnClickListener mBtnCancelClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ShareWrapper.getInstance().cancelShareRecord(mItem.getRecord().getDeviceId(), mItem.getRecord().getShareId());
			mButtonCancel.setText("Failed");
			mButtonCancel.setEnabled(false);
		}
	};

	private OnClickListener mBtnOpenClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mContext.startActivity(OpenFile.openFile(mItem.getRecord().getItem().getFilePath()));
		}
	};

	public void update(Observable item, Object element) {
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_UI, element));
	}

	private void updateUI(Object element) {
		EventElement ie = (EventElement) element;
		ShareItem ii = mItem;
		switch (ie) {
		case PROGRESS:
			updateProgress(ii);
			break;
		case COMPLETE:
			onCompelete(ii);
			if (mItem.getRecord().getType() == ShareType.SEND) {
				mButtonOpen.setText("OK");
				mButtonOpen.setEnabled(false);
			}
			break;
		case ERROR:
			onError(ii);
			break;
		case THUMBNAIL:
			onThumbnailComplete(ii);
			break;
		default:
			break;
		}
	}

	private void updateProgress(ShareItem item) {
		ShareRecord sr = item.getRecord();
		int progress = (int) ((double) item.mCompletedLength / (double) sr.getSize() * PROGRESS_BAR_MAX);
		showProgressBar(progress);
		mProgressInfo.setText(progress + "%");
	}

	@SuppressWarnings("unchecked")
	private void onCompelete(ShareItem item) {
		showProgressBar(PROGRESS_BAR_MAX);
		mProgressInfo.setText("100%");

		ArrayList<HistoryRecord> list = new ArrayList<HistoryRecord>();
		HistoryRecord hr = new HistoryRecord();

		try {
			FileInputStream fis = mContext.openFileInput("history.txt");
			ObjectInputStream ois = new ObjectInputStream(fis);
			list = (ArrayList<HistoryRecord>) ois.readObject();

		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			FileOutputStream outStream = mContext.openFileOutput("history.txt", Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(outStream);

			if (item.getRecord().getType() == ShareType.RECEIVE) {
				hr.setMode("receive");
			} else {
				hr.setMode("send");
			}

			hr.setUri(item.getRecord().getItem().getFilePath());
			hr.setType(item.getRecord().getItem().getContentType().toString());
			hr.setName(item.getRecord().getItem().getName());
			hr.setThumb(item.getRecord().getItem().getThumbnailPath());
			hr.setId(item.getRecord().getItem().getId());
			list.add(hr);
			oos.writeObject(list);
			outStream.close();
		} catch (FileNotFoundException e) {
			return;
		} catch (IOException e) {
			return;
		}

		mButtonCancel.setVisibility(View.GONE);
		mButtonOpen.setVisibility(View.VISIBLE);
	}

	private void onThumbnailComplete(ShareItem item) {
		fillThumbnail();
	}

	private void onError(ShareItem item) {

		mButtonCancel.setText("Failed");
		mButtonCancel.setEnabled(false);
		mFailedInfo.setText(mContext.getString(fetchErrorResourceId(item.getError())));
		mFailedInfo.setTextColor(Color.RED);
	}

	public void bindItem(ShareItem item) {
		ShareRecord sc = item.getRecord();
		if (sc.getRecordType() == RecordType.ITEM)
			bindItemRecord(item);
		else
			bindCollectionRecord(item);
	}

	private void bindItemRecord(ShareItem item) {
		try {
			mItem = item;
			ShareRecord sr = item.getRecord();
			ContentItem ci = item.getRecord().getItem();

			// set item title
			String title = ci.getName();
			mTitle.setText(title);
			mTitle.setTextColor(Color.parseColor("#484848"));

			// the item load from history has no CompletedLength
			if (sr.getStatus() == Status.COMPLETED)
				item.mCompletedLength = ci.getSize();

			boolean completed = (item.mCompletedLength == ci.getSize());
			// if transfering
			if (!completed && sr.getStatus() != Status.ERROR) {
				int progress = (int) ((double) item.mCompletedLength / (double) ci.getSize() * PROGRESS_BAR_MAX);
				showProgressBar(progress);
				mButtonCancel.setVisibility(View.VISIBLE);
				mButtonOpen.setVisibility(View.GONE);
				// if transfer successfully completed
			} else {
				showProgressBar(PROGRESS_BAR_MAX);
				mButtonCancel.setVisibility(View.GONE);
				mButtonOpen.setVisibility(View.VISIBLE);
				mProgressInfo.setText(String.format("%s kb / %s kb", item.mCompletedLength / 1024, item.mCompletedLength / 1024));
			}

			// the file type
			mType.setText(ci.getContentType().toString());

			Drawable d = mItem.getThumbnail();
			if (d == null)
				// asynchronously
				fillThumbnail();
			else
				// synchronize
				mThumbnail.setImageDrawable(d);

		} catch (Exception e) {
			Log.w(TAG, e.getMessage());
		}
	}

	private void bindCollectionRecord(ShareItem item) {
		try {
			mItem = item;
			ShareRecord sr = item.getRecord();
			ShareCollection sc = sr.getCollection();

			// set item title
			String title = sc.getName();
			mTitle.setText(title);
			mTitle.setTextColor(Color.parseColor("#484848"));

			// the item load from history has no CompletedLength
			if (sr.getStatus() == Status.COMPLETED)
				item.mCompletedLength = sc.getSize();

			boolean completed = (item.mCompletedLength == sc.getSize());
			// if transfering
			if (!completed && sr.getStatus() != Status.ERROR) {
				int progress = (int) ((double) item.mCompletedLength / (double) sc.getSize() * PROGRESS_BAR_MAX);
				showProgressBar(progress);
				// if transfer successfully completed
			}

			// the file type
			mType.setText(sc.getType().toString());

			// the progress details
			mProgressInfo.setText(String.format("%s/%s", sc.getSize(), item.mCompletedLength));

			Drawable d = mItem.getThumbnail();
			if (d == null)
				// asynchronously
				fillThumbnail();
			else
				// synchronize
				mThumbnail.setImageDrawable(d);

		} catch (Exception e) {
			Log.w(TAG, e.getMessage());
		}
	}

	public ShareItem getShareItem() {
		return mItem;
	}

	private void showProgressBar(int progress) {
		mProgress.setProgress(progress);
		mProgress.setVisibility(View.VISIBLE);
	}

	private int fetchErrorResourceId(TransmitException exception) {
		int resourceId = -1;
		switch (exception.getCode()) {
		case TransmitException.Canceled:
			resourceId = R.string.receive_canceled;
			break;

		case TransmitException.FileNotFound:
			boolean sdcardErr = !Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
			resourceId = sdcardErr ? R.string.sdcard_unavailable : R.string.receive_cannot_created_file;
			break;

		default:
			resourceId = R.string.error_transmit;
			break;
		}
		return resourceId;
	}

	public void fillThumbnail() {
		Drawable d = null;
		if (mItem.getRecord().getType() == ShareType.RECEIVE)
			d = fetchReceiveThumbnail(mContext, mItem, mThumbnail.getWidth(), mThumbnail.getHeight());
		else
			d = fetchSendThumbnail(mContext, mItem);

		if (d != null)
			mItem.setThumbnail(d);
		mHandler.sendMessage(mHandler.obtainMessage(MSG_FIT_THUMBNAIL));
	}

	@SuppressWarnings("deprecation")
	private Drawable fetchReceiveThumbnail(Context context, ShareItem item, int width, int height) {
		try {
			if (item.getRecord().getRecordType() == RecordType.ITEM) {
				ContentItem ci = item.getRecord().getItem();

				if (ci.getContentType() == ContentType.FILE) {
					String mimeType = FileUtils.getMimeType(new File(ci.getFilePath()));

					Log.e("Log", "RecordView" + ci.getFilePath());

					Drawable drawable = ThumbnailUtils.getDrawableForMimetype(context, mimeType);
					if (drawable != null) {
						return drawable;
					}

					return context.getResources().getDrawable((R.drawable.item_bg_unknown));
				}

				Bitmap bitmap = ThumbnailUtils.getThumbnail(ci.getThumbnailPath(), width, height);
				if (bitmap != null) {
					return new BitmapDrawable(bitmap);
				}

				switch (item.getRecord().getItem().getContentType()) {
				case MUSIC:
					return context.getResources().getDrawable((R.drawable.item_bg_music));
				case CONTACT:
					return context.getResources().getDrawable((R.drawable.item_bg_contact));
				case PHOTO:
					return context.getResources().getDrawable((R.drawable.item_bg_photo));
				case VIDEO:
					return context.getResources().getDrawable((R.drawable.item_bg_video));
				default:
					return context.getResources().getDrawable((R.drawable.item_bg_unknown));
				}
			} else if (item.getRecord().getRecordType() == RecordType.COLLECTION) {
				return context.getResources().getDrawable((R.drawable.item_bg_unknown));
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	private Drawable fetchSendThumbnail(Context context, ShareItem item) {
		Drawable drawable = null;
		if (item.getRecord().getRecordType() == RecordType.ITEM) {
			ContentType ct = item.getRecord().getItem().getContentType();
			String metadataId = item.getRecord().getItem().getId();
			Bitmap bm;
			switch (ct) {
			case APP:

				return ThumbnailUtils.getAppThumbnail(context, metadataId);
			case MUSIC:
				bm = ThumbnailUtils.getAudioThumbnail(context, metadataId);
				if (bm != null) {
					drawable = new BitmapDrawable(bm);
				} else {
					drawable = context.getResources().getDrawable(R.drawable.item_bg_music);
				}
				break;
			case CONTACT:
				return context.getResources().getDrawable((R.drawable.item_bg_contact));
			case VIDEO:
				bm = ThumbnailUtils.getVideoThumbnail(context, metadataId);
				if (bm != null) {
					drawable = new BitmapDrawable(bm);
				} else {
					drawable = context.getResources().getDrawable(R.drawable.item_bg_video);
				}
				break;
			case FILE:
				return context.getResources().getDrawable((R.drawable.ex_folder));
			case PHOTO:
				bm = ThumbnailUtils.getPhotoThumbnail(context, metadataId);
				if (bm != null) {
					drawable = new BitmapDrawable(bm);
				} else {
					drawable = context.getResources().getDrawable(R.drawable.item_bg_photo);
				}
				break;

			default:
				break;
			}
		} else if (item.getRecord().getRecordType() == RecordType.COLLECTION) {
			drawable = context.getResources().getDrawable(R.drawable.item_bg_unknown);
		}
		return drawable;
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_UPDATE_UI:
				updateUI(msg.obj);
				break;
			case MSG_FIT_THUMBNAIL:
				mThumbnail.setImageDrawable(mItem.getThumbnail());
				break;
			default:
				break;
			}
		}

	};

}