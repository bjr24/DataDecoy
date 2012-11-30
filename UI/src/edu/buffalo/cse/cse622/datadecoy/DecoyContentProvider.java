package edu.buffalo.cse.cse622.datadecoy;

/*
 * The ContentProvider class is mainly used to allow other applications to access a particular application's data
 * This is not possible without the ContentProvider.
 * 
 * Since our application has no such use where its private data needs to be accessed by third-party applications,
 * This class need not be implemented at all. The ApplicationTableSource class has the necessary mechanisms to
 * interact with the Database.
 * 
 * However, if we do wish to proceed with this and implement the ContentProvider to do some fancy data-pulls
 * for Steve and Geoff, we can do so using this as a code-base. 
 */


import edu.buffalo.cse.cse622.datadecoy.database.DecoyDatabase;
import edu.buffalo.cse.cse622.datadecoy.database.PermissionManager;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import static edu.buffalo.cse.cse622.datadecoy.database.DecoyDatabase.KEY_APPLICATION;
import static edu.buffalo.cse.cse622.datadecoy.database.DecoyDatabase.KEY_PERMISSION;
import static edu.buffalo.cse.cse622.datadecoy.database.DecoyDatabase.KEY_DECOYTYPE;

public class DecoyContentProvider extends ContentProvider {
	private DecoyDatabase database;
	public static final Uri PROVIDER_URI = Uri.parse("content://edu.buffalo.cse.cse622.datadecoy.provider");
	Thread requestHandlerThread = null;


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		int val = database.deleteDecoyEntry(selectionArgs[0], selectionArgs[1]);
		return val;
		
	}
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		String app 	= values.getAsString(KEY_APPLICATION);
		String perm = values.getAsString(KEY_PERMISSION);
		int mock	= values.getAsInteger(KEY_DECOYTYPE);
		String trace = null;
		
		if (values.containsKey(DecoyDatabase.KEY_TRACE_PATH)){
			trace = values.getAsString(DecoyDatabase.KEY_TRACE_PATH);
		}
		
		long rowID = database.insertDecoyEntry(app, perm, mock, trace);
		
		Log.d("info","<" + values.getAsString(KEY_APPLICATION) + "," + values.getAsString(KEY_PERMISSION) +
															"," + values.getAsString(KEY_DECOYTYPE) + ">");
		Log.d("info","Inserted in row :"+rowID);
		System.out.println("");
		Uri _uri = ContentUris.withAppendedId(PROVIDER_URI, rowID);
		getContext().getContentResolver().notifyChange(_uri, null);

		return _uri;
	}
	@Override
	public boolean onCreate() {
		database = new DecoyDatabase(getContext());
		PermissionManager.getInstance().initialize(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final Cursor cursor = database.query(selectionArgs[0], selectionArgs[1]);
		return cursor;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//		String app 	= values.getAsString("KEY_APPLICATION");
//		String perm = values.getAsString("KEY_PERMISSION");
//		int mock	= values.getAsInteger("KEY_DECOYTYPE");
//		
//		return database.updateEntry(app, perm, mock);
		return -1;
	}
}