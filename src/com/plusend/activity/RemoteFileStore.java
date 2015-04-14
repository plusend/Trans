package com.plusend.activity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;
import org.apache.http.protocol.HTTP;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import com.lenovo.channel.base.IRemoteFileStore;
import com.lenovo.content.base.ContentType;
import com.plusend.utils.FileUtils;

public class RemoteFileStore implements IRemoteFileStore {

	private static final String DIR_EXTERNAL_APP_ROOT = "SevengaTrans/";
	private static final String DIR_EXTERNAL_TEMP = ".tmp/";
	private static final String DIR_EXTERNAL_THUMBNAIL = ".thumbnails/";
	private static final String DIR_EXTERNAL_APP = "apps/";
	private static final String DIR_EXTERNAL_CONTACT = "contacts/";
	private static final String DIR_EXTERNAL_MUSIC = "audios/";
	private static final String DIR_EXTERNAL_VIDEO = "videos/";
	private static final String DIR_EXTERNAL_PICTURE = "pictures/";
	private static final String DIR_EXTERNAL_FILE = "files/";

	private File mSdcardRootDir;
	private File mExternalAppRootDir;

	private File mExternalTempDir;
	private File mExternalThumbnailDir;

	public RemoteFileStore(Context context) {
		initAppDirs();
	}

	@Override
	public File getExternalStorage() {
		mSdcardRootDir = Environment.getExternalStorageDirectory();
		return mSdcardRootDir;
	}

	@Override
	public File getExternalTempDir() {
		if (!mExternalTempDir.exists())
			mExternalTempDir.mkdir();
		return mExternalTempDir;
	}

	@Override
	public File createTempFileName(String suggestedFileName) {
		String tempFileName = suggestedFileName;
		if (TextUtils.isEmpty(tempFileName))
			tempFileName = UUID.randomUUID().toString() + ".tmp";
		return new File(getExternalTempDir(), tempFileName);
	}

	@Override
	public File getRemoteItemDir(ContentType type, String fileName) {
		return getRemoteItemDir(type, null, fileName);
	}

	@Override
	public File getRemoteItemDir(ContentType type, String parentPath, String fileName) {
		String subDir = null;
		switch (type) {
		case PHOTO:
			subDir = DIR_EXTERNAL_PICTURE;
			break;
		case APP:
			subDir = DIR_EXTERNAL_APP;
			break;
		case MUSIC:
			subDir = DIR_EXTERNAL_MUSIC;
			break;
		case VIDEO:
			subDir = DIR_EXTERNAL_VIDEO;
			break;
		case CONTACT:
			subDir = DIR_EXTERNAL_CONTACT;
			break;
		case FILE:
			subDir = DIR_EXTERNAL_FILE;
			break;
		default:
			break;
		}
		return new File(mExternalAppRootDir, subDir);
	}

	@Override
	public File getThumbnailDir() {
		if (!mExternalThumbnailDir.exists())
			mExternalThumbnailDir.mkdir();
		return mExternalThumbnailDir;
	}

	@Override
	public File getRemoteItemThumbnail(String deviceId, ContentType itemType, String itemId) {
		String fileName;
		try {
			fileName = deviceId + "_" + URLEncoder.encode(itemId, HTTP.UTF_8) + "_" + itemType.name();
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		return new File(getThumbnailDir(), fileName);
	}

	private void gc() {
		FileUtils.removeFolderDescents(getExternalTempDir());
	}

	private void initAppDirs() {
		mExternalAppRootDir = new File(getExternalStorage(), DIR_EXTERNAL_APP_ROOT);
		if (!mExternalAppRootDir.exists())
			mExternalAppRootDir.mkdirs();

		{
			mExternalTempDir = new File(mExternalAppRootDir, DIR_EXTERNAL_TEMP);
			if (!mExternalTempDir.exists())
				mExternalTempDir.mkdirs();

			try {
				File noMedia = new File(mExternalThumbnailDir, ".nomedia");
				if (!noMedia.exists())
					noMedia.createNewFile();
			} catch (IOException e) {
			}
		}

		{
			mExternalThumbnailDir = new File(mExternalAppRootDir, DIR_EXTERNAL_THUMBNAIL);
			if (!mExternalThumbnailDir.exists())
				mExternalThumbnailDir.mkdirs();

			try {
				File noMedia = new File(mExternalThumbnailDir, ".nomedia");
				if (!noMedia.exists())
					noMedia.createNewFile();
			} catch (IOException e) {
			}
		}

		{
			File subDir = new File(mExternalAppRootDir, DIR_EXTERNAL_APP);
			if (!subDir.exists())
				subDir.mkdirs();
		}

		{
			File subDir = new File(mExternalAppRootDir, DIR_EXTERNAL_PICTURE);
			if (!subDir.exists())
				subDir.mkdirs();
		}

		{
			File subDir = new File(mExternalAppRootDir, DIR_EXTERNAL_MUSIC);
			if (!subDir.exists())
				subDir.mkdirs();
		}

		{
			File subDir = new File(mExternalAppRootDir, DIR_EXTERNAL_VIDEO);
			if (!subDir.exists())
				subDir.mkdirs();
		}

		{
			File subDir = new File(mExternalAppRootDir, DIR_EXTERNAL_FILE);
			if (!subDir.exists())
				subDir.mkdirs();
		}

		{
			File subDir = new File(mExternalAppRootDir, DIR_EXTERNAL_CONTACT);
			if (!subDir.exists())
				subDir.mkdirs();
		}

		gc();
	}

}
