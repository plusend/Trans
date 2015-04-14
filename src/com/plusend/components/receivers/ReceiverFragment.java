package com.plusend.components.receivers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lenovo.network.base.AbstractClientListener;
import com.lenovo.network.base.Device;
import com.lenovo.network.base.INetworkStatusListener;
import com.lenovo.sharesdk.ShareWrapper;
import com.plusend.activity.SelectActivity;
import com.plusend.controller.TransController;
import com.plusend.trans.R;

public class ReceiverFragment extends Fragment {

	private static final int MSG_SHOW_SEARCHING_VIEW = 0x01;
	private static final int MSG_SHOW_RECEIVER_VIEW = 0x02;
	private static final int MSG_UPDATE_UI = 0x03;

	private FrameLayout mLayoutSearching;
	private ListView mReceivers; // ReceiverAdapter
	private TextView Tip;

	private ReceiverAdapter mReceiverAdapter;
	private ShareWrapper sdk;
	private Map<String, String> mServers;
	private boolean isShowingReceivers = false;
	private OnSelectReceivrListener mSelectReceiverListener;

	public interface OnSelectReceivrListener {
		public void onSelect(String receiver);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_receivers, container, false);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mReceivers = (ListView) view.findViewById(R.id.list_receiver);
		Tip = (TextView) view.findViewById(R.id.receive_picker_select);
		mLayoutSearching = (FrameLayout) view.findViewById(R.id.searching_view);
		mReceiverAdapter = new ReceiverAdapter(getActivity(), R.layout.receiver, new OnClickListener() {
			@Override
			public void onClick(View v) {
				String ssid = (String) v.getTag();
				sdk.connect(ssid);

				mSelectReceiverListener.onSelect(mServers.get(ssid));
			}
		});
		mReceivers.setAdapter(mReceiverAdapter);

		mServers = new HashMap<String, String>();
		sdk = ShareWrapper.getInstance();
		sdk.addNetworkStatusListener(mApClientListener);
	}

	@Override
	public void onDestroy() {
		if (sdk != null)
			sdk.removeNetworkStatusListener(mApClientListener);
		Log.e("Log", "activity " + getActivity());
		getActivity().finish();
		mServers.clear();
		mReceiverAdapter.removeAll();
		// mReceiverAdapter.notifyDataSetChanged();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		mServers.clear();
		mReceiverAdapter.removeAll();
		// mReceiverAdapter.notifyDataSetChanged();
		super.onPause();
	}

	public void setOnSelectReceiver(OnSelectReceivrListener l) {
		mSelectReceiverListener = l;
	}

	private INetworkStatusListener mApClientListener = new AbstractClientListener() {

		@Override
		public void onClientStatusChanged(boolean succeeded) {
			Log.e("onClientStatusChanged", "onClientStatusChanged" + succeeded);
			if (!succeeded) {
				Intent intent = new Intent(getActivity(), SelectActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				TransController.getInstance().getSelectedItems().clear();
				startActivity(intent);
			}
		}

		@Override
		public void onScanResult(final List<Device> receivers) {
			if (receivers.isEmpty()) {
				mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_SEARCHING_VIEW));
				return;
			}

			mServers.clear();
			mReceiverAdapter.removeAll();
			for (Device receiver : receivers) {
				mServers.put(receiver.getId(), receiver.getNickname());
				mReceiverAdapter.addItem(0, new Pair<String, String>(receiver.getId(), receiver.getNickname()));
			}
			mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_UI));
			mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_RECEIVER_VIEW));

			if (!isShowingReceivers)
				mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_RECEIVER_VIEW));
		}

		@Override
		public void onScanFailed() {
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MSG_SHOW_SEARCHING_VIEW:
				showReceivers(false);
				Tip.setVisibility(View.GONE);
				break;
			case MSG_SHOW_RECEIVER_VIEW:
				showReceivers(true);
				Tip.setVisibility(View.VISIBLE);
				break;
			case MSG_UPDATE_UI:
				mReceiverAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
		}
	};

	private void showReceivers(boolean isShow) {
		if (isShow) {
			mReceivers.setVisibility(View.VISIBLE);
			mLayoutSearching.setVisibility(View.GONE);
			isShowingReceivers = true;
			return;
		}

		mReceivers.setVisibility(View.GONE);
		mLayoutSearching.setVisibility(View.VISIBLE);
	}
}
