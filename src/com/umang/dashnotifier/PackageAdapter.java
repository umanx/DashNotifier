package com.umang.dashnotifier;


import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;


public class PackageAdapter extends ArrayAdapter<PackageItem> implements SectionIndexer {
	
    private Context context;
    private String[] mSections;
    private int[] mPositions;
    private List<PackageItem> values;
    final private int bound;
 
	public PackageAdapter(Context context, List<PackageItem> values) {
		super(context, R.layout.app_item, values);
		this.context = context;
		this.values = values;
		bound = context.getResources().getDimensionPixelSize(
				R.dimen.app_picker_padding);
	}
	
	@Override
    public int getCount() {
        return values.size();
    }
	
	public void setSection(List<String> sections, List<Integer> positions){
		mSections = sections.toArray(new String[sections.size()]);
        mPositions = new int[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            mPositions[i] = positions.get(i);
        }
        
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final PackageItem item = getItem(position);
		
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.app_item, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.appitem);
		Drawable icon = item.getIcon();
		icon.setBounds(0,0,bound,bound);
		textView.setText(item.getName());
		textView.setCompoundDrawables(icon, null, null, null);
		
		
		return rowView;
	}

	@Override
    public int getPositionForSection(int section) {
        if (section < 0 || section >= mSections.length) {
            return -1;
        }
        
        return mPositions[section];
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position < 0 || position >= getCount()) {
            return -1;
        }
        
        int index = Arrays.binarySearch(mPositions, position);
        
        return index >= 0 ? index : -index - 2;
    }

    @Override
    public Object[] getSections() {
    	
        return mSections;
    }

}
