package com.example.datadecoy.test;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;
import android.content.Context;
import android.util.Log;
import android.view.View;

public class DataDecoyTest extends Activity {

	LocationManager locationManager;
	LocationListener locationListener;
	TextView eTextView;
	double initLat;
	double initLong;
	double startLat;
	double startLong;
	double nextLat;
	double nextLong;
	double lastLat;
	double lastLong;
	Boolean flag= false; //flag to see if its the first coordinate
	Location startLocation;
	Location lastLocation;
	Location testMockdata = new Location("");
	String myProvider = LocationManager.GPS_PROVIDER;
	double distancecheck;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_decoy_test);
        
      //Error msg reference
        eTextView= (TextView)findViewById(R.id.textViewError);
        eTextView.setText("No errors to report, Getting GPS Data");
        
        //Acquire a reference to the system Location Manager
        locationManager= (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //Define a listener that responds to location updates
        locationListener = new LocationListener(){
        	public void onLocationChanged(Location location){
        		//Called when a new location is found by GPS
        		if(flag==false){ //If its the first location found.
        			Log.d("GPS tracking","First location found, storing it");
        	    	initLat= location.getLatitude();
        	    	initLong= location.getLongitude();
        	    	String tempLoc= "initial lat is " + initLat + " and initial long is " + initLong;
        	    	Log.d("GPS tracking",tempLoc);
        	        eTextView.setText(tempLoc);
        	    	flag=true;
        		}
        		if(flag==true){ // all other locations past the first reading
        			lastLocation= locationManager.getLastKnownLocation(myProvider);
        			lastLat= lastLocation.getLatitude();
        			lastLong= lastLocation.getLongitude();
        			Log.d("GPS tracking","New GPS reading");
                    nextLat= location.getLatitude();
        			nextLong= location.getLongitude();
        			//testMockdata.set(location);
        			testMockdata.setLatitude(43.003016122953184);
        			testMockdata.setLongitude(-78.78740608692169);
        			distancecheck= location.distanceTo(testMockdata);
        			//distancecheck=0.0;
        			String tempLoc= "last lat is " + lastLat + "and last long is " + lastLong + "\nnext lat is " + nextLat + " and next long is " + nextLong+"\ndistance bet. mock & davishall is "+ distancecheck;
        			eTextView.setText(tempLoc);
        			Log.d("GPS tracking",tempLoc);
        		}//End outer if
        	} //onLocationChanged method end
        	public void onStatusChanged(String provider, int status, Bundle extras){
        		String tempStatus= "The provider " + provider + " has a status of " + status;
        		Log.d("GPS tracking",tempStatus);
        	}
        	public void onProviderEnabled(String provider){
        		Log.d("GPS tracking", "GPS enabled and working.");
        	}
        	public void onProviderDisabled(String provider){
        		Log.d("GPS tracking", "GPS disabled for the moment."); //may be caused by interference!
        		eTextView.setText("Last GPS location failed.");
        	}       		
        };
        
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,2,locationListener);
    }//End of onCreate method

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_data_decoy_test, menu);
        return true;
    }
    
  //resets app. 
    public void resetTimer (View view) {
    	//Reset GPS -This prevents stale gps locations from being used!
    	locationManager.removeUpdates(locationListener);
    	Log.d("GPS tracking", "GPS should now be off!");
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,2,locationListener);
        eTextView.setText("No errors to report, Getting GPS data");
        flag= false; //gets a whole new first reading
    } //End reset method
} //end DataDecoyTest.java
