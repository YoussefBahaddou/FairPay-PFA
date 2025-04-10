package com.emsi.fairpay_maroc.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.utils.GeolocationHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapLog";
    
    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ProgressBar progressBar;
    private TextView selectedLocationText;
    private Button confirmButton;
    private Marker selectedMarker;
    private int selectedCityId = -1;
    private String selectedCityName = "";
    private List<Marker> markers = new ArrayList<>();
    private LatLng selectedLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (checkGooglePlayServices()) {
            // Initialize map
            mapView = findViewById(R.id.map_view);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        } else {
            Toast.makeText(this, "Google Play Services not available", Toast.LENGTH_SHORT).show();
        }

        // Initialize views
        progressBar = findViewById(R.id.progress_bar);
        selectedLocationText = findViewById(R.id.selected_location_text);
        confirmButton = findViewById(R.id.confirm_button);
        
        Button useLocationButton = findViewById(R.id.use_location_button);
        useLocationButton.setOnClickListener(v -> useCurrentLocation());
        
        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> confirmLocationSelection());
        }

        Log.d(TAG, "onCreate: Initializing MapView and FusedLocationProviderClient");

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }
      @Override
      public void onMapReady(@NonNull GoogleMap map) {
          googleMap = map;
          Log.d(TAG, "onMapReady: Google Map is ready");

          // Add markers for major cities
          addCityMarkers();

          // Enable map click listener
          googleMap.setOnMapClickListener(latLng -> {
              Log.d(TAG, "onMapClick: User clicked on the map at " + latLng.toString());
              selectedLatLng = latLng;

              // Get city name from coordinates
              getCityFromCoordinates(latLng);
          });

          // Get the user's current location
          getCurrentLocation();
      }

    private void addCityMarkers() {
        // Add markers for major cities with their database IDs
        addCityMarker(new LatLng(31.6295, -7.9811), "Marrakech", 1);
        addCityMarker(new LatLng(33.5731, -7.5898), "Casablanca", 2);
        addCityMarker(new LatLng(34.0209, -6.8416), "Rabat", 3);
        addCityMarker(new LatLng(34.0181, -5.0078), "Fes", 4);
        addCityMarker(new LatLng(35.7595, -5.8340), "Tangier", 5);
    }

    private void addCityMarker(LatLng position, String cityName, int cityId) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(cityName)
                .snippet("Tap to select")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        
        if (marker != null) {
            marker.setTag(cityId); // Store the city ID in the marker's tag
            markers.add(marker);
        }
        
        // Set up info window click listener to select the city
        googleMap.setOnInfoWindowClickListener(clickedMarker -> {
            if (clickedMarker.getTag() != null) {
                selectedCityId = (int) clickedMarker.getTag();
                selectedCityName = clickedMarker.getTitle();
                selectedLatLng = clickedMarker.getPosition();
                
                // Update UI
                if (selectedLocationText != null) {
                    selectedLocationText.setText(getString(R.string.selected_location, selectedCityName));
                    selectedLocationText.setVisibility(View.VISIBLE);
                }
                
                if (confirmButton != null) {
                    confirmButton.setEnabled(true);
                }
                
                // Update marker appearance
                if (selectedMarker != null) {
                    selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                }
                
                clickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                selectedMarker = clickedMarker;
                
                Toast.makeText(MapActivity.this, "Selected: " + selectedCityName, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getCityFromCoordinates(LatLng latLng) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        GeolocationHelper.getCurrentLocationAndCity(this, new GeolocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(LatLng location, String cityName, int cityId) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                selectedCityId = cityId;
                selectedCityName = cityName;
                
                if (selectedLocationText != null) {
                    selectedLocationText.setText(getString(R.string.selected_location, selectedCityName));
                    selectedLocationText.setVisibility(View.VISIBLE);
                }
                
                if (confirmButton != null) {
                    confirmButton.setEnabled(true);
                }
                
                Toast.makeText(MapActivity.this, "Selected location: " + cityName, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onLocationError(String error) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Toast.makeText(MapActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCurrentLocation() {
        Log.d(TAG, "getCurrentLocation: Checking location permissions");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "getCurrentLocation: Location permissions not granted, requesting permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        Log.d(TAG, "getCurrentLocation: Permissions granted, fetching last known location");

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Log.d(TAG, "getCurrentLocation: Location fetched successfully - Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());

                // Move the camera to the user's current location
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12));
                
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(currentLatLng)
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                
                if (marker != null) {
                    markers.add(marker);
                }
            } else {
                Log.w(TAG, "getCurrentLocation: Location is null");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "getCurrentLocation: Failed to fetch location", e));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Location permission granted");
                getCurrentLocation();
            } else {
                Log.w(TAG, "onRequestPermissionsResult: Location permission denied");
                Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void useCurrentLocation() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        GeolocationHelper.getCurrentLocationAndCity(this, new GeolocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(LatLng location, String cityName, int cityId) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                // Update the selected city
                selectedCityId = cityId;
                selectedCityName = cityName;
                selectedLatLng = location;
                
                // Update UI
                if (selectedLocationText != null) {
                    selectedLocationText.setText(getString(R.string.selected_location, selectedCityName));
                    selectedLocationText.setVisibility(View.VISIBLE);
                }
                
                if (confirmButton != null) {
                    confirmButton.setEnabled(true);
                }
                
                // Move camera to the location
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10f));
                
                // Clear previous markers
                if (selectedMarker != null) {
                    selectedMarker.remove();
                }
                
                // Add new marker
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(cityName)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                
                if (marker != null) {
                    selectedMarker = marker;
                    markers.add(marker);
                }
                
                Toast.makeText(MapActivity.this, "Nearest city: " + cityName, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onLocationError(String error) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Toast.makeText(MapActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void confirmLocationSelection() {
        if (selectedCityId != -1 && !selectedCityName.isEmpty()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("city_id", selectedCityId);
            resultIntent.putExtra("city_name", selectedCityName);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 
                1);
        } else {
            // Permissions already granted, initialize map
            if (googleMap != null) {
                try {
                    googleMap.setMyLocationEnabled(true);
                } catch (SecurityException e) {
                    Log.e("MapLog", "Error enabling location", e);
                }
            }
        }
    }    
      private boolean checkGooglePlayServices() {
          GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
          int result = googleApiAvailability.isGooglePlayServicesAvailable(this);
          if (result != ConnectionResult.SUCCESS) {
              if (googleApiAvailability.isUserResolvableError(result)) {
                  googleApiAvailability.getErrorDialog(this, result, 2404).show();
              }
              return false;
          }
          return true;
      }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();  // Add this method
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();  // Add this method
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}