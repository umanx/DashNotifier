package com.umang.dashnotifier;



import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.umang.dashnotifier.provider.NotificationProvider;



public class DashNotifierExtension8 extends DashClockExtension {
	static final String extNumber= "8";
	SharedPreferences preferences;
	PackageManager pm ;
	String packageName ;
	Cursor result;
	String[] prefValues;
	
	@Override
	public void onCreate(){
		super.onCreate();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		pm = getPackageManager();
		prefValues = getResources().getStringArray(R.array.entryvalues_list_preference);
		
	}
	
	@Override
	protected void onUpdateData(int arg0) {
		ExtensionData data = Commons.publishOrPerish(this, extNumber, preferences, pm, prefValues);
		publishUpdate(data);
			
	}
	
	@Override
    protected void onInitialize(boolean isReconnect) {
		super.onInitialize(isReconnect);
        if (!isReconnect) {
            addWatchContentUris(new String[]{NotificationProvider.CONTENT_URI.toString()});
        }
	}

}
