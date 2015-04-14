package com.plusend.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lenovo.content.base.ContentType;
import com.lenovo.network.base.IApClientToneListener;
import com.lenovo.sharesdk.ShareWrapper;
import com.plusend.controller.TransController;
import com.plusend.view.PagerView;
import com.plusend.view.SelectAppView;
import com.plusend.view.SelectContactView;
import com.plusend.view.SelectFileView;
import com.plusend.view.SelectMusicView;
import com.plusend.view.SelectPictureView;
import com.plusend.view.SelectVideoView;
import com.plusend.trans.R;

public class SelectActivity extends Activity {
	private ViewPager viewPager;
	private List<PagerView> pagerViews;

	private Button selectSelected, sendButton, selectBackButton;
	private AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sendselect);

		selectBackButton = (Button) findViewById(R.id.sendselect_back);
		sendButton = (Button) findViewById(R.id.selectSend);
		selectSelected = (Button) findViewById(R.id.selectSelected);
		PagerTabStrip tab = (PagerTabStrip) findViewById(R.id.select_activity_tab);

		tab.setBackgroundColor(Color.WHITE);
		tab.setTextColor(Color.BLACK);
		tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

		InitViewPager();

		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getSelectedItems().size() == 0) {
					Toast.makeText(getApplicationContext(), "please select first", Toast.LENGTH_SHORT).show();
					return;
				}
				ShareWrapper.getInstance().addClientToneListener(mToneListener);
				ShareWrapper.getInstance().startClient(false);
				Intent intent = new Intent(SelectActivity.this, ReceiverPickerActivity.class);
				intent.putExtra("is_collection", false);
				startActivity(intent);
			}
		});

		selectSelected.setText("Selected(" + getSelectedItems().size() + ")");
		selectSelected.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (getSelectedItems().size() == 0) {
					Toast.makeText(getApplicationContext(), "please select first", Toast.LENGTH_SHORT).show();
					return;
				}
				actionAlertDialog();
			}
		});

		selectBackButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void updateSelectedButton() {
		selectSelected.post(new Runnable() {
			@Override
			public void run() {
				selectSelected.setText("Selected(" + getSelectedItems().size() + ")");
			}
		});
	}

	@Override
	public void finish() {
		getSelectedItems().clear();
		super.finish();
	}

	public List<Pair<ContentType, String>> getSelectedItems() {
		return TransController.getInstance().getSelectedItems();
	}

	private void InitViewPager() {
		viewPager = (ViewPager) findViewById(R.id.vPager);
		pagerViews = new ArrayList<PagerView>();

		pagerViews.add(new SelectContactView(this));
		pagerViews.add(new SelectAppView(this));
		pagerViews.add(new SelectPictureView(this));
		pagerViews.add(new SelectMusicView(this));
		pagerViews.add(new SelectVideoView(this));
		pagerViews.add(new SelectFileView(this));

		for (final PagerView view : pagerViews) {
			new Thread() {
				public void run() {
					view.loadDataAsync();
				};
			}.start();
		}
		viewPager.setAdapter(new MyViewPagerAdapter());
		viewPager.setCurrentItem(1);
	}

	public class MyViewPagerAdapter extends PagerAdapter {

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(pagerViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(pagerViews.get(position), 0);
			return pagerViews.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return pagerViews.get(position).getTitle();
		}

		@Override
		public int getCount() {
			return pagerViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	IApClientToneListener mToneListener = new IApClientToneListener() {
		@Override
		public void onConnecting(String name) {
		}

	};

	public class DialogAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return getSelectedItems().size();
		}

		@Override
		public Object getItem(int position) {
			return getSelectedItems().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(SelectActivity.this, R.layout.dialog_selected_item, null);
				holder.image = (ImageView) convertView.findViewById(R.id.dialog_selected_item_image);
				holder.title = (TextView) convertView.findViewById(R.id.dialog_selected_item_title);
				holder.delete = (Button) convertView.findViewById(R.id.dialog_selected_item_delete);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Pair<ContentType, String> item = getSelectedItems().get(position);
			for (PagerView view : pagerViews) {
				if (item.first == view.getCommonContentType()) {
					view.fillSelectedItem(item, holder.title, holder.image);
				}
			}

			holder.delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getSelectedItems().remove(position);
					notifyDataSetChanged();
					if (getSelectedItems().size() == 0) {
						alertDialog.dismiss();
					}
					for (PagerView view : pagerViews) {
						view.onUpdate();
					}
					updateSelectedButton();
				}
			});

			return convertView;
		}

	}

	private static class ViewHolder {
		private ImageView image;
		private TextView title;
		private Button delete;
	}

	protected void actionAlertDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_selected, (ViewGroup) findViewById(R.id.dialog_select));
		ListView dialogListView = (ListView) layout.findViewById(R.id.dialog_select_listview);
		Button clear = (Button) layout.findViewById(R.id.dialog_select_clear);

		final DialogAdapter adapter = new DialogAdapter();
		dialogListView.setAdapter(adapter);
		builder.setView(layout);
		alertDialog = builder.create();
		alertDialog.show();
		clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				getSelectedItems().clear();
				adapter.notifyDataSetChanged();
				alertDialog.dismiss();
				for (PagerView view : pagerViews) {
					view.onUpdate();
				}
				updateSelectedButton();
			}
		});

	}
}
