package com.plusend.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.lenovo.content.base.ContentType;
import com.plusend.utils.PingYinUtil;
import com.plusend.trans.R;

public class SelectAppView extends PagerView {

	private AppAdapter appAdapter;

	private ArrayList<AppInfo> appList;
	private ArrayList<AppInfo> searchAppList = new ArrayList<AppInfo>();

	private GridView mGridView;

	public SelectAppView(Context context) {
		super(context);
		title = "App";
		commonContentType = ContentType.APP;

		View view = View.inflate(context, R.layout.select_app_view, null);
		addView(view);
		appList = new ArrayList<AppInfo>();
		mGridView = (GridView) view.findViewById(R.id.appgridView);
		mGridView.setVisibility(View.GONE);
		final EditText searchText = (EditText) view.findViewById(R.id.search_text);
		Button button = (Button) view.findViewById(R.id.search);

		appAdapter = new AppAdapter(context);
		mGridView.setAdapter(appAdapter);
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				ViewHolder holder = ((ViewHolder) view.getTag());

				if (getSelectedItems().contains(holder.item)) {
					holder.appChecked.setVisibility(View.GONE);
					getSelectedItems().remove(holder.item);
				} else {
					holder.appChecked.setVisibility(View.VISIBLE);
					getSelectedItems().add(holder.item);
				}
				notifyItemChanged();
			}
		});
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				String tag = searchText.getText().toString();
				searchAppList.clear();
				for (AppInfo info : appList) {
					if (info.item.second.contains(tag) || info.AppName.contains(tag))
						searchAppList.add(info);
				}
				appAdapter.notifyDataSetChanged();
			}
		});

	}

	@Override
	public void fillSelectedItem(Pair<ContentType, String> item, TextView title, ImageView image) {
		for (AppInfo info : appList) {
			if (info.item.equals(item)) {
				title.setText(info.AppName);
				image.setImageDrawable(info.AppIcon);
				break;
			}
		}
	}

	@Override
	public void loadDataAsync() {
		List<PackageInfo> packageList = getContext().getPackageManager().getInstalledPackages(0);
		for (PackageInfo packageInfo : packageList) {
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				AppInfo tmpInfo = new AppInfo();
				tmpInfo.item = new Pair<ContentType, String>(commonContentType, packageInfo.packageName);
				tmpInfo.AppName = packageInfo.applicationInfo.loadLabel(getContext().getPackageManager()).toString();
				tmpInfo.AppIcon = packageInfo.applicationInfo.loadIcon(getContext().getPackageManager());
				appList.add(tmpInfo);// 如果非系统应用，则添加至appList
			}
		}

		for (AppInfo info : appList) {
			searchAppList.add(info);
		}

		Collections.sort(searchAppList, new Comparator<AppInfo>() {

			@Override
			public int compare(AppInfo o1, AppInfo o2) {
				String str1 = PingYinUtil.getPingYin(o1.AppName);
				String str2 = PingYinUtil.getPingYin(o2.AppName);
				return str1.compareTo(str2);
			}

		});

		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				appAdapter.notifyDataSetChanged();
				SelectAppView.this.findViewById(R.id.select_app_view_loading).setVisibility(View.GONE);
				mGridView.setVisibility(View.VISIBLE);
			}
		});
	}

	private class ViewHolder {
		private ImageView appIconView;
		private TextView appNameView;
		private ImageView appChecked;
		private Pair<ContentType, String> item;
	}

	private class AppAdapter extends BaseAdapter {
		Context mContext;

		public AppAdapter(Context context) {
			mContext = context;
		}

		public int getCount() {
			return searchAppList.size();
		}

		public Object getItem(int position) {
			return searchAppList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.app_item, null);
				holder.appIconView = (ImageView) convertView.findViewById(R.id.appImageview);
				holder.appNameView = (TextView) convertView.findViewById(R.id.appTextview);
				holder.appChecked = (ImageView) convertView.findViewById(R.id.app_item_select);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.item = searchAppList.get(position).item;
			holder.appNameView.setText(searchAppList.get(position).AppName);
			holder.appIconView.setImageDrawable(searchAppList.get(position).AppIcon);
			holder.appChecked.setVisibility(getSelectedItems().contains(holder.item) ? View.VISIBLE : View.GONE);
			return convertView;
		}
	}

	private static class AppInfo {
		private String AppName;
		private Drawable AppIcon;
		private Pair<ContentType, String> item;
	}

	@Override
	public void onUpdate() {
		appAdapter.notifyDataSetChanged();
	}

}
