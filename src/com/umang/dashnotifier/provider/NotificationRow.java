package com.umang.dashnotifier.provider;

import java.util.ArrayList;

import android.content.ContentValues;

public class NotificationRow {
	private long id;
	private String packageName;
	private int notif_id;
	private String title;
	private String text;
	private String time;
	private String ticker;
	private String clearable;
	private String onGoing;
	
	public NotificationRow(){
		
	}
	
	public NotificationRow(String pName, int nid, ArrayList<String> notifText, String tick, boolean clear, boolean status){
		this.packageName = pName;
		this.notif_id = nid;
		this.title = notifText.get(0);
		this.time = notifText.get(1);
		this.text = notifText.get(2);
		this.ticker = tick;
		this.clearable = Boolean.toString(clear);
		this.onGoing = Boolean.toString(status);
	}
	
	public long getId() {
		return this.id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public String getpackageName() {
	    return this.packageName;
	}
	
	public void setpackageName(String pName) {
		this.packageName = pName;
	}

	public int getNotifId() {
	    return this.notif_id;
	}
	
	public void setNotifId(int id) {
		this.notif_id = id;
	}

	public String getTitle() {
	    return this.title;
	}
	
	public void setTitle(String text) {
		this.title = text;
	}

	public String getTime() {
	    return this.time;
	}
	
	public void setTime(String text) {
		this.time = text;
	}
	
	public String getText() {
	    return this.text;
	}
	
	public void setText(String msg) {
		this.text = msg;
	}
	
	public String getTicker() {
	    return this.ticker;
	}
	
	public void setTicker(String text) {
		this.ticker = text;
	}
	
	public String getClearable() {
	    return this.clearable;
	}
	
	public void setClearable(String text) {
		this.clearable = text;
	}

	public String getOnGoing() {
	    return this.onGoing;
	}
	
	public void setOnGoing(String text) {
		this.onGoing = text;
	}
	
	public ContentValues toContentValue(){
		ContentValues temp  = new ContentValues();
		temp.put(NotifSQLiteHelper.COL_ID, this.id);
		temp.put(NotifSQLiteHelper.COL_PNAME, this.packageName);
		temp.put(NotifSQLiteHelper.COL_NOTIF_ID, this.notif_id);
		temp.put(NotifSQLiteHelper.COL_TITLE, this.title);
		temp.put(NotifSQLiteHelper.COL_TEXT, this.text);
		temp.put(NotifSQLiteHelper.COL_TIME, this.time);
		temp.put(NotifSQLiteHelper.COL_TICKER, this.ticker);
		temp.put(NotifSQLiteHelper.COL_CLEAR, this.clearable);
		temp.put(NotifSQLiteHelper.COL_ONGOING, this.onGoing);
	    return temp;
	    
	}
	
	
}
