package com.plusend.activity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lenovo.invitesdk.InviteManager;
import com.lenovo.invitesdk.InviteManager.ZeroTrafficListener;
import com.plusend.trans.R;

public class ShareActivity extends Activity {

	private static final int MSG_TOAST_ZERO_START = 1;
	private static final int MSG_TOAST_ZERO_STOP = 2;
	private static final int MSG_TOAST_ZERO_READY = 3;

	private static final int REQ_CODE_TURNON_BLUETOOTH = 1;
	private static final int REQ_CODE_SEND_FILE = 2;

	private boolean mZeroOpened = false;

	private Button shareBack = null;
	
	String strs = "Tips: The phone will establish a mobile phone hotspot, Other phones can connect to the hotspot to download the Sevenga Trans and free flow. By this way, the recipient should enter and open the url '192.168.1.1:2999' in a browser after connecting hotspot";

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Button button = (Button) findViewById(R.id.button_by_zero);
			TextView zeroflow = (TextView)findViewById(R.id.zerosharetextView);
			switch (msg.what) {
			case MSG_TOAST_ZERO_START:
				//Toast.makeText(ShareActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
				button.setText("Close zero");
				zeroflow.setText((String)msg.obj);
				break;
			case MSG_TOAST_ZERO_STOP:
				Toast.makeText(ShareActivity.this, "zero traffic stoped!", Toast.LENGTH_SHORT).show();
				button.setText("Open zero");
				zeroflow.setText("zero flow url");
				break;
			case MSG_TOAST_ZERO_READY:
				Toast.makeText(ShareActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_quick_share);
		Button button = (Button) findViewById(R.id.button_by_zero);
		button.setOnClickListener(mBtnClickListener);
		if (!InviteManager.canShareByZeroTraffic())
			button.setClickable(false);
		button = (Button) findViewById(R.id.button_by_bluetooth);
		button.setOnClickListener(mBtnClickListener);
		if (!InviteManager.canShareByBluetooth(this))
			button.setClickable(false);
		
		InviteManager.init(this);

		shareBack = (Button)findViewById(R.id.shareback);
		shareBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void openZeroTraffic() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
			InviteManager.startZeroTraffic(this, new File(info.applicationInfo.sourceDir), "Sevenga Trans.apk", new ZeroTrafficListener() {
				@Override
				public void informServerInfo(String ssidPrefix, String url) {
					mZeroOpened = true;
					String toast = "zero traffic started! prefix:" + ssidPrefix + " url:" + url;
					Message msg = handler.obtainMessage(MSG_TOAST_ZERO_START, toast);
					handler.sendMessage(msg);
				}

				@Override
				public void serverStarted() {
					String toast = "server is ready!";
					Message msg = handler.obtainMessage(MSG_TOAST_ZERO_READY, toast);
					handler.sendMessage(msg);
				}

				@Override
				public void serverStopped() {
					mZeroOpened = false;
					handler.sendEmptyMessage(MSG_TOAST_ZERO_STOP);
				}

			});
		} catch (NameNotFoundException e) {
		} catch (Exception e) {
		}
	}

	private void closeZeroTraffic() {
		mZeroOpened = false;
		InviteManager.stopZeroTraffic();
	}

	private void sendFile() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
			InviteManager.shareByBluetooth(this, info.applicationInfo.sourceDir, REQ_CODE_SEND_FILE);
		} catch (Exception e) {
		}
		;
	}

	private void shareByBluetooth() {
		if (!InviteManager.assureBluetoothEnabled())
			InviteManager.turnOnBluetoothEnabled(this, REQ_CODE_TURNON_BLUETOOTH);
		else
			sendFile();
	}

	private OnClickListener mBtnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.button_by_zero:
				if (mZeroOpened)
					closeZeroTraffic();
				else {
					openZeroTraffic();
				}

				break;
			case R.id.button_by_bluetooth:
				shareByBluetooth();
				break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mZeroOpened)
			closeZeroTraffic();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQ_CODE_TURNON_BLUETOOTH:
			if (resultCode == RESULT_CANCELED)
				return;
			sendFile();
			break;
		case REQ_CODE_SEND_FILE:
			break;
		default:
			break;
		}
	}

}
