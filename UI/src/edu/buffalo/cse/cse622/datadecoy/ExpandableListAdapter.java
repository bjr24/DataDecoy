package edu.buffalo.cse.cse622.datadecoy;




import java.util.LinkedList;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;


public class ExpandableListAdapter extends BaseExpandableListAdapter{
	private Context context;
	private LinkedList<String> groups;
	private LinkedList<LinkedList<String>> children;
	static int lastGroup = -1,lastChild = -1;	// The lastGroup and lastChild are used to save state before refresh
	
	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}
	
	public ExpandableListAdapter(Context context, LinkedList<String> groups,
				LinkedList<LinkedList<String>> children) {
			this.context = context;
			this.groups = groups;
			this.children = children;
			
		}

	public void addItem(String name,LinkedList<String> permissions) {
		if (!groups.contains(name)) {
			groups.add(name);
		}
		int index = groups.indexOf(name);
		if (children.size() < index + 1) {
			children.add(new LinkedList<String>());
		}
		children.get(index).addAll(permissions);
	}
	  
	  public String getChild(int groupPosition, int childPosition) {
			return children.get(groupPosition).get(childPosition);
		}


		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}
		
		// Return a child view. You can load your custom layout here.
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
				View convertView, ViewGroup parent) {
			String string = (String) getChild(groupPosition, childPosition);
			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(R.layout.child_layout, null);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.tvChild);
			String app 	= getGroup(groupPosition);
			String perm	= getChild(groupPosition, childPosition);
			Cursor cursor = context.getContentResolver().query(DecoyContentProvider.PROVIDER_URI, null, 
																null, new String[] {app,perm}, null);
			if(cursor.getCount() > 0) {
				cursor.moveToFirst();
				int type = Integer.parseInt(cursor.getString(2));
				cursor.close();
				cursor = null;
				switch(type) {
				case 0 : {
					tv.setTextColor(context.getResources().getColor(R.color.white));
					break;
				}
				case 1 : {
					tv.setTextColor(context.getResources().getColor(R.color.red));
					break;
				}
				case 2 : {
					tv.setTextColor(context.getResources().getColor(R.color.yellow));
					break;
				}
				}
				
			}
			else
				tv.setTextColor(context.getResources().getColor(R.color.white));
			tv.setText("   " + string);
			return convertView;
		}


		public int getChildrenCount(int groupPosition) {
			return children.get(groupPosition).size();
		}


		public String getGroup(int groupPosition) {
			return groups.get(groupPosition);
		}


		public int getGroupCount() {
			return groups.size();
		}


		public long getGroupId(int groupPosition) {
			return groupPosition;
		}


		// Return a group view. You can load your custom layout here.
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
				ViewGroup parent) {
			String group = (String) getGroup(groupPosition);
			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(R.layout.group_layout, null);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
			tv.setText(group);
			if(isExpanded)
				lastGroup = groupPosition;
			return convertView;
		}


		public boolean hasStableIds() {
			return true;
		}


		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}

		public void setExpandedGroup() {
			if(lastGroup >= 0)
				getGroupView(lastGroup, true, null, null);
		}
}
