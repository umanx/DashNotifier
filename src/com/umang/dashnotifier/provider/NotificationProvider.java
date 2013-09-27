package com.umang.dashnotifier.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class NotificationProvider extends ContentProvider {
	private static final String TAG = "NotificationProvider";
	private NotificationStore db;
	// Used for the UriMacher
	private static final int NOTIFICATIONS = 10;
	private static final int NOTIFICATION_ID = 20;

	// public constants for client development
	public static final String AUTHORITY = "com.umang.provider.dashnotifier";
	private static final String BASE_PATH = "notifications";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
		      + "/notifications";
		  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
		      + "/notification";
		  
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, NOTIFICATIONS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", NOTIFICATION_ID);
	}
	// helper constants for use with the UriMatcher
	
	//private static final UriMatcher URI_MATCHER;
	public boolean check(){
		return true;
	}

    @Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
    	int uriType = sURIMatcher.match(uri);
        int rowsDeleted = 0;
        switch (uriType) {
        case NOTIFICATIONS:
          rowsDeleted = db.removeNotification(selection, selectionArgs);
          break;
        case NOTIFICATION_ID:
          String id = uri.getLastPathSegment();
          if (TextUtils.isEmpty(selection)) {
            rowsDeleted = db.removeNotification(id);
          } else {
            rowsDeleted = db.removeNotification(selection, selectionArgs);
          }
          break;
        default:
          throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowsDeleted > 0)
        	getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues notif) {
		
		int uriType = sURIMatcher.match(uri);
	    long id = 0;
	    switch (uriType) {
	    case NOTIFICATIONS:
	    	id = db.storeNotification(notif);
	    	break;
	    default:
	    	throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    getContext().getContentResolver().notifyChange(uri, null);
	    return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public boolean onCreate() {
		Log.d(TAG,"ContentProvider created");
		this.db = new NotificationStore(this.getContext());
		db.open();
	    if (this.db == null) {
	    	return false;
	    }
	    return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
		return db.query(projection, selection, selectionArgs, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
	    int rowsUpdated = 0;
	    switch (uriType) {
	    case NOTIFICATIONS:
	      rowsUpdated = db.updateNotification( 
	          values, 
	          selection,
	          selectionArgs);
	      break;
	    case NOTIFICATION_ID:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsUpdated = db.updateNotification(
	            values,
	            NotifSQLiteHelper.COL_ID + "=" + id, 
	            null);
	      } else {
	        rowsUpdated = db.updateNotification( 
	            values,
	            NotifSQLiteHelper.COL_ID + "=" + id 
	            + " and " 
	            + selection,
	            selectionArgs);
	      }
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    if (rowsUpdated > 0)
	    	getContext().getContentResolver().notifyChange(uri, null);
	    return rowsUpdated;
		
	}
	
}
