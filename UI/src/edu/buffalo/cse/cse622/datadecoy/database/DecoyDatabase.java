package edu.buffalo.cse.cse622.datadecoy.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
/*
 * Currently, the database never uses the update functionality although it is implemented. 
 * Instead the insert method incorporates the update scenario
 */

public class DecoyDatabase {
	// This separate one will be implemented later 
	public static final String KEY_TRACE_TIMES = "times";
	
	public static final String KEY_TRACE_PATH = "trace";
    public static final String KEY_DECOYTYPE 	= "decoyType";
    public static final String KEY_PERMISSION 	= "permission";
    public static final String KEY_APPLICATION 	= "application";
    public static final String KEY_ROWID 		= "_id";
    public static final String DATABASE_TABLE 	= "DataDecoy";
    private static File dbFile					= new File("./data/data/edu.buffalo.cse.cse622.datadecoy/databases/db.txt");
    
//    Made the enum class directly usable within code. Eric, do change this if some changes I made are unnecessary
    public enum DecoyType{
    	MOCK_DEFAULT("0"),
    	MOCK_AUTO("1"),
    	MOCK_MANUAL("2");
    	private final String type;
    	
    	DecoyType(String type) {
    		this.type = type;
    	}
    	
    	public String getType() {
    		return type;
    	}
    	
    	@Override
    	public String toString() {
    		return type;
    	}
    }
    
    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement,
     * the only nullable field is the trace, as automatically generated field will not need one.
     */
    private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + KEY_APPLICATION + " TEXT NOT NULL, " + 
    						KEY_PERMISSION +" TEXT NOT NULL," + KEY_DECOYTYPE + " INTEGER NOT NULL, " + 
    						KEY_TRACE_PATH + " TEXT," + "PRIMARY KEY(application, permission));";

    private static final String DATABASE_NAME = "database";
    private static final int DATABASE_VERSION = 4;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
            if(!dbFile.exists())
				try {
					dbFile.createNewFile();
					Log.d("MOD", dbFile.getName() + " created successfully!");
					LocationGenerator.genAutoMock();
					Log.d("MOD", "auto-mock.txt generated successfully");
				} catch (IOException e) {
					Log.e("MOD", "Error creating " + dbFile.getName() + "\n" + e.getMessage());
				}
            try {
//            	It is important to chmod any file that we create within the application to make it accessible by the LocationManager
				Process p = Runtime.getRuntime().exec("chmod 777 ./data/data/edu.buffalo.cse.cse622.datadecoy/databases/database");
				p.waitFor();
				p		  = Runtime.getRuntime().exec("chmod 666 ./data/data/edu.buffalo.cse.cse622.datadecoy/databases/db.txt");
				p.waitFor();
				//  Add separate files to this folder.
				//  Filename will be appname-mock.txt, then place trace in the file.
				p		  = Runtime.getRuntime().exec("chmod 666 ./data/data/edu.buffalo.cse.cse622.datadecoy/databases/auto-mock.txt");
				p.waitFor();
			} catch (IOException e) {
				Log.e("MOD","IOException :" + e.getMessage());
			}
            //eventually can set up the foreign databases here if used
            catch (InterruptedException e) {
				Log.e("MOD","InterruptedException :" + e.getMessage());
			}
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
    
    public DecoyDatabase(Context ctx) {
        this.mCtx = ctx;
        open();
    }

    public SQLiteDatabase open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return mDb;
    }

    /**
     * Deletes an entry from the database.
     * @param app Application Name
     * @param perm Corresponding Permission
     * @return number of rows affected by the delete (Should ideally be 1)
     */
    public int deleteDecoyEntry(String app, String perm) {
    	
    	
    	//  We can't simply delete the bad line if it exists?
    	//  To speed things up we might want to use a regular expression (time ending).
    	//  Also, StringBuilders are usually better than writers, and synchronization shouldn't be
    	//  needed here.
    	BufferedReader in 		= null;
    	try {
    		//delete the file as well
    		in					= new BufferedReader(new FileReader(dbFile));
    		StringWriter writer	= new StringWriter();
    		String line 		= null;
    		while( (line = in.readLine()) != null) {
    			if(!(line.contains(app) && line.contains(perm)))
    				writer.append(line + System.getProperty("line.separator"));
    		}
    		BufferedWriter out 	= new BufferedWriter(new FileWriter(dbFile));
    		out.write(writer.toString());
			out.close();
			
			String appFile = "./data/data/edu.buffalo.cse.cse622.datadecoy/databases/" + app + "-" + perm + ".txt";
			File file = new File(appFile);
			if (file.exists()) {
				boolean retBln = file.delete();
				if (retBln = false){
					Log.e("MOD", "Could not delete file " + appFile);
				}
			}
			
		} catch (IOException e) {
			Log.e("MOD", "IOException while deleting row from " + dbFile.getName() + "\n" + e.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				Log.e("MOD","IOException while closing BufferedReader in deleteDecoyEntry! " + e.getMessage());
			}
		}
        int result = mDb.delete(DATABASE_TABLE, KEY_APPLICATION + "=\"" + app + "\" AND " + KEY_PERMISSION + "=\"" + perm + "\"", null);
        return result;
        
    }
    
    /**
     * Create a new decoy entry using the app and permission provided. If the decoy is
     * successfully entered return the new rowId for that decoy, otherwise return
     * a -1 to indicate failure.
     * 
     * @param app the title of the application
     * @param perm the permission used by the application
     * @return rowId or -1 if failed
     */
    public long insertDecoyEntry(String app, String perm, int mock, String trace) {
        BufferedReader in 	= null;
        BufferedWriter out	= null;
        
        //  Why are we doing two separate ways of manipulating the file?
        //  One of them should be provably better...
        try {
        	//create/overwrite the file
        	in 	= new BufferedReader(new FileReader(dbFile));
            
            String line 	= null;
            String output 	= new String(); 
            while ( (line = in.readLine()) != null) {
            	if(!(line.contains(app) && line.contains(perm)))
            		output += line + System.getProperty("line.separator");
            }
            out	= new BufferedWriter(new FileWriter(dbFile));
        	output 		   += app + "\t\t\t" + perm + "\t\t" + mock + "\t" + trace + System.getProperty("line.separator");
        	out.write(output);
        	//should always close out of a writer
        	out.close();
        	
			try{
				String appFile = "./data/data/edu.buffalo.cse.cse622.datadecoy/databases/" + app + "-" + perm + ".txt";
				File file = new File(appFile);
				if (!file.exists()) {
				        try {
				            file.createNewFile();
				            Process p = Runtime.getRuntime().exec("chmod 666 " + appFile);
							p.waitFor();
				        } catch (IOException e) {
				            e.printStackTrace();
				            if(file.exists()){
				            	file.delete();
				            }
				        }
				}
				if(trace != null && file.exists()){
					FileWriter fileW = new FileWriter(file);
					out	= new BufferedWriter(fileW);
					out.write(trace);
					out.close();
					
				}
				
				
			}
			catch(Exception ex){
				
			}
		} catch (IOException e) {
			Log.e("MOD","IOException occured while inserting/updating entry\n" + e.getMessage());
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				Log.e("MOD","IOException while closing " + dbFile.getName() + " output stream\n" + e.getMessage());
			}
		}
        
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_APPLICATION, app);
        initialValues.put(KEY_PERMISSION, perm);
        initialValues.put(KEY_DECOYTYPE, mock);
        initialValues.put(KEY_TRACE_PATH, trace);
       
        long id =  mDb.insertWithOnConflict(DATABASE_TABLE, null, initialValues,SQLiteDatabase.CONFLICT_REPLACE);
        return id;
    }
    
    /**
     * Update the app permission using the details provided. The permission to be updated is
     * specified using the application name and permission name, and it is altered to use the title and body
     * values passed in
     * 
     * @param app name of string to update
     * @param perm name to permission to update 
     * @param mock type of mock to change to
     * @return true if the app was successfully updated, false otherwise
     */
    /*
    public int updateEntry(String app, String perm, int mock) {
        ContentValues args = new ContentValues();
        args.put(KEY_DECOYTYPE, mock);

        return mDb.update(DATABASE_TABLE, args, KEY_APPLICATION + "=" + app + " AND " + KEY_PERMISSION + "=" + perm , null);
        
    }
    */
    
    /**Queries the database for the given application name and permission
     * Note: The primary key of the table is a combination of application name and permission string.
     * 
     * @param app Application name to query
     * @param perm Permission 
     * @return Cursor to the results of the query.
     * Note: You need to check for null and use cursor.moveToFirst() before accessing data from the cursor.
     */
    public Cursor query(String app, String perm) {
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder(); 
        qBuilder.setTables(DATABASE_TABLE);
        qBuilder.appendWhere(KEY_APPLICATION + "= \"" + app + "\" AND " + KEY_PERMISSION + "=\"" + perm + "\"");
        final Cursor cursor = qBuilder.query(mDb, null, null, null, null, null, null);
        return cursor;
    }
    
    public void close() {
    	mDb.close();
    }
}
