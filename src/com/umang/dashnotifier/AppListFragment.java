package com.umang.dashnotifier;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class AppListFragment extends ListFragment {
	private PackageAdapter adapter;
	private List<PackageItem> data;
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	PackageManager pm;
	//private static final String TAG = "AppListFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pm = getActivity().getPackageManager();
		setRetainInstance(true);
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		editor = preferences.edit();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState == null){
			startNewAsyncTask();
			super.onCreate(savedInstanceState);
			data = new ArrayList<PackageItem>();
		}
		
	}
	
	public void onListItemClick(ListView l, View v, int position, long id) {

		PackageItem item = (PackageItem) getListAdapter().getItem(position);
		
		editor.putString(getActivity().getIntent().getStringExtra("ext"),
				item.getPackageName());
		editor.commit();
		Toast.makeText(getActivity(), item.getName() + " added.",
				Toast.LENGTH_LONG).show();
		getActivity().finish();

	}

	private void startNewAsyncTask() {
		ListAppTask asyncTask = new ListAppTask(this, getActivity());
		asyncTask.execute();
	}

	private class ListAppTask extends AsyncTask<Void, Void, List<PackageItem>> {
		private ArrayList<String> sections = new ArrayList<String>();
        private ArrayList<Integer> positions = new ArrayList<Integer>();
		private WeakReference<AppListFragment> fragmentWeakRef;
		private Context mContext;
		
		private ListAppTask(AppListFragment fragment, Context context) {
			this.fragmentWeakRef = new WeakReference<AppListFragment>(fragment);
			this.mContext = context;
		}

		@Override
		protected List<PackageItem> doInBackground(Void... args) {
			String lastSectionIndex = null;
	        int offset = 0;
			
	        
			List<ApplicationInfo> listInfo = pm.getInstalledApplications(0);
			Collections.sort(listInfo,
					new ApplicationInfo.DisplayNameComparator(pm));
			List<PackageItem> data = new ArrayList<PackageItem>();

			for (ApplicationInfo content : listInfo) {
				String sectionIndex;
				try {
					if ((content.flags != ApplicationInfo.FLAG_SYSTEM)
							&& content.enabled) {
						
							data.add(new PackageItem(content.packageName, 
									pm.getApplicationLabel(content).toString(),
									pm.getApplicationIcon(content.packageName)));
							
							if (pm.getApplicationLabel(content).toString().isEmpty()) {
				                sectionIndex = "";
				            } else {
				                sectionIndex = pm.getApplicationLabel(content).toString().substring(0, 1).toUpperCase();
				            }
							if (lastSectionIndex == null) {
				                lastSectionIndex = sectionIndex;
				            }
							
				            if (!TextUtils.equals(sectionIndex, lastSectionIndex)) {
				                sections.add(sectionIndex);
				                positions.add(offset);
				                lastSectionIndex = sectionIndex;
				            }
				            offset++;
						
					}
				} catch (Exception e) {

				}
			}
			
			return data;
		}

		@Override
		protected void onPostExecute(List<PackageItem> result) {
			data.clear();
			data.addAll(result);
			adapter = new PackageAdapter(mContext, data);
			adapter.notifyDataSetChanged();
			if (this.fragmentWeakRef.get() != null) {
				setListAdapter(adapter);
				adapter.setSection(sections, positions);
				getListView().setFastScrollEnabled(true);
				//getListView().setFastScrollAlwaysVisible(true);
			}
		}
	}
}
