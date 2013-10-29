package com.umang.dashnotifier;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.ListFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

public class AppListFragment extends ListFragment implements OnQueryTextListener, OnCloseListener{
	private PackageAdapter adapter;
	private ArrayList<PackageItem> data;
	SharedPreferences preferences;
	SharedPreferences.Editor editor;
	PackageManager pm;
	SearchView searchView;
	AtomicBoolean showSearch = new AtomicBoolean(false);
	AtomicBoolean loadComplete = new AtomicBoolean(false);
	AtomicBoolean asyncFired = new AtomicBoolean(false);
	ArrayList<String> iconNames;
	ArrayList<String> packageNames;
	ListAppTask asyncTask ;
	
	//private static final String TAG = "AppListFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pm = getActivity().getPackageManager();
		setRetainInstance(true);
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		editor = preferences.edit();
		setHasOptionsMenu(true);
		
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
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem menuitem = menu.findItem(R.id.search);
		if(showSearch.get())
			menuitem.setVisible(true);
		
	}
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
	    inflater.inflate(R.menu.options_menu, menu);
	    
	 // Associate searchable configuration with the SearchView
	    SearchManager searchManager =
	           (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
	    searchView = (SearchView) menu.findItem(R.id.search).getActionView();
	    searchView.setSearchableInfo(
	            searchManager.getSearchableInfo(getActivity().getComponentName()));
	    searchView.setOnQueryTextListener(this);
	    searchView.setOnCloseListener(this);
	    
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		getListView().setFastScrollEnabled(true);
		getListView().setTextFilterEnabled(true);
		iconNames = new ArrayList<String>(Arrays.asList(getActivity().getResources().getStringArray(R.array.icon_file_names)));
		packageNames = new ArrayList<String>(Arrays.asList(getActivity().getResources().getStringArray(R.array.package_names)));

	}
	
	public void onListItemClick(ListView l, View v, int position, long id) {

		PackageItem item = (PackageItem) getListAdapter().getItem(position);
		
		int index = packageNames.indexOf(item.getPackageName());
		if (index != -1){
			int iconId = getActivity().getResources()
					.getIdentifier(iconNames.get(index)	, "drawable",
							getActivity().getPackageName());
			if (iconId != 0){
				editor.remove("iconExt"+getActivity().getIntent().getStringExtra("ext").substring(6));
				editor.putString("icon_preference"+getActivity().getIntent().getStringExtra("ext").substring(6),iconNames.get(index));
				editor.putString("icon_preference_default_"+getActivity().getIntent().getStringExtra("ext").substring(6),iconNames.get(index));
			}
				
		}
		
		editor.putString(getActivity().getIntent().getStringExtra("ext"),
				item.getPackageName());
		editor.commit();
		Toast.makeText(getActivity(), item.getName() + " added.",
				Toast.LENGTH_LONG).show();
		getActivity().finish();

	}
	
	private void startNewAsyncTask() {
		if (!loadComplete.get() && !asyncFired.get()){
			asyncTask = new ListAppTask(this, getActivity());
			asyncTask.execute();
		}
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if (asyncTask.cancel(true))
			Log.v("DashNotifier", "AppList loading canceled");
		
	}
	
	
	private class ListAppTask extends AsyncTask<Void, Void, List<PackageItem>> {
		private ArrayList<String> sections = new ArrayList<String>();
        private ArrayList<Integer> positions = new ArrayList<Integer>();
		private WeakReference<AppListFragment> fragmentWeakRef;
		private Context mContext;
		
		private ListAppTask(AppListFragment fragment, Context context) {
			this.fragmentWeakRef = new WeakReference<AppListFragment>(fragment);
			this.mContext = context;
			asyncFired.compareAndSet(false, true);
			System.out.println("Starting Async");
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
				showSearch.compareAndSet(false, true);
				loadComplete.compareAndSet(false, true);
				fragmentWeakRef.get().getFragmentManager().invalidateOptionsMenu();
				//getListView().setFastScrollAlwaysVisible(true);
			}
		}
	}


	@Override
	public boolean onClose() {
		
		return false;
	}

	@Override
	public boolean onQueryTextChange(String queryString) {
		adapter.getFilter().filter(queryString);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String queryString) {
		if (searchView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }
            searchView.clearFocus();
        }
        return true;
	}
}
