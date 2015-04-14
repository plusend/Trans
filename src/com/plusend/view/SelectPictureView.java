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
import android.provider.MediaStore.Images;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.lenovo.content.base.ContentType;
import com.plusend.utils.BitmapMemoryCache;
import com.plusend.utils.PingYinUtil;
import com.plusend.trans.R;

public class SelectPictureView extends PagerView {

	private MyAdapter mMusicAdapter;
	private MyData myData;
	private ExpandableListView expandableListView_one;

	public ArrayList<PictureInfo> pictureList = new ArrayList<PictureInfo>();

	public SelectPictureView(Context context) {
		super(context);
		commonContentType = ContentType.PHOTO;
		title = "Picture";

		View view = View.inflate(context, R.layout.select_music_view, null);
		addView(view);

		expandableListView_one = (ExpandableListView) findViewById(R.id.expandableListView);
		expandableListView_one.setVisibility(View.GONE);
		myData = new MyData();
		mMusicAdapter = new MyAdapter(getContext());
		expandableListView_one.setAdapter(mMusicAdapter);

	}

	@Override
	public void fillSelectedItem(Pair<ContentType, String> item, TextView title, ImageView image) {
		for (PictureInfo info : pictureList) {
			if (info.item.equals(item)) {
				title.setText(info.title);
				Bitmap bitmap = BitmapMemoryCache.getInstance().getBitmap("image" + info.item.second);
				if (bitmap != null) {
				} else {
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inDither = false;
					options.inPreferredConfig = Bitmap.Config.ARGB_8888;
					bitmap = MediaStore.Images.Thumbnails.getThumbnail(SelectPictureView.this.getContext().getContentResolver(), Long.parseLong(item.second), Images.Thumbnails.MICRO_KIND, options);
				}
				image.setImageBitmap(bitmap);
				break;
			}
		}
	}

	@Override
	public void loadDataAsync() {
		String[] mediaColumns = new String[] { MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE, MediaStore.Images.Media.DATA };
		Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaColumns, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				PictureInfo info = new PictureInfo();
				info.item = new Pair<ContentType, String>(ContentType.PHOTO, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)));
				info.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
				info.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
				info.path = info.path.substring(0, info.path.lastIndexOf("/"));
				pictureList.add(info);
			} while (cursor.moveToNext());
		}
		cursor.close();

		for (PictureInfo info : pictureList) {
			if (myData.supList.contains(info.path)) {

			} else {
				myData.supList.add(info.path);
				myData.subList.add(new ArrayList<PictureInfo>());
			}

		}

		for (PictureInfo info : pictureList) {
			for (int i = 0; i < myData.supList.size(); i++) {
				if (info.path.equals(myData.supList.get(i))) {
					myData.subList.get(i).add(info);
					break;
				}
			}
		}

		for (ArrayList<PictureInfo> info : myData.subList) {
			Collections.sort(info, new Comparator<PictureInfo>() {

				@Override
				public int compare(PictureInfo o1, PictureInfo o2) {

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
				SelectPictureView.this.findViewById(R.id.select_music_view_loading).setVisibility(View.GONE);
				expandableListView_one.setVisibility(View.VISIBLE);
				expandableListView_one.expandGroup(0);
			}
		});
	}

	public class PictureInfo {
		private String title;
		private Pair<ContentType, String> item;
		private String path;
	}

	public class MyData {
		private ArrayList<String> supList = new ArrayList<String>();
		private ArrayList<ArrayList<PictureInfo>> subList = new ArrayList<ArrayList<PictureInfo>>();

		public ArrayList<ArrayList<PictureInfo>> getSubList() {
			return subList;
		}

		public void setSubList(ArrayList<ArrayList<PictureInfo>> subList) {
			this.subList = subList;
		}

		public ArrayList<String> getSupList() {
			return supList;
		}

		public void setSupList(ArrayList<String> supList) {
			this.supList = supList;
		}
	}

	class MyAdapter extends BaseExpandableListAdapter {

		public Context context;
		private LayoutInflater layoutInflater;

		public MyAdapter(Context context) {
			this.context = context;
			layoutInflater = LayoutInflater.from(context);
		}

		@Override
		public int getGroupCount() {
			return myData.supList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return myData.getSupList().get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return myData.getSubList().get(groupPosition).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return true;
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
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			if (convertView == null) {
				layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.select_picture_view, null);
				PictureGridView grid;
				grid = (PictureGridView) convertView.findViewById(R.id.pictureGridView);
				grid.setNumColumns(4);// 设置每行列数
				grid.setGravity(Gravity.CENTER);// 位置居中
				grid.setHorizontalSpacing(10);
				grid.setVerticalSpacing(20);
				PictureAdapter adapter = new PictureAdapter();
				adapter.setGroupIndex(groupPosition);
				grid.setAdapter(adapter);// 设置菜单Adapter

				grid.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
						ViewHolder holder = (ViewHolder) v.getTag();
						ImageView image = holder.checkImage;
						if (getSelectedItems().contains(holder.item)) {
							getSelectedItems().remove(holder.item);
							image.setVisibility(View.GONE);
						} else {
							getSelectedItems().add(holder.item);
							image.setVisibility(View.VISIBLE);
						}
						notifyItemChanged();
					}
				});
			} else {
				PictureAdapter adapter = ((PictureAdapter) ((PictureGridView) convertView.findViewById(R.id.pictureGridView)).getAdapter());
				adapter.setGroupIndex(groupPosition);
				adapter.notifyDataSetChanged();
			}
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	private class PictureAdapter extends BaseAdapter {

		ArrayList<PictureInfo> list;

		public void setGroupIndex(int groupIndex) {
			list = myData.subList.get(groupIndex);
		}

		public int getCount() {
			return list.size();
		}

		public Object getItem(int p) {
			return list.get(p);
		}

		public long getItemId(int p) {
			return p;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(getContext(), R.layout.picture_item, null);
				holder.thumbImage = (ImageView) convertView.findViewById(R.id.picture_item_image);
				holder.checkImage = (ImageView) convertView.findViewById(R.id.picture_item_select);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final String imageKey = "image" + list.get(position).item.second;
			BitmapMemoryCache cache = BitmapMemoryCache.getInstance();
			final Bitmap bitmap = cache.getBitmap(imageKey);
			if (bitmap != null) {
				holder.thumbImage.setImageBitmap(bitmap);
			} else {
				holder.thumbImage.setImageResource(R.drawable.item_bg_photo);
				final int index = position;
				final ImageView iv = holder.thumbImage;
				final View currentView = convertView;
				new Thread() {
					public void run() {
						AsyncTask<Integer, Void, Bitmap> task = new AsyncTask<Integer, Void, Bitmap>() {

							@Override
							protected Bitmap doInBackground(Integer... params) {
								BitmapFactory.Options options = new BitmapFactory.Options();
								options.inDither = false;
								options.inPreferredConfig = Bitmap.Config.ARGB_8888;
								Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(SelectPictureView.this.getContext().getContentResolver(), Long.parseLong(list.get(index).item.second),
										Images.Thumbnails.MICRO_KIND, options);
								String cacheId = "image" + list.get(index).item.second;
								BitmapMemoryCache.getInstance().addBitmap(cacheId, bitmap);
								return bitmap;

							}
						};
						try {
							final Bitmap b = task.execute(index).get();
							if (list.get(index).item.equals(((ViewHolder) (currentView.getTag())).item)) {
								iv.post(new Runnable() {
									@Override
									public void run() {
										iv.setImageBitmap(b);
										iv.invalidate();
									}
								});
							}
						} catch (Exception e) {
						}

					};
				}.start();
			}
			holder.item = list.get(position).item;
			holder.checkImage.setVisibility(getSelectedItems().contains(holder.item) ? View.VISIBLE : View.GONE);
			return convertView;
		}

	}

	private static class ViewHolder {
		private ImageView thumbImage;
		private ImageView checkImage;
		private Pair<ContentType, String> item;
	}

	@Override
	public void onUpdate() {
		mMusicAdapter.notifyDataSetChanged();
	}

}
