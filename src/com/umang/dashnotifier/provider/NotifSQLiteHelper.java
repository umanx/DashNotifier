package com.umang.dashnotifier.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotifSQLiteHelper extends SQLiteOpenHelper {
	public static final String TABLE_NOTIF = "notifications";
	public static final String COL_ID = "_id";
    public static final String COL_PNAME = "package_name";
    public static final String COL_NOTIF_ID = "notif_id";
    public static final String COL_TITLE = "notif_title";
    public static final String COL_TEXT = "notif_text";
    public static final String COL_EXTRA = "notif_extra";
    public static final String COL_TIME = "notif_time";
    public static final String COL_TICKER = "notif_ticker";
    public static final String COL_CLEAR = "notif_clearable";
    public static final String COL_ONGOING = "notif_ongoing";
    public static final String COL_COUNT = "notif_count";
    
    private static final String DATABASE_NAME = "dashNotifications";
    private static final int DATABASE_VERSION = 2;
	
	public NotifSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		 String CREATE_NOTIF_TABLE = "CREATE TABLE " + TABLE_NOTIF + 
				 "(" + COL_ID 	+ " integer primary key autoincrement, "
				 + COL_PNAME 	+ " text not null, "
				 + COL_NOTIF_ID + " integer, " 
				 + COL_TITLE 	+ " text not null, " 
				 + COL_TEXT 	+ " text, " 
				 + COL_EXTRA 	+ " text, " 
				 + COL_TIME 	+ " text not null, " 
				 + COL_TICKER 	+ " text, "
				 + COL_CLEAR 	+ " integer, "
				 + COL_ONGOING 	+ " integer, "
				 + COL_COUNT	+ " integer );";
		 db.execSQL(CREATE_NOTIF_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIF);
		onCreate(db);
	}

}
