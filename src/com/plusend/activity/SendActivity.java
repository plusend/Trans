package com.plusend.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.lenovo.channel.base.IUserListener;
import com.lenovo.channel.base.ShareRecord.CollectionShareRecord;
import com.lenovo.channel.base.UserInfo;
import com.lenovo.content.base.ContentType;
import com.lenovo.sharesdk.ShareWrapper;
import com.plusend.controller.TransController;
import com.plusend.trans.R;

public class SendActivity extends FragmentActivity {

	public static final String INTENT_KEY_SELECT_ITEMS = "select_items";

	private ShareWrapper sdk;

	private Button sendBack, sendContinue, sendFinish;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_send);
		sdk = ShareWrapper.getInstance();
		sdk.addUserListener(mUserListener);
		sendContinue = (Button) findViewById(R.id.send_continue);
		sendContinue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SendActivity.this, SelectActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				TransController.getInstance().getSelectedItems().clear();
				startActivity(intent);
			}
		});
		sendFinish = (Button) findViewById(R.id.send_finish);
		sendFinish.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SendActivity.this, MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		sendBack = (Button) findViewById(R.id.send_back);
		sendBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SendActivity.this, SelectActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				TransController.getInstance().getSelectedItems().clear();
				startActivity(intent);
			}
		});

	}

	private void handleIntent(Context context) {
		Intent intent = getIntent();
		assert intent != null;
		boolean isCollection = intent.getBooleanExtra("is_collection", false);
		if (isCollection) {
			sdk.sendCollection(getSelectedCollection(intent.getData()), null);
		} else {
			sdk.sendItems(ShareWrapper.getInstance().createShareRecords(TransController.getInstance().getSelectedItems()), null);
		}
	}

	@Override
	protected void onDestroy() {
		sdk.removeUserListener(mUserListener);
		// stop client, because we exit and will act as none.
		sdk.stopClient();
		super.onDestroy();
	}

	private IUserListener mUserListener = new IUserListener() {
		@Override
		public void onLocalUserChanged(UserEventType type, UserInfo user) {
		}

		@Override
		public void onRemoteUserChanged(final UserEventType type, final UserInfo user) {
			if (!user.online) {
				return;
			}
			if (type == UserEventType.ONLINE) {
				// NOTICE: send items after remote user is online.
				handleIntent(SendActivity.this);
			}
		}
	};

	private CollectionShareRecord getSelectedCollection(Uri uri) {
		String path = uri.getPath();
		CollectionShareRecord collectionRecord = ShareWrapper.getInstance().createCollectionShareRecord(ContentType.FILE, path);
		return collectionRecord;
	}

}
