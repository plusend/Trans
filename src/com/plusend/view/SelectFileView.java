package com.plusend.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lenovo.content.base.ContentType;
import com.plusend.trans.R;

public class SelectFileView extends PagerView {

	private FileAdapter mFileAdapter = null;
	private List<FileInfo> fileInfoList;

	public SelectFileView(Context context) {
		super(context);
		title = "File";
		commonContentType = ContentType.FILE;

		fileInfoList = new ArrayList<SelectFileView.FileInfo>();

		View view = View.inflate(context, R.layout.select_file_view, null);
		addView(view);
		ListView fileListView = (ListView) view.findViewById(R.id.filelistview);
		String mDir = Environment.getExternalStorageDirectory().toString();
		generateFileInfo(mDir);

		fileListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
				ViewHolder holder = ((ViewHolder) view.getTag());
				FileInfo info = fileInfoList.get(position);

				if (info.fileType == FileInfo.FOLDER) {
					generateFileInfo(info.item.second);
					mFileAdapter.notifyDataSetChanged();
				} else {
					boolean contains = false;
					int index = -1;
					for (int i = getSelectedItems().size() - 1; i >= 0; i--) {
						Pair<ContentType, String> pair = getSelectedItems().get(i);
						if (pair.equals(info.item)) {
							contains = true;
							index = i;
							break;
						}
					}
					if (contains) {
						getSelectedItems().remove(index);
						holder.fileCheckBox.setChecked(false);
					} else {
						getSelectedItems().add(info.item);
						holder.fileCheckBox.setChecked(true);
					}
					notifyItemChanged();
				}
			}

		});
		mFileAdapter = new FileAdapter();
		fileListView.setAdapter(mFileAdapter);
	}

	@Override
	public void fillSelectedItem(Pair<ContentType, String> item, TextView title, ImageView image) {
		File file = new File(item.second);
		title.setText(file.getName());
		image.setImageResource(file.isDirectory() ? R.drawable.ex_folder : R.drawable.ex_doc);
	}

	@Override
	public void loadDataAsync() {

	}

	private class ViewHolder {
		public ImageView img;
		public TextView title;
		public TextView path;
		public CheckBox fileCheckBox;
	}

	private class FileAdapter extends BaseAdapter {

		public int getCount() {
			return fileInfoList.size();
		}

		public Object getItem(int position) {
			return fileInfoList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(SelectFileView.this.getContext(), R.layout.file_item, null);
				holder.img = (ImageView) convertView.findViewById(R.id.fileImage);
				holder.title = (TextView) convertView.findViewById(R.id.fileTitle);
				holder.path = (TextView) convertView.findViewById(R.id.fileInfo);
				holder.fileCheckBox = (CheckBox) convertView.findViewById(R.id.fileCheckBox);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			FileInfo info = fileInfoList.get(position);

			holder.img.setImageResource(info.fileType == FileInfo.FILE ? R.drawable.ex_doc : R.drawable.ex_folder);
			holder.title.setText(info.name);
			holder.path.setText(info.item.second);

			if (info.fileType == FileInfo.FOLDER) {
				holder.fileCheckBox.setVisibility(View.GONE);
			} else {
				holder.fileCheckBox.setVisibility(View.VISIBLE);
				holder.fileCheckBox.setChecked(getSelectedItems().contains(info.item));
			}
			return convertView;
		}
	}

	private void generateFileInfo(String dirPath) {
		fileInfoList.clear();
		File dirFile = new File(dirPath);
		File[] files = dirFile.listFiles();
		if (!dirPath.equals(Environment.getExternalStorageDirectory().toString())) {
			FileInfo info = new FileInfo();
			info.name = "Back to ../";
			info.item = new Pair<ContentType, String>(commonContentType, dirFile.getParent());
			info.fileType = FileInfo.FOLDER;
			fileInfoList.add(info);
		}
		if (files != null) {
			for (File file : files) {

				FileInfo info = new FileInfo();
				info.name = file.getName();
				info.item = new Pair<ContentType, String>(commonContentType, file.getAbsolutePath());
				info.fileType = file.isDirectory() ? FileInfo.FOLDER : FileInfo.FILE;
				fileInfoList.add(info);
			}
		}
	}

	private static class FileInfo {
		private static final int FILE = 0;
		private static final int FOLDER = 1;
		private int fileType;
		private String name;
		private Pair<ContentType, String> item;

	}

	@Override
	public void onUpdate() {
		mFileAdapter.notifyDataSetChanged();
	}

}
