package edu.buffalo.cse.cse622.datadecoy.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

/**
 * The PermissionManager class is used for storing the set of permissions that we mock. 
 * It is a singleton class that is accessed by using getInstance().
 * 
 *
 */
public enum PermissionManager {
	INSTANCE;
	private List<String> permissionList = new ArrayList<String>();
	
	private PermissionManager() {
		Log.d("info", "SortManager Instantiated");
	}
	
	/**
	 * This method is used to identify whether an application uses one of the permissions that are required to be mocked.
	 * Currently, if an application one or more of the permissions to be mocked, all permissions are displayed to the user.
	 * Future implementations should ensure that only the permissions to be mocked are to be displayed for all applications.
	 * @param list List<String> of permissions requested by an application
	 * @return 	true if the application uses one or more permissions that are mocked.
	 * 			false if the application does not request any permission that is to be mocked.
	 */
	public boolean comparePermissions(List<String> list) {
		for(String string : list)
			if(permissionList.contains(string))
				return true;
		return false;
	}

	/**
	 * getInstance() is used to obtain the singleton instance of this class.
	 * @return Instance of the class.
	 */
	public static PermissionManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Initializes the PermissionManager class and its associated permissionList attribute.
	 * This method must be called during application initialization. 
	 * Default location for this method is the onCreate() method defined within the DecoyContentProvider class; 
	 * @param context Context of the application. It is necessary to obtain the application's assets (permissions.txt)
	 */
	public void initialize(Context context) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(context.getAssets().open("permissions.txt")));
			String line = new String();
			while((line = in.readLine()) != null)
				permissionList.add(line);
		} catch (FileNotFoundException e) {
			Log.d("error", "FileNotFoundException occured!\n" + e.getMessage());
		} catch (IOException e) {
			Log.d("error", "IOException occured while reading permissions file!\n" + e.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				Log.d("error", "IOException while closing permissions file!\n" + e.getMessage());
			}
		}
		Log.d("info", "PermissionManager has been initialized");
	}

	/**
	 * This static method is used to trim down android permissions from {@code android.Manifest.permission} format to simply permission
	 * @param permissionArray {@code String[]} containing all the permissions in the default {@code android.Manifest.permission} format.
	 * @return {@code String[]} containing permissions in a trimmed format
	 */
	public static String[] trim(String[] permissionArray) {
		String[] result = new String[permissionArray.length];
		for(int i = 0; i < permissionArray.length; i++) {
			String string = permissionArray[i].substring(permissionArray[i].lastIndexOf(".") + 1);
			result[i] = string;
		}
		return result;
	}
}
