package com.plusend.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.lenovo.content.base.ContentType;
import com.plusend.utils.BitmapMemoryCache;
import com.plusend.utils.PingYinUtil;
import com.plusend.trans.R;

public class SelectVideoView extends PagerView {

	private MyAdapter mMusicAdapter;
	private MyData myData;
	public ArrayList<VideoInfo> videoList = new ArrayList<VideoInfo>();

	private ExpandableListView expandableListView_one;

	public SelectVideoView(Context context) {
		super(context);
		title = "Video";
		commonContentType = ContentType.VIDEO;

		View view = View.inflate(context, R.layout.select_music_view, null);
		addView(view);

		expandableListView_one = (ExpandableListView) findViewById(R.id.expandableListView);

		expandableListView_one.setVisibility(View.GONE);

		myData = new MyData();

		mMusicAdapter = new MyAdapter(getContext(), myData);
		expandableListView_one.setAdapter(mMusicAdapter);

		expandableListView_one.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				CheckBox checkBox = (CheckBox) v.findViewById(R.id.musicCheckBox);
				if (getSelectedItems().contains(myData.subList.get(groupPosition).get(childPosition).item)) {
					getSelectedItems().remove(myData.subList.get(groupPosition).get(childPosition).item);
					checkBox.setChecked(false);
				} else {
					getSelectedItems().add(myData.subList.get(groupPosition).get(childPosition).item);
					checkBox.setChecked(true);
				}
				notifyItemChanged();
				return false;
			}
		});
	}

	private class VideoInfo {
		private String title;
		private long videoSize;
		private String path;
		private Pair<ContentType, String> item;
	}

	public class MyAdapter extends BaseExpandableListAdapter {
		public Context context;
		public MyData myData;
		private LayoutInflater layoutInflater;

		public MyAdapter(Context context, MyData myData2) {
			this.context = context;
			this.myData = myData2;
			layoutInflater = LayoutInflater.from(context);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return myData.getSubList().get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View view = layoutInflater.inflate(R.layout.child, null);
			ImageView image = (ImageView) view.findViewById(R.id.music_item_image);
			TextView title = (TextView) view.findViewById(R.id.music_item_title);
			TextView musicSize = (TextView) view.findViewById(R.id.music_item_size);
			CheckBox musicCheck = (CheckBox) view.findViewById(R.id.musicCheckBox);

			VideoInfo info = (VideoInfo) getChild(groupPosition, childPosition);
			long size = info.videoSize;
			String size2;
			if (size >= 1024) {
				size = size / 1024;
				if (size >= 1024) {
					size = size / 1024;
					size2 = size + " MB";
				} else {
					size2 = size + " KB";
				}
			} else {
				size2 = size + " B";
			}
			title.setText(info.title);
			musicSize.setText(size2);

			final String videoKey = info.item.second;
			BitmapMemoryCache cache = BitmapMemoryCache.getInstance();
			final Bitmap bitmap = cache.getBitmap("video" + videoKey);
			if (bitmap != null) {
				image.setImageBitmap(bitmap);
			} else {
				image.setImageResource(R.drawable.item_bg_video);
				final ImageView iv = image;
				new Thread() {
					public void run() {
						AsyncTask<Integer, Void, Bitmap> task = new AsyncTask<Integer, Void, Bitmap>() {
							@Override
							protected Bitmap doInBackground(Integer... params) {
								BitmapFactory.Options options = new BitmapFactory.Options();
								options.inDither = false;
								options.inPreferredConfig = Bitmap.Config.ARGB_8888;
								Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(SelectVideoView.this.getContext().getContentResolver(), Long.parseLong(videoKey), Video.Thumbnails.MICRO_KIND,
										options);
								String cacheId = "video" + videoKey;
								BitmapMemoryCache.getInstance().addBitmap(cacheId, bitmap);
								return bitmap;
							}
						};
						try {
							final Bitmap b = task.execute().get();
							iv.post(new Runnable() {
								@Override
								public void run() {
									iv.setImageBitmap(b);
									iv.invalidate();
								}
							});
						} catch (Exception e) {
						}
					};
				}.start();
			}
			musicCheck.setChecked(getSelectedItems().contains(info.item));

			return view;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return myData.getSubList().get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return myData.getSupList().get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return myData.getSupList().size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View group_v = layoutInflater.inflate(R.layout.group, null);
			TextView group_tv = (TextView) group_v.findViewById(R.id.group_tv);
			group_tv.setText(groupPosition + 1 + " " + getGroup(groupPosition).toString());
			ImageView group_img = (ImageView) group_v.findViewById(R.id.group_img);

			if (isExpanded) {
				group_img.setImageResource(R.drawable.open);
			} else {
				group_img.setImageResource(R.drawable.close);
			}
			return group_v;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}

	public class MyData {
		private ArrayList<String> supList = new ArrayList<String>();
		private ArrayList<ArrayList<VideoInfo>> subList = new ArrayList<ArrayList<VideoInfo>>();

		public ArrayList<ArrayList<VideoInfo>> getSubList() {
			return subList;
		}

		public void setSubList(ArrayList<ArrayList<VideoInfo>> subList) {
			this.subList = subList;
		}

		public ArrayList<String> getSupList() {
			return supList;
		}

		public void setSupList(ArrayList<String> supList) {
			this.supList = supList;
		}
	}

	public void fillSelectedItem(Pair<ContentType, String> item, TextView title, ImageView image) {
		for (VideoInfo info : videoList) {
			if (info.item.equals(item)) {
				title.setText(info.title);
				Bitmap bitmap = BitmapMemoryCache.getInstance().getBitmap("video" + item.second);
				if (bitmap != null) {
				} else {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inDither = false;
					options.inPreferredConfig = Bitmap.Config.ARGB_8888;
					bitmap = MediaStore.Video.Thumbnails.getThumbnail(SelectVideoView.this.getContext().getContentResolver(), Long.parseLong(item.second), Video.Thumbnails.MICRO_KIND, options);
				}
				image.setImageBitmap(bitmap);
				break;
			}
		}
	}

	public void loadDataAsync() {
		String[] mediaColumns = new String[] { MediaStore.Video.Media._ID, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DATA };

		Cursor cursor = getContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaColumns, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				VideoInfo info = new VideoInfo();
				info.item = new Pair<ContentType, String>(commonContentType, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)));
				info.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
				info.videoSize = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
				info.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
				info.path = info.path.substring(0, info.path.lastIndexOf("/"));
				videoList.add(info);
			} while (cursor.moveToNext());
		}
		cursor.close();

		for (VideoInfo info : videoList) {
			if (myData.supList.contains(info.path)) {

			} else {
				myData.supList.add(info.path);
				myData.subList.add(new ArrayList<VideoInfo>());
			}

		}

		for (VideoInfo info : videoList) {
			for (int i = 0; i < myData.supList.size(); i++) {
				if (info.path.equals(myData.supList.get(i))) {
					myData.subList.get(i).add(info);
					break;
				}
			}
		}

		for (ArrayList<VideoInfo> info : myData.subList) {
			Collections.sort(info, new Comparator<VideoInfo>() {

				@Override
				public int compare(VideoInfo o1, VideoInfo o2) {

					String str1 = PingYinUtil.getPingYin(o1.title);
					String str2 = PingYinUtil.getPingYin(o2.title);
					return str1.compareTo(str2);
				}

			});
		}

		new Handler(Looper.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				mMusicAdapter.notifyDataSetChanged();
				SelectVideoView.this.findViewById(R.id.select_music_view_loading).setVisibility(View.GONE);
				expandableListView_one.setVisibility(View.VISIBLE);
				//expandableListView_one.expandGroup(0);
			}
		});
	}

	public void onUpdate() {
		mMusicAdapter.notifyDataSetChanged();
	}

}