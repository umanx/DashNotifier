package com.umang.dashnotifier;

import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;

public class AppSelectActivity extends Activity {
	AppListFragment mAppSelectFragment;
	String message;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FragmentManager mFragmentManager = getFragmentManager();
		
		mAppSelectFragment = (AppListFragment) mFragmentManager.findFragmentByTag("appSelect");
		 
	    // If the Fragment is non-null, then it is currently being
	    // retained across a configuration change.
	    if (mAppSelectFragment == null) {
	    	mAppSelectFragment = new AppListFragment();
	    	mFragmentManager.beginTransaction().replace(android.R.id.content, mAppSelectFragment,"appSelect").commit();
	    }
	}

}
