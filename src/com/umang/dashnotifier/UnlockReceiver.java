package com.umang.dashnotifier;

import java.util.ArrayList;

import com.umang.dashnotifier.provider.NotifSQLiteHelper;
import com.umang.dashnotifier.provider.NotificationProvider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class UnlockReceiver extends BroadcastReceiver {

	private static final int exts = 7;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("UnlcokReceiver", "Unlocked");
		ArrayList<String> packageNames = clearOnUnlock(context);
		System.out.println(packageNames.toString());
		
		if ( !packageNames.isEmpty() ){
			int count = context.getContentResolver().delete(NotificationProvider.CONTENT_URI,  
					NotifSQLiteHelper.COL_PNAME  + " IN ( " + makePlaceholders(packageNames.size()) + " )", 
					packageNames.toArray(new String[packageNames.size()]));
			System.out.println("Deleted: "+ Integer.toString(count));
		}
		

	}
	
	private ArrayList<String> clearOnUnlock(Context context){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		ArrayList<String> packages = new ArrayList<String>();
		String pName;
		for (int i = 1 ; i<=exts ; i++){
			if (preferences.getBoolean("clear_on_unlock" + Integer.toString(i), false)){
				pName = preferences.getString("extapp" + Integer.toString(i), "dummy.xx.name");
				if (pName != "dummy.xx.name")
					packages.add(pName);
			}
		}
		return packages;
	}
	
	String makePlaceholders(int len) {
	    if (len < 1) {
	        // It will lead to an invalid query anyway ..
	        throw new RuntimeException("No placeholders");
	    } else {
	        StringBuilder sb = new StringBuilder(len * 2 - 1);
	        sb.append("?");
	        for (int i = 1; i < len; i++) {
	            sb.append(",?");
	        }
	        return sb.toString();
	    }
	}

}
