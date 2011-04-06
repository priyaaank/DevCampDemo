package com.test.devcampdemo.helpers;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class LocationHelper {
	
	private LocationManager locationManager;
	
	public LocationHelper(Context context) {
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public Location getBestLastKnownLocation() {
		Location networkLocation =null, gpsLocation =null;
        if(isGPSSupported())
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(isNetworkSupported())
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        //if there are both values use the latest one
        if(gpsLocation != null && networkLocation != null){
            if(gpsLocation.getTime() > networkLocation.getTime())
                return gpsLocation;
            else
                return networkLocation;
        }

        //In case one of them was null then use the one that is not null
        if (gpsLocation != null) {
        	return gpsLocation;
        }
        
        if (networkLocation != null){
            return networkLocation;
        }
        
        return null;
	}
	
	public GeoPoint getBestLastKnownGeoPoint() {
		Location lastKnownLocation = getBestLastKnownLocation();
		if(lastKnownLocation != null) {
			Double longitudeE6 = new Double (lastKnownLocation.getLongitude() * 10E5);
			Double latitudeE6 = new Double (lastKnownLocation.getLatitude() * 10E5);
			Log.i(this.getClass().getName(), "Returning last known geopoint with cordinates as : Latitude/Longitude[" + latitudeE6+"/"+longitudeE6+"]" );
			return new GeoPoint(latitudeE6.intValue(), longitudeE6.intValue());
		}
		
		return null;
	}
	
	public boolean isGPSSupported() {
		boolean gpsSupported = false;
		try {
			gpsSupported = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch(Exception e) {
			Log.e("LocationUpdateTrigger", "Error occured while querying gps availability. " + e.getMessage());
		}
		return gpsSupported;
	}
	
	public boolean isNetworkSupported() {
		boolean networkSupported = false;
		try {
			networkSupported = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch(Exception e) {
			Log.e("LocationUpdateTrigger", "Error occured while querying network availability. " + e.getMessage());
		}
		
		return networkSupported;
	}

}
