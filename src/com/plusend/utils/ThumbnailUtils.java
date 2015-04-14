package com.plusend.utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.util.Log;

import com.lenovo.content.base.ContentType;
import com.plusend.components.records.HistoryRecord;
import com.plusend.trans.R;

public class ThumbnailUtils {
	private final static String TAG = "ThumbnailUtils";

	public static Bitmap getPhotoThumbnail(Context ctx, String id) {
		try {
			int mediaId = Integer.parseInt(id);
			String thumbnailPath = getPhotoMiniThumbnailPath(ctx.getContentResolver(), mediaId);
			Bitmap icon = BitmapFactory.decodeFile(thumbnailPath);
			return icon;
		} catch (Throwable t) {
		}
		return null;
	}

	public static Drawable getAppThumbnail(Context ctx, String id) {
		try {
			List<PackageInfo> packageList = ctx.getPackageManager().getInstalledPackages(0);
			for (PackageInfo packageInfo : packageList) {
				if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
					if (packageInfo.packageName.equals(id)) {
						return packageInfo.applicationInfo.loadIcon(ctx.getPackageManager());
					}
				}
			}
		} catch (Throwable t) {
		}
		return null;
	}

	public static Bitmap getAudioThumbnail(Context ctx, String id) {
		try {
			int mediaId = Integer.parseInt(id);

			BitmapMemoryCache cache = BitmapMemoryCache.getInstance();
			final Bitmap bitmap = cache.getBitmap("audio" + mediaId);

			if (bitmap != null) {
				return bitmap;
			} else {
				return null;
			}
		} catch (Throwable t) {
		}
		return null;
	}

	public static Bitmap getVideoThumbnail(Context ctx, String id) {
		try {
			int mediaId = Integer.parseInt(id);

			BitmapMemoryCache cache = BitmapMemoryCache.getInstance();
			final Bitmap bitmap = cache.getBitmap("video" + mediaId);

			if (bitmap != null) {
				return bitmap;
			} else {
				return null;
			}
		} catch (Throwable t) {
		}
		return null;
	}

	static HashMap<String, Drawable> mMimeIconMap;

	public static Drawable getDrawableForMimetype(Context context, String mimetype) {

		if (mimetype == null)
			return context.getResources().getDrawable(R.drawable.item_bg_unknown);

		Drawable icon = null;
		if (mMimeIconMap == null) {
			mMimeIconMap = new HashMap<String, Drawable>();
		}

		icon = mMimeIconMap.get(mimetype);
		if (icon == null) {
			PackageManager pm = context.getPackageManager();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromParts("file", "", null), mimetype);

			List<ResolveInfo> lri = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			List<ResolveInfo> lriRe = pm.queryBroadcastReceivers(intent, PackageManager.MATCH_DEFAULT_ONLY);
			if (lri != null && lri.size() > 0)
				icon = lri.get(0).loadIcon(pm);
			else if (lriRe != null && lriRe.size() > 0)
				icon = lriRe.get(0).loadIcon(pm);

			if (icon != null)
				mMimeIconMap.put(mimetype, icon);
			else {
				icon = context.getResources().getDrawable(R.drawable.item_bg_unknown);
				mMimeIconMap.put(mimetype, icon);
			}
		}

		return icon;
	}

	public static String getPhotoMiniThumbnailPath(ContentResolver cr, int imageid) {
		String[] projection = { MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.DATA };
		String thumbnailPath = null;

		Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(cr, imageid, MediaStore.Images.Thumbnails.MINI_KIND, projection);
		try {
			if (cursor != null && cursor.moveToFirst()) {
				int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
				thumbnailPath = cursor.getString(dataIndex);
			}
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (Exception e) {
					Log.w(TAG, "close cursor failed, " + e.getMessage());
				}
			}
		}

		return thumbnailPath;
	}

	public static Bitmap getThumbnail(String imagepath, int width, int height) {
		Bitmap source = BitmapFactory.decodeFile(imagepath);
		if ((source != null) && ((source.getWidth() > width || source.getHeight() > height)))
			return android.media.ThumbnailUtils.extractThumbnail(source, width, width, android.media.ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return source;
	}

	public static Bitmap getThumbnail(HistoryRecord history, Context ctx) {

		if (history.getType().equals(ContentType.CONTACT.toString())) {
			return BitmapFactory.decodeResource(ctx.getResources(), R.drawable.item_bg_contact);
		} else if (history.getType().equals(ContentType.APP.toString())) {
			Drawable drawable = getAppThumbnail(ctx, history.getId());
			int w = drawable.getIntrinsicWidth();
			int h = drawable.getIntrinsicHeight();
			Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
			Bitmap bitmap = Bitmap.createBitmap(w, h, config);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, w, h);
			drawable.draw(canvas);
			return bitmap;
		} else if (history.getType().equals(ContentType.PHOTO.toString())) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(ctx.getContentResolver(), Long.parseLong(history.getId()), Images.Thumbnails.MICRO_KIND, options);
			return bitmap;
		} else if (history.getType().equals(ContentType.VIDEO.toString())) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(ctx.getContentResolver(), Long.parseLong(history.getId()), Video.Thumbnails.MICRO_KIND, options);
			return bitmap;
		} else if (history.getType().equals(ContentType.MUSIC.toString())) {
			String str = "content://media/external/audio/media/" + history.getId() + "/albumart";
			Uri uri2 = Uri.parse(str);
			ParcelFileDescriptor pfd = null;
			Bitmap bitmap = null;
			try {
				pfd = ctx.getContentResolver().openFileDescriptor(uri2, "r");
			} catch (FileNotFoundException e) {
			}
			if (pfd != null) {
				FileDescriptor fd = pfd.getFileDescriptor();
				bitmap = BitmapFactory.decodeFileDescriptor(fd);
			}
			if (bitmap == null) {
				BitmapDrawable draw = (BitmapDrawable) ctx.getResources().getDrawable(R.drawable.music_icon);
				bitmap = draw.getBitmap();
			}
			return bitmap;
		}else if(history.getType().equals(ContentType.FILE)){
			return BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ex_doc);
		}
		return null;
	}
}
