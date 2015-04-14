package com.plusend.components.receivers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.plusend.trans.R;
//搜索到的设备的  ReceiverFragment.java    ListView mReceivers
public class ReceiverAdapter extends BaseAdapter {
    private static final String TAG = "ReceiverAdapter";

    private List<String> mReceivers = new ArrayList<String>();
    private Map<String, String> mSsids = new HashMap<String, String>();
    private OnClickListener mOnClickListener;
    private Context mContext;
    private int mLayoutId;

    public ReceiverAdapter(Context context, int resid, OnClickListener listener) {
        mContext = context;
        mLayoutId = resid;
        mOnClickListener = listener;
    }

    @Override
    public int getCount() {
        return mReceivers.size();
    }

    @Override
    public Object getItem(int position) {
        return mReceivers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String ssid = mReceivers.get(position);
        ItemWrapper wrapper;
        if (convertView == null) {
            convertView = View.inflate(mContext, mLayoutId, null);
            wrapper = new ItemWrapper(convertView);
            
            Log.e("Log", "ReceiverAdapter : "+wrapper.getSsid());
            
            convertView.setTag(wrapper);
            if (mOnClickListener != null)
                wrapper.mNickname.setOnClickListener(mOnClickListener);
        } else {
            wrapper = (ItemWrapper)convertView.getTag();
        }
        wrapper.setSsid(ssid);
        wrapper.mNickname.setText(mSsids.get(ssid));
        Log.e("Tag", "mSsids.get(ssid)"+mSsids.get(ssid));
        wrapper.mNickname.setTag(ssid);
        return convertView;
    }

    public void addItem(int position, Pair<String, String> receiver) {
        if (mSsids.containsKey(receiver.first))
            return;
        Log.d(TAG, "add receiver's ssid: " + receiver.first);
        mReceivers.add(position, receiver.first);
        mSsids.put(receiver.first, receiver.second);
    }

    public void removeItem(String ssid) {
        if (!mSsids.containsKey(ssid))
            return;

        mReceivers.remove(ssid);
        mSsids.remove(ssid);
    }

    public void removeAll() {
        mReceivers.clear();
        mSsids.clear();
    }

    public static class ItemWrapper {
        private TextView mNickname;
        private String mSsid;

        public ItemWrapper(View view) {
            mNickname = (TextView)view.findViewById(R.id.nickname);
        }

        public String getSsid() {
            return mSsid;
        }

        public void setSsid(String ssid) {
            mSsid = ssid;
        }
    }

	// private static Comparator<UserInfo> mComparator = new
	// Comparator<UserInfo>() {
	// public int compare(UserInfo obj1, UserInfo obj2) {
	// return obj1.name.compareTo(obj2.name);
	// }
	// };

}
