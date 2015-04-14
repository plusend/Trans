package com.plusend.utils;

import java.io.File;

import android.webkit.MimeTypeMap;

public final class FileUtils {
    @SuppressWarnings("unused")
    private static final String TAG = "FileUtils";

    public static String getMimeType(File file) {
        return getMimeType(file.getName());
    }

    public static String getMimeType(String fileName) {
        String ext = getExtension(fileName);
        String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return mimetype;
    }

    /*
     * return file extension name, without .
     */
    public static String getExtension(String uri) {
        if (uri == null)
            return "";
        int dot = uri.lastIndexOf(".");
        if (dot == -1)
            return "";
        return uri.substring(dot + 1);
    }
    
    /**
     * remove folder's descents recursively, don't remove root directory itself.
     * note: upon return, the contents of folder may not completed removed if errors occurs.
     * @param parent the root folder
     * @throws no exceptions thrown, will ignore all errors when executing
     */
    public static final void removeFolderDescents(File parent) {
        if (parent == null || !parent.exists())
            return;

        File[] files = parent.listFiles();
        
        for (File item : files) {
            if (item.isDirectory())
                removeFolderDescents(item);
            item.delete();
        }
    }

}
