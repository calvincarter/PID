package com.example.com.pid.test;

import gps.GPS;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pid.PIDControl;


import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// Instantiate Textviews
	TextView tv_wp_start;
	TextView tv_wp_end;
	TextView tv_error;
	TextView tv_pidHeading;
	TextView tv_desired_course;
	TextView tv_current_course;
	TextView tvLat;
	TextView tvLon;
	TextView tv_distance_from_end_wp;
	
	// Instantiate Buttons
	Button bt_wp_start;
	Button bt_wp_end;
	Button bt_myLocation;
	Button bt_manual_stop;
	
	// Instantiate GPS Data
	LocationManager locationManager;
	Criteria criteria;
	String bestProvider;
	Location location;
	LocationListener loc_listener;
	
	// Instantiate Phone Latitude and Longitude
	// These will be the actual values from the GPS
	double lat;
	double lon;
	
	// Instantiate Start, End Wavepoints
	// also User current location
	static double start_wp_lat;
	static double start_wp_lon;
	static double end_wp_lat;
	static double end_wp_lon;
	static double myCurrentLat;
	static double myCurrentLon;
	
	// Instantiate desired course bearing,
	// current course bearing, crosstrack error,
	// and PID Heading
	static float desireCourseBearing;
	static float currentCourseBearing;
	static int crossTrackError;
	static int pidHeading;
	static float distanceFromEndWaivePoint;
	
	// Verify that both start and end 
	// wavepoints have been recorded
	boolean AreAllTheWavepointsRecorded = false;
	
	// Confirm that 1 wavepoint has been recorded
	boolean OneWavepointIsAlreadyRecorded = false;
	
	// User will set to true if they press the manual
	// stop button
	boolean ManualStop = false;
	
	GPS gps = new GPS();
	PIDControl pc = new PIDControl();
	
	public static ExecutorService pool = Executors.newCachedThreadPool();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        // Set up Text Views
        tv_wp_start = (TextView) findViewById(R.id.tv_startwp);
        tv_wp_end = (TextView) findViewById(R.id.tv_endwp);
        tv_error = (TextView) findViewById(R.id.tv_error);
        tv_pidHeading = (TextView) findViewById(R.id.tv_pidHeading);
        tv_desired_course = (TextView) findViewById(R.id.tv_desiredCourse);
        tv_current_course = (TextView) findViewById(R.id.tv_currentCourse);
        tvLat = (TextView) findViewById(R.id.txtLat);
        tvLon = (TextView) findViewById(R.id.txtLon);
        tv_distance_from_end_wp = (TextView) findViewById(R.id.tv_distance_from_end_wp);
    	
        // Set up buttons and initiate their onclick listeners
        bt_wp_start = (Button) findViewById(R.id.bt_startwp);
        bt_wp_end = (Button) findViewById(R.id.bt_endwp);
        bt_myLocation = (Button) findViewById(R.id.bt_myLocation);
        bt_manual_stop = (Button) findViewById(R.id.bt_stop);
              
        bt_wp_start.setOnClickListener(wp_startHandler);
        bt_wp_end.setOnClickListener(wp_endHandler);
        bt_myLocation.setOnClickListener(wp_mylocationHandler);
        bt_manual_stop.setOnClickListener(wp_manualstopHandler);
        
        // Run Phone GPS to get latitude and longitude
        // GPS code done by: Jose Lopez
        gpsSetup();

    
    }
    
    /** WE'LL NEED TO TAKE THAT WHILE LOOP THATS RUNNING IN THE 
     * wp_mylocationHandler AND INSERT IT HERE IN THIS THREAD
     */
    class PIDLoop implements Runnable {
    	
    	public void run() {
    		
    		while(true) {
	
    		}
    	}
    }
    
    // Record the Start Wavepoint and get desired course only if
    // the end wavepoint has already been determined
    View.OnClickListener wp_startHandler = new View.OnClickListener() {
        public void onClick(View v) {
           
        	// Take the Phone latitude and longitude values
        	// and store them in start wavepoint
        	start_wp_lat = lat;
        	start_wp_lon = lon;
        	
        	// Display the latitude and longitude to the start wavepoint textview
        	tv_wp_start.setText("lat: " + String.valueOf(start_wp_lat) +", " + "lon: " + String.valueOf(start_wp_lon));
           
        	// Set the desired course by passing in the start and end wavepoints
        	// only if the end wavepoint has already been recorded
        	if(OneWavepointIsAlreadyRecorded) {        		
        		// all the wavepoints have been recorded now get desired course
        		AreAllTheWavepointsRecorded = true;
        		
        		desireCourseBearing = gps.get_gps_course((int)start_wp_lat, (int)start_wp_lon, (int)end_wp_lat, (int)end_wp_lon);
        		tv_desired_course.setText(String.valueOf(desireCourseBearing));
        	}
        	else
        		// else then record the start wavepoint
        		OneWavepointIsAlreadyRecorded = true;
        }
      };
      
      // Record the End Wavepoint and get desired course only if
      // the start wavepoint has already been determined
      View.OnClickListener wp_endHandler = new View.OnClickListener() {
          public void onClick(View v) {
        	
          	// Take the Phone latitude and longitude values
          	// and store them in end wavepoint
        	end_wp_lat = lat;
        	end_wp_lon = lon;
        	
        	// Display the latitude and longitude to the end wavepoint textview
        	tv_wp_end.setText("lat: " + String.valueOf(end_wp_lat) +", " + "lon: " + String.valueOf(end_wp_lon));
              
			// Set the desired course by passing in the start and end wavepoints
			// only if the start wavepoint has already been recorded
			if(OneWavepointIsAlreadyRecorded) {
				
        		// all the wavepoints have been recorded now get desired course
        		AreAllTheWavepointsRecorded = true;
        		
        		desireCourseBearing = gps.get_gps_course((int)start_wp_lat, (int)start_wp_lon, (int)end_wp_lat, (int)end_wp_lon);
				tv_desired_course.setText(String.valueOf(desireCourseBearing));
			}
			else
				// else then record the end wavepoint
				OneWavepointIsAlreadyRecorded = true;
              
          }
        };
        
        // Now at this point the desired course should already be determined
        // now begin to run algorithm and user should begin to navigate to 
        // end wavepoint
        View.OnClickListener wp_mylocationHandler = new View.OnClickListener() {
            
        	public void onClick(View v) {
        		
        		if(AreAllTheWavepointsRecorded) {
        		
	                myCurrentLat = lat;
	                myCurrentLon = lon;
	         		 
	         		// Get the distance user is from waivepoint
	                //double LatdistanceFromEndWavePoint = Math.abs(myCurrentLat - end_wp_lat);
	                //double LondistanceFromEndWavePoint = Math.abs(myCurrentLon - end_wp_lon);
	                
	         		distanceFromEndWaivePoint = gps.get_gps_dist((float)myCurrentLat, (float)myCurrentLon, (float)end_wp_lat, (float)end_wp_lon);
	         		
	          		// Running on UI
	         		 runOnUiThread(new Runnable(){
	         			@Override
	         			public void run() {
	                      	// Display the current latitude and longitude of the user
	         				tv_distance_from_end_wp.setText(String.valueOf(distanceFromEndWaivePoint));
	         			}
	         		});
	         		
	         		
	                // The following while loop will need to be on a separate thread
	                // possible use a ExecutorService thread
	                // pool.execute(new PIDLoop())
	                
	                // Keep Running PID Algorithm until the user has issued a manual stop
	                // or until user is near .1 distance of the end waivepoint
	                while(distanceFromEndWaivePoint > .1 && ManualStop == false) {
	
	                	currentCourseBearing = gps.get_gps_course((int) myCurrentLat, (int) myCurrentLon, (int)end_wp_lat, (int)end_wp_lon);
	                	crossTrackError = pc.compass_error((int)desireCourseBearing, (int) currentCourseBearing);
	                	pidHeading = pc.PID_heading(crossTrackError);
	                	
	              		// running on UI
	             		 runOnUiThread(new Runnable(){
	             			@Override
	             			public void run() {
		                       	tv_current_course.setText(String.valueOf(currentCourseBearing));
		                       	tv_error.setText(String.valueOf(crossTrackError));
		                       	tv_pidHeading.setText(String.valueOf(pidHeading));
	             			}
	             		});
	             		 
	             		 
	                     // if manual stop has not been initiated by user then
	                     // the let the user know that PID algorithm is done processing
	                     // because user found their location
	                     if(ManualStop == false)
	     	               	// User is at the wavepoint. Display an alert toast for a few seconds
	     	            	Toast.makeText(getApplicationContext(), "You Found the Location", Toast.LENGTH_SHORT).show();
	                	
	                }	// End of While Loop. Note: We'll need to move it to a thread
        		}
        		else // User needs to have a start and end wavepoint setup in order to run PID algorithm
 	            	Toast.makeText(getApplicationContext(), "You need to have a start and end wavepoint setup in order to run PID algorithm", Toast.LENGTH_SHORT).show();
            }
          };
          
          // Manually stop running PID algorithm and Stop GPS Listener
          View.OnClickListener wp_manualstopHandler = new View.OnClickListener() {
              public void onClick(View v) {
            	// User is at the wavepoint. Display an alert toast for a few seconds
	            Toast.makeText(getApplicationContext(), "Preparing Manual Stop", Toast.LENGTH_SHORT).show();  
            	  
            	// set manual stop to true. This will also stop
            	// PID algorithm from running
            	ManualStop = true;
                
            	// Stop GPS 
            	locationManager.removeUpdates(loc_listener);
              }
            };
 
            
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void gpsSetup(){
    	
  		tvLat = (TextView) findViewById(R.id.txtLat);
  		tvLon = (TextView) findViewById(R.id.txtLon);
  		
  		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
  		
  		//check if the gps is enabled. if not ask the user to enable it.
  		boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
  		if(!enabled){
  			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
  			startActivity(intent);
  		}
  		
  		//setting the criteria to get the location
  		criteria = new Criteria();
  		criteria.setAccuracy(Criteria.ACCURACY_FINE);
  		criteria.setAltitudeRequired(false);
  		criteria.setBearingRequired(false);
  		criteria.setCostAllowed(false);
  		criteria.setSpeedRequired(false);
  		//criteria.setPowerRequirement(Criteria.POWER_LOW);
  		
  		//adding criteria and best providers
  		bestProvider = locationManager.getBestProvider(criteria, false);
  		location = locationManager.getLastKnownLocation(bestProvider);
  		
  		//location changed listeners
  		loc_listener = new LocationListener(){
  			@Override
  			public void onLocationChanged(Location l){
  				try{
  					lat = l.getLatitude();
  					lon = l.getLongitude();
  					//Log.e("GPS", "Location changed: lat=" + String.valueOf(lat) + " lon=" + String.valueOf(lon));
  					
  				} catch(NullPointerException e){
  					lat = -1.0;
  					lon = -1.0;
  					Log.e("GPS Listener Exception", "" + e.getMessage());
  				}
  				
  				runOnUiThread(new Runnable(){
  					@Override
  					public void run(){
  						tvLat.setText("Lat:" + String.valueOf(lat) + " ");
  						tvLon.setText("Lon:" + String.valueOf(lon) + " ");
  					}
  				});
  			}
  			
  			@Override
  			public void onProviderEnabled(String p){}
  			
  			@Override
  			public void onProviderDisabled(String p){}

  			@Override
  			public void onStatusChanged(String provider, int status, Bundle extras){}
  		};
  		
  		//adding listeners
  		locationManager.requestLocationUpdates(bestProvider, 0, 0, loc_listener);
  		
  		//getting the last known location as the initial value
  		location = locationManager.getLastKnownLocation(bestProvider);
  		try{
  			lat = location.getLatitude();
  			lon = location.getLongitude();
  			
  		} catch(NullPointerException e){
  			lat = -1.0;
  			lon = -1.0;
  			Log.e("GPS Initial Location Exception", "" + e.getMessage());
  		}
  		
  		
  		//running on UI
  		runOnUiThread(new Runnable(){
  			@Override
  			public void run(){
  				tvLat.setText("Lat:" + lat + " ");
  				tvLon.setText("Lon:" + lon + " ");
  			}
  		});
  	}
    
	@Override
	protected void onResume(){
		super.onResume();
		locationManager.requestLocationUpdates(bestProvider, 0, 0, loc_listener);
	}
    
	@Override
	protected void onPause(){
		super.onPause();
		locationManager.removeUpdates(loc_listener);
	}
    
}
