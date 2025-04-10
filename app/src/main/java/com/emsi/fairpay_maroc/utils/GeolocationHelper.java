package com.emsi.fairpay_maroc.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeolocationHelper {
    private static final String TAG = "GeolocationHelper";

    public interface LocationCallback {
        void onLocationResult(LatLng location, String cityName, int cityId);
        void onLocationError(String error);
    }

    public static void getCurrentLocationAndCity(Context context, LocationCallback callback) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        getCityNameFromLocation(context, latLng, callback);
                    } else {
                        callback.onLocationError("Could not get current location");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting location", e);
                    callback.onLocationError("Error getting location: " + e.getMessage());
                });
    }

    private static void getCityNameFromLocation(Context context, LatLng latLng, LocationCallback callback) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String cityName = address.getLocality();
                if (cityName == null) {
                    cityName = address.getSubAdminArea();
                }
                if (cityName == null) {
                    cityName = address.getAdminArea();
                }
                
                // For demo purposes, we'll use a dummy city ID
                // In a real app, you would query your database to get the actual city ID
                int cityId = Math.abs(cityName.hashCode()) % 100; // Just a dummy ID
                
                callback.onLocationResult(latLng, cityName, cityId);
            } else {
                callback.onLocationError("Could not determine city from location");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address from location", e);
            callback.onLocationError("Error determining city: " + e.getMessage());
        }
    }
}