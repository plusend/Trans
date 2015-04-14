package com.plusend.controller;

import java.util.ArrayList;
import java.util.List;

import android.util.Pair;

import com.lenovo.content.base.ContentType;

public class TransController {
	private static TransController instance;
	private List<Pair<ContentType, String>> selectedItems;

	private TransController() {
		selectedItems = new ArrayList<Pair<ContentType, String>>();
	}

	public static TransController getInstance() {
		synchronized (TransController.class) {
			if (instance == null) {
				instance = new TransController();
			}
		}
		return instance;
	}

	public List<Pair<ContentType, String>> getSelectedItems() {
		return selectedItems;
	}

}
