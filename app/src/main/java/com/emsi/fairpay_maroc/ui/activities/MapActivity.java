package com.emsi.fairpay_maroc.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.emsi.fairpay_maroc.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Log.d("MapLog", "onCreate: Initializing MapView and FusedLocationProviderClient");

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        Log.d("MapLog", "onMapReady: Google Map is ready");

        // Enable map click listener
        googleMap.setOnMapClickListener(latLng -> {
            Log.d("MapLog", "onMapClick: User clicked on the map at " + latLng.toString());

            // Add a marker at the clicked location
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Selected Area"));

            if (marker != null) {
                Log.d("MapLog", "onMapClick: Marker added at " + marker.getPosition().toString());
                Toast.makeText(this, "You have selected: " + marker.getTitle(), Toast.LENGTH_SHORT).show();
            } else {
                Log.e("MapLog", "onMapClick: Failed to add marker");
            }
        });

        // Get the user's current location
        getCurrentLocation();
    }

    private void getCurrentLocation() {
        Log.d("MapLog", "getCurrentLocation: Checking location permissions");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("MapLog", "getCurrentLocation: Location permissions not granted, requesting permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        Log.d("MapLog", "getCurrentLocation: Permissions granted, fetching last known location");

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Log.d("MapLog", "getCurrentLocation: Location fetched successfully - Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());

                // Move the camera to the user's current location
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12));
                googleMap.addMarker(new MarkerOptions()
                        .position(currentLatLng)
                        .title("Your Location"));
            } else {
                Log.w("MapLog", "getCurrentLocation: Location is null");
            }
        }).addOnFailureListener(e -> Log.e("MapLog", "getCurrentLocation: Failed to fetch location", e));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MapLog", "onRequestPermissionsResult: Location permission granted");
                getCurrentLocation();
            } else {
                Log.w("MapLog", "onRequestPermissionsResult: Location permission denied");
                Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MapLog", "onResume: Resuming MapView");
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MapLog", "onPause: Pausing MapView");
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MapLog", "onDestroy: Destroying MapView");
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("MapLog", "onSaveInstanceState: Saving MapView state");
        mapView.onSaveInstanceState(outState);
    }
}