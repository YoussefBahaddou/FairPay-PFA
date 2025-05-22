package com.emsi.fairpay_maroc.ui.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.emsi.fairpay_maroc.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapLog";
    private static final int PERMISSION_REQUEST_CODE = 1;

    private GoogleMap googleMap;
    private TextInputEditText searchEditText;
    private Map<String, Map<String, Double>> productPrices;
    private Map<String, Marker> cityMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize search bar
        searchEditText = findViewById(R.id.search_edit_text);
        MaterialButton searchButton = findViewById(R.id.search_button);

        // Handle search button click
        searchButton.setOnClickListener(v -> {
            String searchText = searchEditText.getText().toString().trim().toLowerCase();
            if (searchText.isEmpty()) {
                Toast.makeText(this, "Please enter a product name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate product name
            if (!productPrices.containsKey(searchText)) {
                Toast.makeText(this, "Product not found. Try: poulet, viande hachée, or sardine", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show markers for valid product
            showProductMarkers(searchText);
        });

        // Initialize mock price data
        initializeMockPrices();

        // Request location permission
        requestPermissionsIfNecessary();

        // Initialize Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        setupMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    private void setupMap() {
        if (googleMap == null) return;

        // Enable zoom controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Set initial map position to Morocco
        LatLng moroccoCenter = new LatLng(31.7917, -7.0926);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moroccoCenter, 6.0f));

        // Enable location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void initializeMockPrices() {
        productPrices = new HashMap<>();
        cityMarkers = new HashMap<>();

        // Mock prices for different cities (in MAD)
        Map<String, Double> pouletPrices = new HashMap<>();
        pouletPrices.put("casablanca", 30.0);
        pouletPrices.put("marrakech", 35.0);
        pouletPrices.put("rabat", 32.0);
        pouletPrices.put("fes", 28.0);
        pouletPrices.put("tanger", 33.0);
        pouletPrices.put("essaouira", 31.0);
        productPrices.put("poulet", pouletPrices);

        Map<String, Double> viandeHacheePrices = new HashMap<>();
        viandeHacheePrices.put("casablanca", 120.0);
        viandeHacheePrices.put("marrakech", 115.0);
        viandeHacheePrices.put("rabat", 125.0);
        viandeHacheePrices.put("fes", 118.0);
        viandeHacheePrices.put("tanger", 122.0);
        viandeHacheePrices.put("essaouira", 110.0);
        productPrices.put("viande hachée", viandeHacheePrices);

        Map<String, Double> sardinePrices = new HashMap<>();
        sardinePrices.put("casablanca", 25.0);
        sardinePrices.put("marrakech", 28.0);
        sardinePrices.put("rabat", 26.0);
        sardinePrices.put("fes", 24.0);
        sardinePrices.put("tanger", 27.0);
        sardinePrices.put("essaouira", 20.0); // Cheapest in Essaouira
        productPrices.put("sardine", sardinePrices);
    }

    private void showProductMarkers(String productName) {
        // Clear existing markers
        if (cityMarkers != null) {
            cityMarkers.values().forEach(marker -> marker.remove());
            cityMarkers.clear();
        }

        // Get prices for the searched product
        Map<String, Double> cityPrices = productPrices.get(productName.toLowerCase());
        if (cityPrices == null) {
            Log.e(TAG, "Product not found: " + productName);
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find min and max prices
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;
        for (double price : cityPrices.values()) {
            if (price < minPrice) minPrice = price;
            if (price > maxPrice) maxPrice = price;
        }
        Log.d(TAG, "Price range for " + productName + ": " + minPrice + " - " + maxPrice);

        // Add markers for each city
        addCityMarker(new LatLng(33.5899, -7.6033), "Casablanca", cityPrices.get("casablanca"), minPrice, maxPrice);
        addCityMarker(new LatLng(31.6295, -8.0008), "Marrakech", cityPrices.get("marrakech"), minPrice, maxPrice);
        addCityMarker(new LatLng(34.0183, -6.8416), "Rabat", cityPrices.get("rabat"), minPrice, maxPrice);
        addCityMarker(new LatLng(33.9317, -4.9899), "Fes", cityPrices.get("fes"), minPrice, maxPrice);
        addCityMarker(new LatLng(35.7451, -5.8559), "Tanger", cityPrices.get("tanger"), minPrice, maxPrice);
        addCityMarker(new LatLng(31.4764, -9.4733), "Essaouira", cityPrices.get("essaouira"), minPrice, maxPrice);
        Log.d(TAG, "Markers added for " + productName + " in all cities");

        // Center the map on Morocco
        LatLng moroccoCenter = new LatLng(31.7917, -7.0926);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(moroccoCenter, 5.5f));
        Log.d(TAG, "Map centered on Morocco");
    }

    private void addCityMarker(LatLng position, String cityName, double price, double minPrice, double maxPrice) {
        // Calculate color based on price (green for low, red for high)
        float hue = (float) (1.0 - ((price - minPrice) / (maxPrice - minPrice))) * 240; // 0=red, 120=green, 240=blue
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title(cityName)
                .snippet("Price: " + String.format("%.2f MAD", price))
                .icon(BitmapDescriptorFactory.defaultMarker(hue)));

        cityMarkers.put(cityName.toLowerCase(), marker);
    }

    private void requestPermissionsIfNecessary() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
    }
}
