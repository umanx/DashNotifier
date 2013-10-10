package com.umang.dashnotifier;

import java.util.ArrayList;

import com.umang.dashnotifier.provider.NotificationProvider;
import com.umang.dashnotifier.provider.NotificationStore;

import android.accessibilityservice.AccessibilityService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class DashNotificationListenerAcc extends AccessibilityService {
	private static final String TAG = "DashNotificationListenerAcc";
	
	ContentResolver resolver;
	String[] projection;
	ArrayList<String> notifText;
	SharedPreferences preferences;
	boolean screen_on_pref;
	boolean proximity_pref;
	PowerManager pm;
	Uri mUri;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		projection = NotificationStore.allColumns;
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		Log.v(TAG, "Service created");
		int count = getContentResolver().delete(
				NotificationProvider.CONTENT_URI, null, null);
		Log.v(TAG, "Deleted on create: " + Integer.toString(count));
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		
		Log.d(TAG,"Event detected");
		
	    if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED){
	    	
	    }

	}

	@Override
	public void onInterrupt() {
		
	}

}
