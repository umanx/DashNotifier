package com.umang.dashnotifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.umang.dashnotifier.R;
import com.umang.dashnotifier.provider.NotifSQLiteHelper;
import com.umang.dashnotifier.provider.NotificationProvider;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;


public class DashNotifierSettingsActivity5 extends AbstractSettings {
	static final String ext = "5";
	PrefsFragment mPrefsFragment;
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();
		// Display the fragment as the main content.
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction mFragmentTransaction = mFragmentManager
				.beginTransaction();
		mPrefsFragment = new PrefsFragment();
		mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
		mFragmentTransaction.commit();
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
	
	public static class PrefsFragment extends PreferenceFragment implements IconPicker.OnIconPickListener{
		SharedPreferences preferences;
		private static final String TAG = "PrefsFragment";
		private static final String extNumber = "5";
		String[] prefText;
		String[] prefValues;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.dashnotifiersettings5);
			preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
			prefText = getResources().getStringArray(R.array.entries_list_preference);
			prefValues = getResources().getStringArray(R.array.entryvalues_list_preference);
			
		}
		
		@Override
		public void onResume(){
			super.onResume();
			Drawable icon;
			
			String packageName = preferences.getString("extapp"+extNumber, "dummy.xx.name"); 
			IconPicker ip = new IconPicker(getActivity(), this, extNumber);
			if (!packageName.equals("dummy.xx.name")){
				PackageManager pm = getActivity().getPackageManager();
				
				try {
					ApplicationInfo content = pm.getApplicationInfo(packageName, 0);
					getPreferenceScreen().findPreference("extapp"+extNumber).setTitle
					(pm.getApplicationLabel(content).toString());
					getPreferenceScreen().findPreference("extapp"+extNumber).setIcon(
							pm.getApplicationIcon(packageName));
					String iconPath = preferences.getString("iconExt"+extNumber, "");
					Resources r = getResources();

					getPreferenceScreen().findPreference("ext_title_preference"+extNumber).setSummary
					(prefText[findPosition(prefValues,preferences.getString("ext_title_preference"+extNumber, "app_count"))]);
					
					if (preferences.getString("ext_title_preference"+extNumber, "app_count").contains("custom"))
						getPreferenceScreen().findPreference("text_preference"+extNumber).setEnabled(true);
					try {
						if(!TextUtils.isEmpty(iconPath))
							icon = Drawable.createFromPath(iconPath);
						else{
							int iconId = r.getIdentifier(
									preferences.getString("icon_preference" + extNumber,
											"dummy"), "drawable", getActivity().getPackageName());
							icon = getResources().getDrawable(iconId);
						}
						icon.setColorFilter( Color.DKGRAY, Mode.MULTIPLY );
						getPreferenceScreen().findPreference("icon_preference"+ extNumber).setIcon(icon);
					} catch (NotFoundException e) {
						Log.w("DashNotifier"+extNumber, "No icon set");
					}
					
					getPreferenceScreen().findPreference("icon_preference"+extNumber).
					setOnPreferenceClickListener(ip);
					
					getPreferenceScreen().findPreference("show_ongoing"+extNumber).setOnPreferenceClickListener
					(new Preference.OnPreferenceClickListener(){
						@Override
						public boolean onPreferenceClick(Preference preference) {
							String packageName = preferences.getString("extapp"+extNumber, "dummy.xx.name"); 
							int count = getActivity().getContentResolver().delete(
									NotificationProvider.CONTENT_URI,
									NotifSQLiteHelper.COL_PNAME + " = ? ",
									new String[] { packageName });
							Log.v(TAG,"Deleted: " + Integer.toString(count));
							
							return true;
						}
					});
							
					getPreferenceScreen().findPreference("ext_title_preference"+extNumber).setOnPreferenceChangeListener
					( new Preference.OnPreferenceChangeListener() {
						
						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							preference.setSummary(prefText[findPosition(prefValues,(String)newValue)]);
							if ( newValue.toString().contains("custom"))
								getPreferenceScreen().findPreference("text_preference"+extNumber).setEnabled(true);
							else
								getPreferenceScreen().findPreference("text_preference"+extNumber).setEnabled(false);
							return true;
						}
					});
				} catch (NameNotFoundException e) {
					Log.d(TAG, e.getStackTrace().toString());
					e.printStackTrace();
				}
			}
			
			getPreferenceScreen().findPreference("about"+extNumber).setOnPreferenceClickListener
			(new Preference.OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference preference) {
					HelpUtils.showAboutDialog(
                            getActivity());
					
					return true;
				}
			});
		}

		@Override
		public void iconPicked() {
			int i = getResources().getIdentifier(preferences.getString("icon_preference"+ extNumber, "dummy"), "drawable", getActivity().getPackageName());
			Drawable drawable = getResources().getDrawable(i);
			if(drawable != null) {
				drawable.setColorFilter(Color.DKGRAY, android.graphics.PorterDuff.Mode.MULTIPLY);
				getPreferenceScreen().findPreference("icon_preference"+ extNumber).setIcon(drawable);
			}
		}
		
		public int findPosition(String[] array, String value){
			for (int i = 0; i < array.length; i++){
				if (array[i].equals(value))
					return i;
			}
			return -1;
		}
	}

}
