package com.umang.dashnotifier;

import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;




public class AppSelectActivity extends Activity {

	String message;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null){
			FragmentManager mFragmentManager = getFragmentManager();
			FragmentTransaction mFragmentTransaction = mFragmentManager
					.beginTransaction();
			
			AppListFragment fragment = new AppListFragment();
			mFragmentTransaction.add(android.R.id.content, fragment);
			mFragmentTransaction.commit();
		}
		
		
	}

}
