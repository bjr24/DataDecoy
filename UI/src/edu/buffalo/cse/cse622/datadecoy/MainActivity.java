package edu.buffalo.cse.cse622.datadecoy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import edu.buffalo.cse.cse622.datadecoy.database.DecoyDatabase;
import edu.buffalo.cse.cse622.datadecoy.database.PermissionManager;

import static edu.buffalo.cse.cse622.datadecoy.database.DecoyDatabase.KEY_APPLICATION;
import static edu.buffalo.cse.cse622.datadecoy.database.DecoyDatabase.KEY_PERMISSION;
import static edu.buffalo.cse.cse622.datadecoy.database.DecoyDatabase.KEY_DECOYTYPE;
import static edu.buffalo.cse.cse622.datadecoy.database.DecoyDatabase.DecoyType.MOCK_AUTO;
import static edu.buffalo.cse.cse622.datadecoy.database.DecoyDatabase.DecoyType.MOCK_MANUAL;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.Dialog;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;

public class MainActivity extends Activity {
	final private List<ApplicationInfo> installedApps = new ArrayList<ApplicationInfo>();
	private ExpandableListAdapter adapter;
	private Handler handler = new Handler();
	private Context context = this;
	private StringBuilder locationListBuilder;
    private Intent _MapIntent; 
    LocationManager _locationManager;
	ExpandableListView listView = null;
	List<String> locationPermissionList = new ArrayList<String>(Arrays.asList(new String[]{"android.permission.ACCESS_COARSE_LOCATION","android.permission.ACCESS_FINE_LOCATION"}));
	private static final int LOCATION_BY_MAP = 0;
	private ContentValues mockValues;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		listView = (ExpandableListView) findViewById(R.id.listView);
		Point display = new Point();
		getWindowManager().getDefaultDisplay().getSize(display);
		_MapIntent = new Intent(this, MapContainer.class);
		_locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		listView.setIndicatorBounds(display.x - 20, display.x-5);
		
		listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			public boolean onChildClick(ExpandableListView arg0, View arg1, final int groupID, 
					final int childID, final long cRowID) {
/*				Here, The plan is to throw up a dialog box allowing the user to choose what he wants to do
 * 				With this particular permission (default / mock / manual-mock)
 * 				We will also update the database accordingly
 */
				final Dialog dialog = new Dialog(MainActivity.this);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//				dialog.setTitle("Set Mocking Option");
				dialog.setContentView(R.layout.mock_dialog);
				dialog.setCanceledOnTouchOutside(true);
				
				String app 	= adapter.getGroup(groupID);
				String perm	= adapter.getChild(groupID, childID);
				Cursor cursor = getContentResolver().query(DecoyContentProvider.PROVIDER_URI, null, null,
															new String[] {app,perm},null);
				

/*
 * 				At this point we need to recover from the database the current setting if there is one.
 * 				The RadioGroup in the dialog will have this preset.
 */	
				int value = 0;
				if(cursor.getCount() > 0) {
				
					cursor.moveToFirst();
					value = Integer.parseInt(cursor.getString(2));  // The index of KEY_DECOYTYPE in the database
					cursor.close();
				}
				RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radioGroup);
				switch(value) {
				case 0 : {
					radioGroup.check(R.id.mockDefault_radioButton);
					break;
				}
				case 1 : {
					radioGroup.check(R.id.mockAuto_radioButton);
					break;
				}
				case 2 : {
					radioGroup.check(R.id.mockManual_radioButton);
					break;
				}
				case 3 : {
					radioGroup.check(R.id.injectLoc_radioButton);
					break;
				}
				default : {
					dialog.setContentView(new View(MainActivity.this));
					break;
				}
				}
				try {
					dialog.show();
					
//					Dialog's Apply Button's onClickListener()
					
					Button applyButton = (Button) dialog.findViewById(R.id.apply_button);
					applyButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radioGroup);
							int result = radioGroup.getCheckedRadioButtonId();
							
							
							ContentValues values = new ContentValues();
							values.put(KEY_APPLICATION, adapter.getGroup(groupID));
							values.put(KEY_PERMISSION, adapter.getChild(groupID, childID));
							
							switch(result) {
							case R.id.mockDefault_radioButton 	: {
								String app 	= adapter.getGroup(groupID);
								String perm	= adapter.getChild(groupID, childID);
								getContentResolver().delete(DecoyContentProvider.PROVIDER_URI,null,new String[]{app,perm});
								break;
							}
							
							case R.id.mockAuto_radioButton		: {
								values.put(KEY_DECOYTYPE, MOCK_AUTO.toString());
								getContentResolver().insert(DecoyContentProvider.PROVIDER_URI, values);
								break;
							}
							
							case R.id.mockManual_radioButton	: {
								//use the map container to obtain a trace.
								//  Eventually put in a content provider to decide how 
								//  to obtain the trace (gps track, or manual entry).
								values.put(KEY_DECOYTYPE, MOCK_MANUAL.toString());
								mockValues = values;
								startActivityForResult(_MapIntent, LOCATION_BY_MAP);
								break;
							}
							
							case R.id.injectLoc_radioButton	: {
								//use the map container to obtain a trace.
								//  Eventually put in a content provider to decide how 
								//  to obtain the trace (gps track, or manual entry).
								try {
							        Method method = _locationManager.getClass().getDeclaredMethod("changeLocation", String.class,String.class,String.class);
							        String[] args = new String[] {adapter.getGroup(groupID), "10 20", "gps"};    // CHANGE THIS
							        int val = (Integer) method.invoke(_locationManager, (Object[]) args);
							        if(val < 0)
							            Log.d("MOD","changeLocation reflection call failed!");
							        else
							            Log.d("MOD", "changeLocation reflection succeeded!" + val);
							    } catch (NoSuchMethodException e) {
							        Log.d("MOD","NoSuchMethodException!");
							        Log.d("MOD", e.getMessage());
							    } catch (IllegalArgumentException e) {
							        Log.d("MOD","IllegalArgumentException!");
							        Log.d("MOD", e.getMessage());
							    } catch (IllegalAccessException e) {
							        Log.d("MOD","IllegalAccessException!");
							        Log.d("MOD", e.getMessage());    
							    } catch (InvocationTargetException e) {
							        Log.d("MOD","InvocationTargetException!");
							        Log.d("MOD", e.getMessage());


							    }
								break;
							}
							}
							
							dialog.dismiss();
							refreshInstalledApps(v);
							ExpandableListAdapter.lastChild = childID;
							
							
						}
					});
					
//					Dialog's Cancel Button's onClickListener()
					Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
					cancelButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
				} catch (NullPointerException e) {
					dialog.dismiss();
					final Dialog errDialog = new Dialog(MainActivity.this);
					errDialog.setContentView(new android.widget.LinearLayout(MainActivity.this), 
							new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
					
					final int exitCode = value;
					errDialog.setTitle("Unknown DecoyType :" + value);
					
					Button button = new Button(dialog.getContext());
					button.setText("OK");
					button.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
					button.setVisibility(View.VISIBLE);
					
					button.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
							
							throw new IllegalStateException("Unimplemented DecoyType :" + exitCode);
						}
					});
					
					errDialog.addContentView(button,
							new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					errDialog.show();
				} finally {
					
				}
				return false;
			}
		});
		
		listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				return false;
			}
		});

		// Initialize the adapter with blank groups and children
		// We will be adding children on a thread, and then update the ListView
		adapter = new ExpandableListAdapter(this,new LinkedList<String>(),new LinkedList<LinkedList<String>>());
		// Set this blank adapter to the list view
		listView.setAdapter(adapter);
		getInstalledApps();
	}

	/**
	 * Lists the installed applications on the phone.
	 * Currently, the method is set to filter out applications and permissions
	 */
	private void getInstalledApps() {
		Thread appList_thread = new Thread(new Runnable() {
			public void run() {
				PackageManager pm = getPackageManager();
				List<ApplicationInfo> apps = pm.getInstalledApplications(0);
				for(ApplicationInfo app : apps) {
					try {
						String[] permissionArray = pm.getPackageInfo(app.packageName, 
														PackageManager.GET_PERMISSIONS).requestedPermissions;
						
						/*
						 * Check if the application has any permissions specified within our PermissionManager's list
						 * If it does contain any permission, then we add this application to the list.
						 */
						if(permissionArray != null) {
							permissionArray = PermissionManager.trim(permissionArray);
							List<String> permissionList = Arrays.asList(permissionArray);
							if(PermissionManager.getInstance().comparePermissions(permissionList))
								installedApps.add(app);
						}
					} catch (NameNotFoundException e) {
						Log.e("error","NameNotFound  :"+e.getMessage());
					}
				}
				Log.d("info", "Installed Apps Received. Size  :"+installedApps.size());
				for(final ApplicationInfo inf : installedApps) {
					try {
						PackageInfo packageInfo			= pm.getPackageInfo(inf.packageName, 
																PackageManager.GET_PERMISSIONS);
						if(packageInfo.requestedPermissions != null) {
							final LinkedList<String> permissionList = new LinkedList<String>();

							for(String string : packageInfo.requestedPermissions) {
								if(locationPermissionList.contains(string)) {
									string = string.substring(string.lastIndexOf(".")+1);
									permissionList.add(string);
								}
							}
//							
							handler.post(new Runnable() {
								public void run() {
									String name = inf.packageName.substring(inf.packageName.lastIndexOf(".") + 1);
									
									/* Name needs the substring since packages are of the form com.xxx.yyy.zzz
									 * The above statement removes all the sub-package information except the last one a.k.a 'zzz'  
									 */
									adapter.addItem(name, permissionList);
								}
							});
							try
							{
								// Sleep for two seconds to avoid concurrent manipulations
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							handler.post(new Runnable() {
								public void run() {
									
									adapter.notifyDataSetChanged();
									
								}
							});
						}
						
					} catch (NameNotFoundException e) {
						Log.e("error", "Package name :"+inf.packageName+" not found!");
					}
				}
			}
			
		});
		appList_thread.run();
		System.out.println("");
	}
	
    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) { 
    	super.onActivityResult(requestCode, resultCode, data); 
    	  switch(requestCode) { 
    	    case LOCATION_BY_MAP : { 
    	      if (resultCode == Activity.RESULT_OK) { 
    	    	  Bundle rtnValue = data.getExtras();
    	    	  if(rtnValue != null){
    	    		  Log.d("MOD", "Activity returns something");
    	    		  ArrayList<String> locationList = new ArrayList<String>();
    	    		  locationList.addAll(rtnValue.getStringArrayList(MapContainer.RESULT_KEY));
    	    		  locationListBuilder = new StringBuilder();
    	    		  for(int i = 0; i < locationList.size(); i++){
    	    			  locationListBuilder.append(locationList.get(i) + System.getProperty("line.separator"));
    	    		  }
    	    		  Log.d("MOD", "String is, in length, " + locationListBuilder.length());
    	    		  if(mockValues != null){
    	    			  if( locationListBuilder != null){
    	    				  mockValues.put(DecoyDatabase.KEY_TRACE_PATH, locationListBuilder.toString());
    	    		  		}
    	    		  		getContentResolver().insert(DecoyContentProvider.PROVIDER_URI, mockValues);
    	    		  }	
    	    		  else Log.d("MOD", "Values were empty");
    	    	  }
    	      // TODO Switch tabs using the index.
    	      } 
    	      break; 
    	    } 
    	  } 
    }
	   
	public void refreshInstalledApps(View view) {
		Thread refresh_thread = new Thread(new Runnable() {
			public void run() {
//				Reset the adapter and the list. . .getInstalledApps again!
				adapter = new ExpandableListAdapter(context,new LinkedList<String>(),
														new LinkedList<LinkedList<String>>());
				
				listView.setAdapter(adapter);
				installedApps.clear();
				getInstalledApps();
				
					
			}
		});
		refresh_thread.run();
		try {
			refresh_thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		handler.post(new Runnable() {
			public void run() {
				
				if(ExpandableListAdapter.lastGroup >= 0) {
					listView.expandGroup(ExpandableListAdapter.lastGroup);
					if(ExpandableListAdapter.lastChild < 0)
						listView.setSelectedGroup(ExpandableListAdapter.lastGroup);
					else
						listView.setSelectedChild(ExpandableListAdapter.lastGroup, 
								ExpandableListAdapter.lastChild, true);
				}
				else
					listView.setAdapter(adapter);
			}
		});
	}
}
