package com.plusend.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lenovo.content.base.ContentType;
import com.plusend.utils.PingYinUtil;
import com.plusend.trans.R;

public class SelectContactView extends PagerView {

	LinearLayout layoutIndex;

	private ArrayList<String> data = new ArrayList<String>();
	private ListView listView;
	MyAdapter adapter;
	private Context context;

	private ArrayList<ContactInfo> contactList = new ArrayList<ContactInfo>();

	public SelectContactView(Context context) {
		super(context);
		title = "Contact";
		commonContentType = ContentType.CONTACT;
		this.context = context;
		View view = View.inflate(context, R.layout.select_contact_view, null);
		addView(view);

		listView = (ListView) findViewById(R.id.contactlistview);
		adapter = new MyAdapter();
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
				CheckBox checkBox = (CheckBox) v.findViewById(R.id.contactCheckBox);
				Pair<ContentType, String> item = null;
				for (ContactInfo info : contactList) {
					if ((info.contactName + "\r\n" + info.contactPhoneNum).equals(data.get(position))) {
						item = info.item;
						break;
					}
				}
				if (getSelectedItems().contains(item)) {
					getSelectedItems().remove(item);
					checkBox.setChecked(false);
				} else {
					getSelectedItems().add(item);
					checkBox.setChecked(true);
				}
				notifyItemChanged();
			}
		});
	}

	/** 获取排序后的新数据 */
	public void sortIndex() {
		TreeSet<String> set = new TreeSet<String>();
		for (String string : data) {
			set.add(String.valueOf(PingYinUtil.getPingYin(string).charAt(0)));
		}
		for (String string : set) {
			data.add(string);
		}
		Collections.sort(data, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {

				String str1 = PingYinUtil.getPingYin(o1);
				String str2 = PingYinUtil.getPingYin(o2);
				return str1.compareTo(str2);
			}

		});
	}

	/** 适配器 */
	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public boolean isEnabled(int position) {
			if (data.get(position).length() == 1)// 如果是字母索引
				return false;// 表示不能点击
			return super.isEnabled(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String text = data.get(position);

			if (text.length() == 1) {
				convertView = View.inflate(context, R.layout.index, null);
				TextView tv = (TextView) convertView.findViewById(R.id.textView1);
				tv.setText(text);
			} else {
				convertView = View.inflate(context, R.layout.contact_item, null);
				TextView tv = (TextView) convertView.findViewById(R.id.textView1);
				tv.setText(text);
				CheckBox contactCheck = (CheckBox) convertView.findViewById(R.id.contactCheckBox);

				Pair<ContentType, String> item = null;
				for (ContactInfo info : contactList) {
					if ((info.contactName + "\r\n" + info.contactPhoneNum).equals(data.get(position))) {
						item = info.item;
						break;
					}
				}
				contactCheck.setChecked(getSelectedItems().contains(item));
			}

			return convertView;
		}
	}

	@Override
	public void loadDataAsync() {
		Cursor contact_cursor = getContext().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (contact_cursor.moveToFirst()) {
			do {
				ContactInfo contactInfo = new ContactInfo();
				contactInfo.item = new Pair<ContentType, String>(commonContentType, contact_cursor.getString(contact_cursor.getColumnIndex(ContactsContract.Contacts._ID)));
				contactInfo.contactName = contact_cursor.getString(contact_cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				int phoneCount = contact_cursor.getInt(contact_cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				if (phoneCount > 0) {
					Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactInfo.item.second, null, null);
					if (phones.moveToFirst()) {
						contactInfo.contactPhoneNum = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					}
					phones.close();
				}
				contactList.add(contactInfo);
			} while (contact_cursor.moveToNext());
		}
		contact_cursor.close();

		for (ContactInfo info : contactList) {
			data.add(info.contactName + "\r\n" + info.contactPhoneNum);
		}

		sortIndex();

		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				adapter.notifyDataSetChanged();
				SelectContactView.this.findViewById(R.id.select_contact_view_loading).setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			}
		});
	}

	@Override
	public void onUpdate() {
		adapter.notifyDataSetChanged();
	}

	public static class ContactInfo {
		private String contactName;
		private String contactPhoneNum;
		private Pair<ContentType, String> item;
	}

	@Override
	public void fillSelectedItem(Pair<ContentType, String> item, TextView title, ImageView image) {
		for (ContactInfo info : contactList) {
			if (info.item.equals(item)) {
				title.setText(info.contactName + "\r\n" + info.contactPhoneNum);
				image.setImageResource(R.drawable.item_bg_contact);
				break;
			}
		}
	}
}
