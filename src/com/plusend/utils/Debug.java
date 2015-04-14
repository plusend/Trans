package com.plusend.utils;

import android.util.Log;

public class Debug {
	public static boolean LOG_FLAG = true;
	public static boolean debug = LOG_FLAG;
	public static boolean info = LOG_FLAG;

	public static void i(String message) {
		i(null, message);
	}

	public static void i(String tag, String message) {
		if (info) {
			Log.i(makeTag(tag), message);
		}
	}

	public static void d(String message) {
		w(null, message);
	}

	public static void d(String tag, String message) {
		if (debug) {
			Log.d(tag, message);
		}
	}

	public static void w(String tag, Exception e) {
		if (e != null) {
			w(tag, "Cause:" + //
					(e.getCause() == null ? "Unknown Cause" : e.getCause().getLocalizedMessage()) + //
					"\r\nStackInfo:" + //
					Log.getStackTraceString(e)//
			);
		} else {
			w(tag, "Null Exception");
		}
	}

	public static void e(Exception e) {
		e(null, e);
	}

	public static void e(String message) {
		e(null, message);
	}

	public static void e(String tag, String message) {

		Log.e(makeTag(tag), message);
	}

	public static void e(String tag, Exception e) {
		if (e != null) {
			e(tag, "Cause:" + //
					(e.getCause() == null ? "Unknown Cause" : e.getCause().getLocalizedMessage()) + //
					"\r\nStackInfo:" + //
					Log.getStackTraceString(e)//
			);
		} else {
			e(tag, "Null Exception");
		}
	}

	public static void w(Exception e) {
		w(null, e);
	}

	public static void w(String message) {
		w(null, message);
	}

	public static void w(String tag, String message) {

		Log.w(makeTag(tag), message);
	}

	private static String makeTag(String tag) {
		if (tag == null || tag.trim().equals("")) {
			return "SevengaSDK";
		} else {
			return "SevengaSDK-" + tag;
		}
	}

}
