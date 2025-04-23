package com.emsi.fairpay_maroc.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GeolocationHelper {
    private static final String TAG = "GeolocationHelper";

    public interface LocationCallback {
        void onLocationResult(GeoPoint location, String cityName, int cityId);
        void onLocationError(String error);
    }

    public static void getCurrentLocationAndCity(Context context, LocationCallback callback) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        // Try to get last known location first
        Location lastKnownLocation = null;
        
        // Try GPS provider first
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        
        // If GPS didn't work, try network provider
        if (lastKnownLocation == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (lastKnownLocation != null) {
            // We have a last known location, use it
            GeoPoint geoPoint = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            getCityNameFromLocation(context, geoPoint, callback);
        } else {
            // No last known location, request location updates
            try {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        getCityNameFromLocation(context, geoPoint, callback);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        // Try network provider if GPS is disabled
                        try {
                            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
                        } catch (Exception e) {
                            callback.onLocationError("Could not get location: " + e.getMessage());
                        }
                    }
                }, null);
            } catch (Exception e) {
                // If GPS provider failed, try network provider
                try {
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            getCityNameFromLocation(context, geoPoint, callback);
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            callback.onLocationError("Location providers are disabled");
                        }
                    }, null);
                } catch (Exception ex) {
                    callback.onLocationError("Could not get location: " + ex.getMessage());
                }
            }
        }
    }

    // Method to handle OSMdroid GeoPoint directly
    public static void getCityFromGeoPoint(Context context, GeoPoint geoPoint, LocationCallback callback) {
        getCityNameFromLocation(context, geoPoint, callback);
    }

    private static void getCityNameFromLocation(Context context, GeoPoint geoPoint, LocationCallback callback) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String cityName = address.getLocality();
                if (cityName == null) {
                    cityName = address.getSubAdminArea();
                }
                if (cityName == null) {
                    cityName = address.getAdminArea();
                }
                
                // Map common city names to our database IDs
                int cityId = getCityIdByName(cityName);
                
                callback.onLocationResult(geoPoint, cityName, cityId);
            } else {
                callback.onLocationError("Could not determine city from location");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address from location", e);
            callback.onLocationError("Error determining city: " + e.getMessage());
        }
    }

    // Helper method to map city names to IDs that match our database
    private static int getCityIdByName(String cityName) {
        // Normalize the city name (remove accents, lowercase)
        String normalizedName = cityName != null ? cityName.toLowerCase().trim() : "";
        
        // Map of city names to IDs (matching your database)
        Map<String, Integer> cityMap = new HashMap<>();
        cityMap.put("casablanca", 1);
        cityMap.put("marrakech", 2);
        cityMap.put("marrakesh", 2);
        cityMap.put("rabat", 4);
        cityMap.put("fes", 3);
        cityMap.put("fez", 3); // Alternative spelling
        cityMap.put("tangier", 5);
        cityMap.put("tanger", 5); // French spelling
        
        // Return the mapped ID or a default (you can change this logic)
        return cityMap.getOrDefault(normalizedName, 1); // Default to Casablanca (ID 1) if not found
    }
}
