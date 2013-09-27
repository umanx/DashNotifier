package com.umang.dashnotifier.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class NotificationStore {
	private SQLiteDatabase database;
	  private NotifSQLiteHelper dbHelper;
	  public static final String[] allColumns = { NotifSQLiteHelper.COL_ID, 
			  NotifSQLiteHelper.COL_PNAME,
			  NotifSQLiteHelper.COL_NOTIF_ID, 
			  NotifSQLiteHelper.COL_TITLE, 
			  NotifSQLiteHelper.COL_TEXT,
			  NotifSQLiteHelper.COL_EXTRA, 
			  NotifSQLiteHelper.COL_TIME, 
			  NotifSQLiteHelper.COL_TICKER, 
			  NotifSQLiteHelper.COL_CLEAR, 
			  NotifSQLiteHelper.COL_ONGOING,
			  NotifSQLiteHelper.COL_COUNT};

	  public NotificationStore(Context context) {
	    dbHelper = new NotifSQLiteHelper(context);
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	    
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  public long storeNotification(ContentValues notif) {
	    long insertId = database.insert(NotifSQLiteHelper.TABLE_NOTIF, null,
	        notif);
	    if (insertId == -1)
	    	return insertId;
	    else{
	    	Cursor temp = database.query(NotifSQLiteHelper.TABLE_NOTIF,
	    	        allColumns, null, null, null, null, null);
	    	if (temp.moveToLast())
	    		return temp.getLong(0);
	    	else 
	    		return -1;
	    }
	  }

	  public int removeNotification(String selection, String[] selectionArgs) {
	    return database.delete(NotifSQLiteHelper.TABLE_NOTIF, selection ,selectionArgs);
	  }
	  
	  
	  
	  public int removeNotification(String packageName, int notif_id){
		  return database.delete(NotifSQLiteHelper.TABLE_NOTIF, 
				  NotifSQLiteHelper.COL_PNAME + " = ? and " + NotifSQLiteHelper.COL_NOTIF_ID + " = ?", 
				  new String[]{packageName, Integer.toString(notif_id)});
	  }
	  
	  public int removeNotification(String id){
		  return database.delete(NotifSQLiteHelper.TABLE_NOTIF, NotifSQLiteHelper.COL_ID
			        + " =? ",new String[]{id} );
	  }
	  
	  

	  public int updateNotification( ContentValues values, String whereClause, String[] whereArgs){
		  return database.update(NotifSQLiteHelper.TABLE_NOTIF, values, whereClause, whereArgs);
	  }
	  
	  public Cursor query( String[] projection, String selection, String[] selectionArgs, String sortOrder){
		  return database.query(NotifSQLiteHelper.TABLE_NOTIF, allColumns, selection, selectionArgs, null, null, sortOrder);
	  }
	  
	  
	  
} 


