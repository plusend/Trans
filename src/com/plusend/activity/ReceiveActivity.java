package com.plusend.activity;

import java.util.HashSet;
import java.util.Set;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;

import com.lenovo.channel.base.IUserListener;
import com.lenovo.channel.base.UserInfo;
import com.lenovo.network.base.AbstractServerListener;
import com.lenovo.network.base.INetworkStatusListener;
import com.lenovo.sharesdk.ShareWrapper;
import com.plusend.trans.R;

public class ReceiveActivity extends FragmentActivity {
	private static final String TAG = "ReceiveActivity";

	private FrameLayout mLayoutWaiting;
	private FrameLayout mLayoutReceive;// RecordFragment

	private Button receiveBack,receiveFinish;
	private ShareWrapper sdk;
	private Set<String> mAcceptUsers;

	private static final int MSG_SHOW_WAITING_VIEW = 0x01;
	private static final int MSG_SHOW_RECORDS_VIEW = 0x02;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_receive);

		mLayoutWaiting = (FrameLayout) findViewById(R.id.waiting_view);
		mLayoutReceive = (FrameLayout) findViewById(R.id.records);

		receiveBack = (Button) findViewById(R.id.receive_back);
		receiveFinish=(Button)findViewById(R.id.receive_finish);
		receiveFinish.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mAcceptUsers = new HashSet<String>();
		sdk = ShareWrapper.getInstance();
		sdk.addNetworkStatusListener(mApHostListener);
		sdk.addUserListener(mUserListener);
		// start host when we act as one receiver.
		sdk.startHost(false);

		receiveBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		sdk.stopHost();
		sdk.removeNetworkStatusListener(mApHostListener);
		sdk.removeUserListener(mUserListener);
		super.onDestroy();

	}

	@Override
	protected void onResume() {
		super.onResume();
		sdk.resume();
		sdk.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		sdk.pause();
		sdk.onPause();

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MSG_SHOW_WAITING_VIEW:
				showRecords(false);
				break;
			case MSG_SHOW_RECORDS_VIEW:
				showRecords(true);
				break;
			default:
				break;
			}
		}

	};

	private INetworkStatusListener mApHostListener = new AbstractServerListener() {

		@Override
		public void onServerStatusChanged(boolean enabled) {
			// notify UI when the hotspot's status changed
			Log.d(TAG, "state = " + enabled);
		}
	};

	private IUserListener mUserListener = new IUserListener() {
		@Override
		public void onLocalUserChanged(UserEventType type, UserInfo user) {
		}

		@Override
		public void onRemoteUserChanged(final UserEventType type, final UserInfo user) {
			if (!user.online) {
				mAcceptUsers.remove(user.id);
				if (mAcceptUsers.isEmpty()) {
					// Message msg =
					// mHandler.obtainMessage(MSG_SHOW_WAITING_VIEW);
					// mHandler.sendMessage(msg);
				}
				return;
			}

			// we can show the user's info at UI, let user to accept / refuse
			// the remote user.
			// here, we accept all remote user as one example.
			if (type == UserEventType.ONLINE && user.pending) {
				if (!mAcceptUsers.contains(user.id)) {
					sdk.acceptUser(user.id, true);
					mAcceptUsers.add(user.id);
				}
				Message msg = mHandler.obtainMessage(MSG_SHOW_RECORDS_VIEW);
				mHandler.sendMessage(msg);
			}
		}
	};

	private void showRecords(boolean isShow) {
		if (isShow) {
			mLayoutReceive.setVisibility(View.VISIBLE);
			mLayoutWaiting.setVisibility(View.GONE);
			return;
		}

		mLayoutReceive.setVisibility(View.GONE);
		mLayoutWaiting.setVisibility(View.VISIBLE);
	}
}
