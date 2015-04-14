//RecordFragment

package com.plusend.components.records;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lenovo.channel.base.ShareRecord;

public class RecordAdapter extends BaseAdapter {
	@SuppressWarnings("unused")
	private static final String TAG = "RecordAdapter";

	private Context mContext;
	private List<ShareItem> mShareItems = new ArrayList<ShareItem>();

	public RecordAdapter(Context context) {
		mContext = context;
	}

	@Override
	public int getCount() {
		return mShareItems.size();
	}

	@Override
	public ShareItem getItem(int position) {
		return mShareItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ShareItem si = mShareItems.get(position);
		assert si != null;

		RecordView itemVw;
		if (convertView == null) {
			itemVw = new RecordView(mContext);
			convertView = itemVw;
		} else {
			itemVw = (RecordView) convertView;

			// clear the old relation between ShareItem and ItemView
			ShareItem oldItem = itemVw.getShareItem();
			oldItem.deleteObserver(itemVw);
		}
		si.addObserver(itemVw);
		itemVw.bindItem(si);
		return convertView;
	}

	public ShareItem getItem(ShareRecord sr) {
		for (ShareItem item : mShareItems) {
			if (sr.getUniqueId().equals(item.getRecord().getUniqueId()))
				return item;
		}
		return null;
	}

	public void addItem(int position, ShareItem item) {
		mShareItems.add(position, item);
	}

	public void removeItem(ShareItem item) {
		mShareItems.remove(item);
	}
}
