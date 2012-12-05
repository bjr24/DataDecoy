package edu.buffalo.cse.cse622.datadecoy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapContainer extends MapActivity {

	public static String RESULT_KEY = "locations";
	private AlertDialog quit;
	private AlertDialog addressQuestion;
	private CustomMapView mapView;
	private Button mFinishedButton;
	private Button mAddAddressButton;
	private MapOverlay itemOverlay;
	
	private Context mContext;
	private String enteredAddress;
	private final int ZOOM = 10;
	private ArrayList<String> trace;
	 
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_maptest);
       mContext = this;
       
       mapView = (CustomMapView)findViewById(R.id.Mapview);
       mapView.getController().setCenter(new GeoPoint(43000786, -78789696));
       mapView.getController().setZoom(ZOOM);
       
       mFinishedButton = (Button)findViewById(R.id.btnFinished);
       mAddAddressButton = (Button)findViewById(R.id.btnAddByAddress);
       
       //faster setup (since its constant, set only once)
       CreateQuitAlert();
       CreateAddressAlert();
       
       AddButtonFunctionality();
       
       trace = new ArrayList<String>();
       
       MapSetup();
     
   }
   
   private void AddButtonFunctionality() {
	   mFinishedButton.setOnClickListener(new View.OnClickListener() {
		
		   	@Override
			public void onClick(View v) {
		   		// Close application (return to the caller)
				//set Data return Activity B at anywhere you want
				Bundle locations = new Bundle();
				locations.putStringArrayList(RESULT_KEY, trace);
				
				Intent data = new Intent();
				data.putExtras(locations);

				setResult(RESULT_OK,data);
				//close the activity
				finish();
			
			}
	   });
	   
	   mAddAddressButton.setOnClickListener(new View.OnClickListener() {
			
		   	@Override
			public void onClick(View v) {
		   		addressQuestion.show();
			
			}
	   });
	
   }

   private void MapSetup(){
       mapView.setOnLongpressListener(new CustomMapView.OnLongpressListener() {
          	public void onLongpress(final MapView view, final GeoPoint point) {
          		runOnUiThread(new Runnable() {
          				public void run() {
          					Geocoder geoCoder = new Geocoder(
          			        		mContext, Locale.ENGLISH);
          			        try {
          			            List<Address> addresses = geoCoder.getFromLocation(
          			                point.getLatitudeE6()  / 1E6, 
          			                point.getLongitudeE6() / 1E6, 1);

          			            String address = "";
          			            // Grabs the address of the GeoPoint, if it exists
          			            if (addresses.size() > 0) 
          			            {
          			                for (int i=0; i<addresses.get(0).getMaxAddressLineIndex(); 
          			                     i++)
          			                   address += addresses.get(0).getAddressLine(i) + "\n";
          			            }
          			            
          			            // If for some reason it doesn't, just display the lat/long
          			            if(address.matches("\\s++")){
          			            	address = "Latitude of the point is: " + (double)(point.getLatitudeE6()/1E6) + "\n";
          			            	address += "Longitude of the point is: " + (double)(point.getLongitudeE6()/1E6) + "\n";
          			            }
          			            
          			         final String addr = address;
          			         
          			         AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
          			         // Make sure they want this location
          			         builder.setMessage("Add Location close to " + address).
          			         setPositiveButton("Add", new DialogInterface.OnClickListener() {

   									@Override
   									public void onClick(DialogInterface arg0, int arg1) {
   										// Add location to list to send back to caller
   										trace.add(String.valueOf((double)point.getLatitudeE6()/1E6) + "\t" + String.valueOf((double)point.getLongitudeE6()/1E6));
   										OverlayItem overlayItem = new OverlayItem(point, "", addr);
   										
   										//  Prevents any lingering errors from the itemOverlay
   										if(itemOverlay == null){
   											List<Overlay> mapOverlays = mapView.getOverlays();
   										       Drawable drawable = mContext.getResources().getDrawable(R.drawable.arrow_down);
   										       
   										       itemOverlay = new MapOverlay(drawable, mContext);
   										           
   										     //finally adds it to the list of mapOverlays
   										       mapOverlays.add(itemOverlay);
   										}
   										itemOverlay.addOverlay(overlayItem);
   										
   										//  Refresh the mapView right away
   										runOnUiThread(new Runnable() {
   											 public void run() {            
   											   mapView.postInvalidate();
   											  }
   										});
   										//quit.show();
   									}
   			                 }).
          			         setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

   									@Override
   									public void onClick(DialogInterface arg0, int arg1) {
   										// TODO Auto-generated method stub
   										//See if there might be any reason that
   										//this code should do something.
   										//quit.show();
   									}
          			         });
          			         AlertDialog notify = builder.create();
          			         notify.show();
          			         
          			         // Test to see what lat/long are like
          			         //add = view.getMapCenter().getLatitudeE6()/1E6 + " " + view.getMapCenter().getLongitudeE6()/1E6;
          			         //Toast.makeText(mContext, add, Toast.LENGTH_SHORT).show();
          			        }
          			        catch (IOException e) {                
          			            e.printStackTrace();
          			        }  
          				}
          		});
          	}
          });
   }
   
   public void CreateQuitAlert(){
       AlertDialog.Builder quitBuilder = new AlertDialog.Builder(mContext);
       // See if they want to add more
       quitBuilder.setMessage("Add more locations?").
	         	setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						//anything to them continuing, maybe
						//showing locations they have added so far.
					}
        }).
        setNegativeButton("Finished", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// Close application (return to the caller)
						//set Data return Activity B at anywhere you want
						Bundle locations = new Bundle();
						locations.putStringArrayList(RESULT_KEY, trace);
						
						Intent data = new Intent();
						data.putExtras(locations);

						setResult(RESULT_OK,data);
						//close the activity
						finish();
					}
        });
	         
       quit = quitBuilder.create();
   }

   public void CreateAddressAlert(){
	   
	   AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	    // Get the layout inflater
	    LayoutInflater inflater = LayoutInflater.from(mContext);

	    final View addressEntryView = inflater.inflate(R.layout.address_dialog, null);
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(addressEntryView)
	    // Add action buttons
	           .setPositiveButton("Use Address", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	    EditText mAddressReader = (EditText) addressEntryView.findViewById(R.id.txtAddress);
	            	   enteredAddress = mAddressReader.getText().toString();
	   		   		if(enteredAddress != null){
			   			Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());    
			   	        try {
			   	            List<Address> addresses = geoCoder.getFromLocationName(
			   	                enteredAddress, 5);
			   	            
			   	            if (addresses.size() > 0) {
			   	            	
			   	            	GeoPoint point = new GeoPoint(
			                         (int) (addresses.get(0).getLatitude() * 1E6), 
			                         (int) (addresses.get(0).getLongitude() * 1E6));
			   	            	
			   	            	// Add location to list to send back to caller
								trace.add(String.valueOf((double)point.getLatitudeE6()/1E6) + "\t" + String.valueOf((double)point.getLongitudeE6()/1E6));
			   	            	OverlayItem overlayItem = new OverlayItem(point, "", enteredAddress);
			   	            	
			   	            	//  Prevents any lingering errors from the itemOverlay
								if(itemOverlay == null){
									List<Overlay> mapOverlays = mapView.getOverlays();
									Drawable drawable = mContext.getResources().getDrawable(R.drawable.arrow_down);
									       
									itemOverlay = new MapOverlay(drawable, mContext);
									           
									//finally adds it to the list of mapOverlays
									mapOverlays.add(itemOverlay);
								}
								itemOverlay.addOverlay(overlayItem);
									
			   	            }    
			   	        } catch (IOException e) {
			   	        	
			   	        }
			   		}
			   		enteredAddress = null;
	               }
	           })
	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                   enteredAddress = null;
	               }
	           });     
	    
	    addressQuestion =  builder.create();
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.main, menu);
       return true;
   }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
