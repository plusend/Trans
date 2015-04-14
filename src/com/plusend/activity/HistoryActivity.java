package com.plusend.activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lenovo.content.base.ContentType;
import com.plusend.components.records.HistoryRecord;
import com.plusend.utils.BitmapMemoryCache;
import com.plusend.utils.OpenFile;
import com.plusend.utils.ThumbnailUtils;
import com.plusend.trans.R;

public class HistoryActivity extends Activity {

	private HistoryAdapter historyAdapter = null;
	private ArrayList<HistoryRecord> historyList = new ArrayList<HistoryRecord>();

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);

		ListView historyListView = (ListView) findViewById(R.id.historyListview);

		findViewById(R.id.history_back).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		findViewById(R.id.history_clear).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				FileOutputStream outStream;
				try {
					outStream = HistoryActivity.this.openFileOutput("history.txt", Context.MODE_PRIVATE);
					ObjectOutputStream oos = new ObjectOutputStream(outStream);
					oos.writeObject(new ArrayList<HistoryRecord>());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				historyList = new ArrayList<HistoryRecord>();
				historyAdapter.notifyDataSetChanged();
			}

		});

		historyListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				if (historyList.get(position).getMode().equals("send") && historyList.get(position).getType().equals(ContentType.CONTACT.toString())) {
				} else {
					startActivity(OpenFile.openFile(historyList.get(position).getUri()));
				}
			}

		});
		historyAdapter = new HistoryAdapter();
		historyListView.setAdapter(historyAdapter);

		try {
			FileInputStream fis = this.openFileInput("history.txt");
			ObjectInputStream ois = new ObjectInputStream(fis);
			historyList = (ArrayList<HistoryRecord>) ois.readObject();

			Collections.reverse(historyList);

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
	}

	private class ViewHolder {
		public ImageView img;
		public TextView title;
		public TextView type;
		private ImageView send;
		private ImageView receive;
	}

	private class HistoryAdapter extends BaseAdapter {

		public int getCount() {
			return historyList.size();
		}

		public Object getItem(int position) {
			return historyList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(HistoryActivity.this, R.layout.history_item, null);
				holder.img = (ImageView) convertView.findViewById(R.id.history_image);
				holder.send = (ImageView) convertView.findViewById(R.id.history_send);
				holder.receive = (ImageView) convertView.findViewById(R.id.history_receive);
				holder.title = (TextView) convertView.findViewById(R.id.histoty_title);
				holder.type = (TextView) convertView.findViewById(R.id.history_type);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			HistoryRecord info = historyList.get(position);
			holder.title.setText(info.getName());
			holder.type.setText(info.getType());

			BitmapMemoryCache cache = BitmapMemoryCache.getInstance();

			Bitmap bitmap = cache.getBitmap("history" + info.getId());

			if (info.getMode().equals("send")) {
				holder.send.setVisibility(View.VISIBLE);
				holder.receive.setVisibility(View.GONE);

				if (bitmap != null) {
					holder.img.setImageBitmap(bitmap);
				} else {
					bitmap = ThumbnailUtils.getThumbnail(info, HistoryActivity.this);
					if (bitmap != null) {
						holder.img.setImageBitmap(bitmap);
					} else
						holder.img.setImageResource(R.drawable.item_bg_unknown);
					BitmapMemoryCache.getInstance().addBitmap("history" + info.getId(), bitmap);
				}
			} else {
				if (bitmap != null) {
					holder.img.setImageBitmap(bitmap);
				} else {
					bitmap = ThumbnailUtils.getThumbnail(info.getThumb(), 48, 48);
					if (bitmap != null) {
						holder.img.setImageBitmap(bitmap);
					} else
						holder.img.setImageResource(R.drawable.item_bg_unknown);
					BitmapMemoryCache.getInstance().addBitmap("history" + info.getId(), bitmap);
				}
				holder.send.setVisibility(View.GONE);
				holder.receive.setVisibility(View.VISIBLE);
			}
			return convertView;
		}
	}

}
