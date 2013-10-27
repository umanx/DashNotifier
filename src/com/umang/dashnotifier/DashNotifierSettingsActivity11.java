package com.umang.dashnotifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class DashNotifierSettingsActivity11 extends AbstractSettings {
	static final String ext = "11";
	PrefsFragment mPrefsFragment;
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();
		// Display the fragment as the main content.
		FragmentManager mFragmentManager = getFragmentManager();
		mPrefsFragment = new PrefsFragment(ext);
	    mFragmentManager.beginTransaction().replace(android.R.id.content, mPrefsFragment,"pref").commit();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        finish();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
	    //super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
	    //mPrefsFragment.getPreferenceScreen().findPreference("icon_preference1").setTitle("Testing");
		
	    switch(requestCode) { 
	    case IconPicker.REQUEST_PICK_GALLERY:
	        if(resultCode == RESULT_OK){  
	            Uri selectedImage = imageReturnedIntent.getData();
	            InputStream imageStream;
				try {
					imageStream = getContentResolver().openInputStream(selectedImage);
					Bitmap iconImage = BitmapFactory.decodeStream(imageStream);
					
					try {
					       FileOutputStream out = new FileOutputStream(getFilesDir() +
			                        "/icon_"+ext + ".png");
					       iconImage.compress(Bitmap.CompressFormat.PNG, 90, out);
					       out.close();
					       editor.putString("iconExt"+ext, getFilesDir().toString()+"/icon_"+ext+".png");
					       editor.putString("iconExt_default_"+ext, getFilesDir().toString()+"/icon_"+ext+".png");
					       editor.commit();
					       File iconFile = new File(getFilesDir().toString()+"/icon_"+ext+".png");
					       iconFile.setReadable(true, false);
					} catch (Exception e) {
					       e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
	            
	        }
	    }
	}

}
