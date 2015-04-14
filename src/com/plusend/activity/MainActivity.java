package com.plusend.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.lenovo.sharesdk.ShareWrapper;
import com.plusend.utils.Debug;
import com.plusend.trans.R;

public class MainActivity extends Activity {

	private TextView username;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		username = (TextView) findViewById(R.id.username);
		username.setText(Build.MODEL);

		int icon = 3;
		ShareWrapper.createInstance(getApplicationContext(), Build.MODEL, icon, true);
		ShareWrapper.getInstance().isSupportAP();
		ShareWrapper.getInstance().setRemoteFileStore(new RemoteFileStore(this));
		
		findViewById(R.id.history).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, HistoryActivity.class));
			}
		});

		findViewById(R.id.main_send).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, SelectActivity.class));
			}
		});

		findViewById(R.id.main_receive).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ReceiveActivity.class));
			}
		});

		findViewById(R.id.share).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ShareActivity.class));
			}
		});

	}

//	IApClientToneListener mToneListener = new IApClientToneListener() {
//		@Override
//		public void onConnecting(String name) {
//		}
//
//	};

	@Override
	protected void onPause() {
		super.onPause();
		ShareWrapper.getInstance().pause();
		ShareWrapper.getInstance().onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ShareWrapper.getInstance().resume();
		ShareWrapper.getInstance().onResume();
	}

	@Override
	protected void onDestroy() {
		ShareWrapper.destroyInstance();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Exit").setMessage("really exit?").setPositiveButton("Exit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				MainActivity.this.finish();
				Debug.w("exit mainActivity");
				Process.killProcess(Process.myPid());
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).create();
		dialog.show();
	}

}
