package com.plusend.view;

import java.util.List;

import android.content.Context;
import android.util.Pair;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.lenovo.content.base.ContentType;
import com.plusend.activity.SelectActivity;

public abstract class PagerView extends FrameLayout {
	protected String title;
	protected ContentType commonContentType;

	public PagerView(Context context) {
		super(context);
	}

	public abstract void loadDataAsync();

	public abstract void onUpdate();

	protected void notifyItemChanged() {
		((SelectActivity) getContext()).updateSelectedButton();
	}

	protected void addItem(Pair<ContentType, String> item) {

	}

	public String getTitle() {
		return title;
	}

	public ContentType getCommonContentType() {
		return commonContentType;
	}

	public abstract void fillSelectedItem(Pair<ContentType, String> item, TextView title, ImageView image);

	protected List<Pair<ContentType, String>> getSelectedItems() {
		return ((SelectActivity) getContext()).getSelectedItems();
	}
}
