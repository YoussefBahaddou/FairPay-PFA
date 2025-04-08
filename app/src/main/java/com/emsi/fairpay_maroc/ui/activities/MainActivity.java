package com.emsi.fairpay_maroc.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Toast;


import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.adapters.CategoryAdapter;
import com.emsi.fairpay_maroc.adapters.ItemAdapter;
import com.emsi.fairpay_maroc.adapters.LocationAdapter;

import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.models.Category;
import com.emsi.fairpay_maroc.models.Location;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import com.emsi.fairpay_maroc.models.Item; // Updated to use Item class

import java.util.Map;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import android.util.Log;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set up the navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up the menu button click listener
        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Set up the search bar
        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // In a real app, you would show search suggestions or history
                Toast.makeText(MainActivity.this, "Search functionality coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the recycler views with dummy data
        setupRecyclerViews();

        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

        private void fetchItemsFromDatabase(RecyclerView updatesRecyclerView) {
        new Thread(() -> {
            try {
                Log.d("Fetching Item", "Starting to fetch items from the database...");
                
                // Fetch items from the "produit_serv" table
                JSONArray itemsArray = queryItems("produit_serv");
                List<Item> items = new ArrayList<>();
    
                Log.d("Fetching Item", "Successfully fetched data from the database. Parsing items...");
    
                // Map the JSON data to Item objects
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemData = itemsArray.getJSONObject(i);
    
                    // Handle null values with default values
                    String image = itemData.optString("image", ""); // Handle image as a String
                    String nom = itemData.optString("nom", "Unknown");
                    String prix = itemData.optString("prix", "N/A");
                    String conseil = itemData.optString("conseil", "No advice");
                    String datemiseajour = itemData.optString("datemiseajour", "Unknown date");
                    int categorieId = itemData.optInt("categorie_id", -1);
                    int villeId = itemData.optInt("ville_id", -1);
                    int typeId = itemData.optInt("type_id", -1);
    
                    Item item = new Item(image, nom, prix, conseil, datemiseajour, categorieId, villeId, typeId);
                    items.add(item);
                    Log.d("Fetching Item", "Parsed item: " + item.getNom());
                }
    
                Log.d("Fetching Item", "All items parsed successfully. Updating RecyclerView...");
    
                // Update the RecyclerView on the main thread
                runOnUiThread(() -> {
                    ItemAdapter updateAdapter = new ItemAdapter(items);
                    updatesRecyclerView.setAdapter(updateAdapter);
                    Log.d("Fetching Item", "RecyclerView updated successfully.");
                });
            } catch (Exception e) {
                Log.e("Fetching Item", "Error occurred while fetching items: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    private JSONArray queryItems(String tableName) throws JSONException, IOException {
        // Use the existing SupabaseClient.queryTable method
        return SupabaseClient.queryTable(tableName, null, null, "*");
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView
        RecyclerView categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        categoriesRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Create dummy categories data
        List<Category> categories = createDummyCategories();
        CategoryAdapter categoryAdapter = new CategoryAdapter(categories);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        // Locations RecyclerView
        RecyclerView locationsRecyclerView = findViewById(R.id.locations_recycler_view);
        locationsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Create dummy locations data
        List<Location> locations = createDummyLocations();
        LocationAdapter locationAdapter = new LocationAdapter(locations);
        locationsRecyclerView.setAdapter(locationAdapter);

        // Updates RecyclerView
        RecyclerView updatesRecyclerView = findViewById(R.id.updates_recycler_view);
        updatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch items from the database
        fetchItemsFromDatabase(updatesRecyclerView);
    }

    private List<Category> createDummyCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(R.drawable.ic_food, "Food"));
        categories.add(new Category(R.drawable.ic_electronics, "Electronics"));
        categories.add(new Category(R.drawable.ic_clothing, "Clothing"));
        categories.add(new Category(R.drawable.ic_home, "Home"));
        categories.add(new Category(R.drawable.ic_beauty, "Beauty"));
        categories.add(new Category(R.drawable.ic_sports, "Sports"));
        return categories;
    }

    private List<Location> createDummyLocations() {
        List<Location> locations = new ArrayList<>();
        locations.add(new Location(R.drawable.img_casablanca, "Casablanca", "Economic capital"));
        locations.add(new Location(R.drawable.img_rabat, "Rabat", "Administrative capital"));
        locations.add(new Location(R.drawable.img_marrakech, "Marrakech", "Tourist destination"));
        locations.add(new Location(R.drawable.img_tangier, "Tangier", "Northern port city"));
        locations.add(new Location(R.drawable.img_agadir, "Agadir", "Coastal city"));
        return locations;
    }

   
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home screen
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_categories) {
            // Navigate to categories screen
            Toast.makeText(this, "Categories", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_locations) {
            // Navigate to locations screen
            Toast.makeText(this, "Locations", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_contribute) {
            // Navigate to contribute screen
            Toast.makeText(this, "Contribute", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_profile) {
            // Navigate to profile screen
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_language) {
            // Navigate to language settings
            Intent intent = new Intent(this, LanguageSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            // Navigate to settings screen
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            // Handle logout
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    /**
     * Handles the logout process
     */
    private void logout() {
        // Show a confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear any user session data here
                    // For example, clear SharedPreferences

                    // Create an intent to start the LoginActivity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    // Clear the back stack so the user can't go back to MainActivity after logout
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    // Finish the current activity
                    finish();

                    // Show a toast message
                    Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

}