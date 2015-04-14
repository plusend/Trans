//RecordAdapter  mShareItems

package com.plusend.components.records;

import java.lang.ref.SoftReference;
import java.util.Observable;

import android.graphics.drawable.Drawable;

import com.lenovo.channel.base.ShareRecord;
import com.lenovo.channel.exception.TransmitException;
import com.lenovo.content.base.ContentItem;
import com.lenovo.content.base.ContentProperties;
import com.lenovo.content.base.ContentProperties.AppProps;
import com.lenovo.content.base.ContentProperties.ContactProps;
import com.lenovo.content.base.ContentProperties.FileProps;
import com.lenovo.content.base.ContentProperties.ItemProps;
import com.lenovo.content.base.ContentProperties.MediaProps;
import com.lenovo.content.base.ContentType;
import com.lenovo.content.item.AppItem;
import com.lenovo.content.item.ContactItem;
import com.lenovo.content.item.FileItem;
import com.lenovo.content.item.MusicItem;
import com.lenovo.content.item.PhotoItem;
import com.lenovo.content.item.VideoItem;

public class ShareItem extends Observable {
    public enum EventElement {
        THUMBNAIL, PROGRESS, COMPLETE, ERROR
    }

    protected long mCompletedLength = 0;
    private TransmitException mError = null;
    private SoftReference<Drawable> mThumbnail = null;

    private ShareRecord mRecord;

    public ShareItem(ShareRecord record) {
        mRecord = record;
    }

    public ShareRecord getRecord() {
        return mRecord;
    }

    public Drawable getThumbnail() {
        if (mThumbnail == null)
            return null;

        // here, maybe return null, because the gc free it.
        return mThumbnail.get();
    }

    public void setThumbnail(Drawable d) {
        assert d != null;

        mThumbnail = new SoftReference<Drawable>(d);
        d = null;
    }

    public TransmitException getError() {
        return mError;
    }

    public void onComplete(boolean isThumbnail) {
        setChanged();
        notifyObservers(isThumbnail ? EventElement.THUMBNAIL : EventElement.COMPLETE);
    }

    public void onError(boolean isThumbnail, TransmitException error) {
        if (isThumbnail)
            return;
        mError = error;
        setChanged();
        notifyObservers(EventElement.ERROR);
    }

    public void onProgress(long total, long completed) {
        mCompletedLength = completed;
        setChanged();
        notifyObservers(EventElement.PROGRESS);
    }
    
    /**
     * Example method that Convert contentItem to child item like appitem, photoitem 
     * 
     * @param record
     * @return content Item
     */
    static ContentItem getContentItem(ShareRecord record) {
        ContentItem item = record.getItem();
        
        switch(item.getContentType()) {
            case APP:
                return (AppItem)item;
            case CONTACT:
                return (ContactItem)item;
            case FILE:
                return (FileItem)item;
            case MUSIC:
                return (MusicItem)item;
            case PHOTO:
                return (PhotoItem)item;
            case VIDEO:
                return (VideoItem)item;
		default:
			break;
        }
        return item;
    }
    
    /**
     * Example method that construct content item by properties
     * @param itemType
     * @return
     */
    static ContentItem createContentItem(ContentType itemType) {
        String sValue = "";// string value
        boolean bValue = false; // boolean value
        long lValue = 0; // long value
        int iValue = 0; // int value
        
        ContentProperties props = new ContentProperties();
        props.add(ItemProps.KEY_ID, sValue);
        props.add(ItemProps.KEY_VER, sValue);

        props.add(ItemProps.KEY_NAME, sValue);
        props.add(ItemProps.KEY_IS_EXIST, bValue);
        props.add(ItemProps.KEY_FILE_PATH, sValue);
        props.add(ItemProps.KEY_FILE_SIZE, lValue);

        switch (itemType) {
            case APP:
                props.add(AppProps.KEY_PACKAGE_NAME, sValue);
                props.add(AppProps.KEY_VERSION_NAME, sValue);
                props.add(AppProps.KEY_VERSION_CODE, iValue);
                return new AppItem(props);
            case CONTACT:
                props.add(ContactProps.KEY_CONTACT_ID, iValue);
                props.add(ContactProps.KEY_TEL_TAG, iValue);
                props.add(ContactProps.KEY_TEL_NUMBER, sValue);
                return new ContactItem(props);
            case FILE:
                props.add(FileProps.KEY_LAST_MODIFIED, lValue);
                return new FileItem(props);
            case MUSIC:
                props.add(MediaProps.KEY_MEDIA_ID, iValue);
                props.add(MediaProps.KEY_DURATION, lValue);
                props.add(MediaProps.KEY_ARTIST_NAME, sValue);
                return new MusicItem(props);
            case PHOTO:
                props.add(MediaProps.KEY_MEDIA_ID, iValue);
                return new PhotoItem(props);
            case VIDEO:
                props.add(MediaProps.KEY_MEDIA_ID, iValue);
                props.add(MediaProps.KEY_DURATION, lValue);
                return new VideoItem(props);
		default:
			break;
        }
        return null;
    }
}
