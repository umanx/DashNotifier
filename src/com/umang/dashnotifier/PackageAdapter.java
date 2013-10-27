package com.umang.dashnotifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class PackageAdapter extends ArrayAdapter<PackageItem> implements
		SectionIndexer, Filterable {

	private Context context;
	private String[] mSections;
	private int[] mPositions;
	private ArrayList<PackageItem> values;
	private ArrayList<PackageItem> filtered;
	final private int bound;
	private Filter filter;

	public PackageAdapter(Context context, ArrayList<PackageItem> values) {
		super(context, R.layout.app_item, values);
		this.context = context;
		this.values = new ArrayList<PackageItem>();
		this.values.addAll(values);
		this.filtered = new ArrayList<PackageItem>();
		this.filtered.addAll(values);
		bound = context.getResources().getDimensionPixelSize(
				R.dimen.app_picker_padding);
	}

	@Override
	public int getCount() {
		return filtered.size();
	}

	public void setSection(List<String> sections, List<Integer> positions) {
		mSections = sections.toArray(new String[sections.size()]);
		mPositions = new int[positions.size()];
		for (int i = 0; i < positions.size(); i++) {
			mPositions[i] = positions.get(i);
		}

	}

	public PackageItem getByPosition(int position) {
		return values.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;

		if (convertView == null) {
			convertView = View.inflate(context, R.layout.app_item, null);
			viewHolder = new ViewHolder();
			viewHolder.appRow = (TextView) convertView
					.findViewById(R.id.appitem);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		PackageItem item = filtered.get(position);
		
		if (item != null) {
			Drawable icon = item.getIcon();
			icon.setBounds(0, 0, bound, bound);
			viewHolder.appRow.setText(item.getName());
			viewHolder.appRow.setCompoundDrawables(icon, null, null, null);
		}

		return convertView;
	}

	// Using a ViewHolder results in >2x performance improvement per row
	static class ViewHolder {
		TextView appRow;
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

	@Override
	public Filter getFilter() {
		if (filter == null)
			filter = new ApplicationNameFilter();
		return filter;
	}

	private class ApplicationNameFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			constraint = constraint.toString().toLowerCase();
			FilterResults result = new FilterResults();
			if (constraint != null && constraint.toString().length() > 0) {
				ArrayList<PackageItem> filteredItems = new ArrayList<PackageItem>();

				for (int i = 0, l = values.size(); i < l; i++) {
					PackageItem item = values.get(i);
					if (item.getName().toLowerCase().contains(constraint))
						filteredItems.add(item);
				}
				result.count = filteredItems.size();
				result.values = filteredItems;
			} else {
				synchronized (this) {
					result.values = values;
					result.count = values.size();
				}
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {

			filtered = (ArrayList<PackageItem>) results.values;
			notifyDataSetChanged();
			clear();
			for (int i = 0, l = filtered.size(); i < l; i++)
				add(filtered.get(i));
			notifyDataSetInvalidated();
		}
	}

}
