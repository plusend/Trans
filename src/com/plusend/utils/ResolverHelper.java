package com.plusend.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ResolverHelper {
	public static String queryItemId(Context context, Uri uri, String[] project) {
		String ret = null;
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = null;

		try {
			cursor = contentResolver.query(uri, project, null, null, null);
			if (cursor != null) {
				if (cursor.moveToFirst())
					ret = cursor.getString(0);
			}
		} catch (Exception e) {
		} finally {
			try {
				if (cursor != null)
					cursor.close();
			} catch (Exception e) {
			}
		}
		return ret;
	}
}
