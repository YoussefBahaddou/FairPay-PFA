package com.emsi.fairpay_maroc.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.utils.GeolocationHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapLog";
    private static final int PERMISSION_REQUEST_CODE = 1;
    
    private MapView mapView;
    private ProgressBar progressBar;
    private TextView selectedLocationText;
    private Button confirmButton;
    private Marker selectedMarker;
    private int selectedCityId = -1;
    private String selectedCityName = "";
    private List<Marker> markers = new ArrayList<>();
    private GeoPoint selectedGeoPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize OSMdroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        
        setContentView(R.layout.activity_map);

        // Request permissions
        requestPermissionsIfNecessary(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        });

        // Initialize views
        progressBar = findViewById(R.id.progress_bar);
        selectedLocationText = findViewById(R.id.selected_location_text);
        confirmButton = findViewById(R.id.confirm_button);
        
        Button useLocationButton = findViewById(R.id.use_location_button);
        useLocationButton.setOnClickListener(v -> useCurrentLocation());
        
        if (confirmButton != null) {
            confirmButton.setOnClickListener(v -> confirmLocationSelection());
        }

        // Initialize map
        mapView = findViewById(R.id.map_view);
        setupMap();
    }
    
    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        
        // Set initial map position to Morocco
        IMapController mapController = mapView.getController();
        mapController.setZoom(6.0);
        GeoPoint startPoint = new GeoPoint(31.7917, -7.0926); // Morocco center
        mapController.setCenter(startPoint);
        
        // Add markers for major cities
        addCityMarkers();
        
        // Set up map click listener
        setupMapClickListener();
    }

    private void setupMapClickListener() {
        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                selectedGeoPoint = p;
                getCityFromCoordinates(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapView.getOverlays().add(mapEventsOverlay);
    }

    private void addCityMarker(GeoPoint position, String cityName, int cityId) {
        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        marker.setTitle(cityName);
        marker.setSnippet("Tap to select");
        marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location_blue));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        
        // Store the city ID in the marker's related object
        marker.setRelatedObject(cityId);
        
        // Set up info window click listener
        marker.setOnMarkerClickListener((marker1, mapView) -> {
            if (marker1.isInfoWindowShown()) {
                marker1.closeInfoWindow();
            } else {
                marker1.showInfoWindow();
                
                // Update selected city
                selectedCityId = (int) marker1.getRelatedObject();
                selectedCityName = marker1.getTitle();
                selectedGeoPoint = marker1.getPosition();
                
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
                    selectedMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location_blue));
                }
                
                marker1.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location_red));
                selectedMarker = marker1;
                
                Toast.makeText(MapActivity.this, "Selected: " + selectedCityName, Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        
        mapView.getOverlays().add(marker);
        markers.add(marker);
    }
    
    private void getCityFromCoordinates(GeoPoint geoPoint) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        GeolocationHelper.getCityFromGeoPoint(this, geoPoint, new GeolocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(GeoPoint location, String cityName, int cityId) {
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

    private void useCurrentLocation() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        GeolocationHelper.getCurrentLocationAndCity(this, new GeolocationHelper.LocationCallback() {
            @Override
            public void onLocationResult(GeoPoint location, String cityName, int cityId) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                // Update the selected city
                selectedCityId = cityId;
                selectedCityName = cityName;
                selectedGeoPoint = location;
                
                // Update UI
                if (selectedLocationText != null) {
                    selectedLocationText.setText(getString(R.string.selected_location, selectedCityName));
                    selectedLocationText.setVisibility(View.VISIBLE);
                }
                
                if (confirmButton != null) {
                    confirmButton.setEnabled(true);
                }
                
                // Move camera to the location
                IMapController mapController = mapView.getController();
                mapController.animateTo(selectedGeoPoint);
                mapController.setZoom(12.0);
                
                // Clear previous markers
                if (selectedMarker != null) {
                    mapView.getOverlays().remove(selectedMarker);
                }
                
                // Add new marker
                Marker marker = new Marker(mapView);
                marker.setPosition(selectedGeoPoint);
                marker.setTitle(cityName);
                marker.setIcon(ContextCompat.getDrawable(MapActivity.this, R.drawable.ic_location_red));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                
                mapView.getOverlays().add(marker);
                selectedMarker = marker;
                markers.add(marker);
                
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

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
                if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[i]) ||
                    Manifest.permission.ACCESS_COARSE_LOCATION.equals(permissions[i])) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        // Permission granted, try to get current location
                        useCurrentLocation();
                    } else {
                        Toast.makeText(this, "Location permission is required for better experience", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
    }

    private void addCityMarkers() {
        try {
            // Show progress while loading cities
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            // Fetch cities from your database (using SupabaseClient or other data source)
            new Thread(() -> {
                try {
                    // Example: Fetch cities from Supabase
                    JSONArray cities = SupabaseClient.queryTable("ville", null, null, "id,nom,latitude,longitude");
                    
                    runOnUiThread(() -> {
                        try {
                            // Add a marker for each city
                            for (int i = 0; i < cities.length(); i++) {
                                JSONObject city = cities.getJSONObject(i);
                                int cityId = city.getInt("id");
                                String cityName = city.getString("nom");
                                double latitude = city.getDouble("latitude");
                                double longitude = city.getDouble("longitude");
                                
                                GeoPoint position = new GeoPoint(latitude, longitude);
                                addCityMarker(position, cityName, cityId);
                            }
                        } catch (JSONException e) {
                            Log.e("MapActivity", "Error parsing city data: " + e.getMessage(), e);
                            Toast.makeText(MapActivity.this, "Error loading cities", Toast.LENGTH_SHORT).show();
                        } finally {
                            // Hide progress when done
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e("MapActivity", "Error fetching cities: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(MapActivity.this, "Error loading cities", Toast.LENGTH_SHORT).show();
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            Log.e("MapActivity", "Error in addCityMarkers: " + e.getMessage(), e);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}
