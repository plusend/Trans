//ReceiveActivity mLayoutReceive

package com.plusend.components.records;

import java.util.Collection;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.lenovo.channel.base.IShareReceiveListener;
import com.lenovo.channel.base.IShareSendListener;
import com.lenovo.channel.base.ShareRecord;
import com.lenovo.channel.exception.TransmitException;
import com.lenovo.sharesdk.ShareWrapper;
import com.plusend.trans.R;

public class RecordFragment extends Fragment {
    private static final String TAG = "RecordFragment";
    private static final int MSG_UPDATE_UI = 0x01;

    private ListView mRecords;

    private RecordAdapter mAdapter;
    private ShareWrapper sdk;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_records, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new RecordAdapter(this.getActivity());
        mRecords = (ListView)view.findViewById(R.id.list_record);
        mRecords.setAdapter(mAdapter);

        sdk = ShareWrapper.getInstance();
        sdk.addReceiveListener(mDownloadListener);
        sdk.addSendListener(mSendCallback);
    }

    @Override
    public void onDestroy() {
        sdk.removeReceiveListener(mDownloadListener);
        sdk.removeSendListener(mSendCallback);
        getActivity().finish();
        super.onDestroy();
    }

    private IShareReceiveListener mDownloadListener = new IShareReceiveListener() {

        public void onReceived(Collection<ShareRecord> records) {
            // onReceived will be called when receive the items.
            RecordFragment.this.onSendOrReceived(records);
        }

        public void onProgress(ShareRecord record, long total, long completed) {
         // onProgress will be called when progress is changed.
            RecordFragment.this.onProgress(record, total, completed);
        }

        public void onResult(ShareRecord record, boolean succeeded, TransmitException error, boolean isThumbnail) {
         // onResult will be called when the file or thumbnail is completed or failed
            RecordFragment.this.onResult(record, succeeded, error, isThumbnail);
        }
    };

    private IShareSendListener mSendCallback = new IShareSendListener() {

        public void onSent(Collection<ShareRecord> records) {
         // onReceived will be called when send items.
            RecordFragment.this.onSendOrReceived(records);
        }

        public void onStarted(ShareRecord record, long total) {}

        public void onProgress(ShareRecord record, long total, long completed) {
         // onProgress will be called when progress is changed.
            RecordFragment.this.onProgress(record, total, completed);
        }

        public void onResult(ShareRecord record, boolean succeeded, TransmitException error) {
         // onResult will be called when the file or thumbnail is completed or failed
            RecordFragment.this.onResult(record, succeeded, error, false);
        }
    };

    private void onSendOrReceived(Collection<ShareRecord> records) {
        Log.d(TAG, "on send / receive something");

        for (ShareRecord sr : records) {
            if (mAdapter.getItem(sr) == null) {
                mAdapter.addItem(0, new ShareItem(sr));
                mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_UI));
            }
        }
    }

    private void onProgress(ShareRecord record, long total, long completed) {
        assert (record != null);

        ShareItem si = mAdapter.getItem(record);
        if (si == null)
            return;

        si.onProgress(total, completed);
    }

    private void onResult(ShareRecord record, boolean succeeded, TransmitException error, boolean isThumbnail) {
        ShareItem si = mAdapter.getItem(record);

        if (succeeded)
            si.onComplete(isThumbnail);
        else
            si.onError(isThumbnail, error);
    }

    @SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_UI:
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }

    };
}
