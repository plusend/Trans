package com.plusend.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.lenovo.channel.base.IUserListener;
import com.lenovo.channel.base.UserInfo;
import com.lenovo.sharesdk.ShareWrapper;
import com.plusend.components.receivers.ReceiverFragment;
import com.plusend.components.receivers.ReceiverFragment.OnSelectReceivrListener;
import com.plusend.trans.R;

public class ReceiverPickerActivity extends FragmentActivity {

	FrameLayout mLayoutSearching;
	ListView mReceivers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_receiverpicker);

		ReceiverFragment rf = (ReceiverFragment) Fragment.instantiate(this, ReceiverFragment.class.getName(), null);
		mReceivers = (ListView) findViewById(R.id.list_receiver);
		mLayoutSearching = (FrameLayout) findViewById(R.id.searching_view);
		rf.setOnSelectReceiver(new OnSelectReceivrListener() {
			public void onSelect(String receiver) {
				onSelectedReceiver(receiver);
			}
		});
		FragmentManager fm = this.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.reciever_picker, rf);
		ft.commit();
		ShareWrapper.getInstance().addUserListener(mUserListener);

		Button receivePickerBack = (Button) findViewById(R.id.receiver_picker_back);
		receivePickerBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onPause() {
		this.finish();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void onSelectedReceiver(String receiver) {
		Intent intent = new Intent(ReceiverPickerActivity.this, SendActivity.class);
		startActivity(intent);
	}

	private IUserListener mUserListener = new IUserListener() {

		@Override
		public void onLocalUserChanged(UserEventType type, UserInfo user) {

			Log.e("ReceiverPicker", "onLocalUserChanged");
		}

		@Override
		public void onRemoteUserChanged(UserEventType type, UserInfo user) {

			if (!user.online) {

				Log.e("ReceiverPicker", "onRemoteUserChanged  !OnLINE");

				mReceivers.setVisibility(View.GONE);
				mLayoutSearching.setVisibility(View.VISIBLE);
				return;
			} else {
				mReceivers.setVisibility(View.VISIBLE);
				mLayoutSearching.setVisibility(View.GONE);
			}

			if (type == UserEventType.ONLINE) {

				final String receiver = user.name;
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						onSelectedReceiver(receiver);

						Log.e("ReceiverPicker", "onRemoteUserChanged  OnLINE");
					}
				});

			}
		}

	};

}
