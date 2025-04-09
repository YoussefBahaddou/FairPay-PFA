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
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

                    // Resolve foreign key data
                    String categorieName = resolveForeignKey("categorie", "id", itemData.optInt("categorie_id", -1), "nom");
                    String regionName = resolveForeignKey("ville", "id", itemData.optInt("ville_id", -1), "nom");
                    String typeName = resolveForeignKey("type", "id", itemData.optInt("type_id", -1), "name");

                    // Calculate the difference between current date and "datemiseajour"
                    String datemiseajour = itemData.optString("datemiseajour", "Unknown date");
                    String timeDifference = calculateTimeDifference(datemiseajour);

                    // Map the data to an Item object
                    String image = itemData.optString("image", ""); // Handle image as a String
                    String nom = itemData.optString("nom", "Unknown");
                    String prix = itemData.optString("prix", "N/A");

                    Item item = new Item(image, nom, prix, "No advice", timeDifference, categorieName, regionName, typeName);
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

    private void fetchCategoriesFromDatabase(RecyclerView categoriesRecyclerView) {
        new Thread(() -> {
            try {
                Log.d("Fetching Category", "Starting to fetch categories from the database...");
                
                // Fetch categories from the "categorie" tablex
                JSONArray categoriesArray = queryItems("categorie");
                List<Category> categories = new ArrayList<>();

                Log.d("Fetching Category", "Successfully fetched data from the database. Parsing categories...");

                // Map the JSON data to Category objects
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject categoryData = categoriesArray.getJSONObject(i);

                    String name = categoryData.optString("nom", "Unknown");
                    String iconUrl = categoryData.optString("icon", "");

                    Category category = new Category(iconUrl, name);
                    categories.add(category);
                    Log.d("Fetching Category", "Parsed category: " + category.getName());
                }

                Log.d("Fetching Category", "All categories parsed successfully. Updating RecyclerView...");

                // Update the RecyclerView on the main thread
                runOnUiThread(() -> {
                    CategoryAdapter categoryAdapter = new CategoryAdapter(categories);
                    categoriesRecyclerView.setAdapter(categoryAdapter);
                    Log.d("Fetching Category", "RecyclerView updated successfully.");
                });
            } catch (Exception e) {
                Log.e("Fetching Category", "Error occurred while fetching categories: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Error fetching categories", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void fetchLocationsFromDatabase(RecyclerView locationsRecyclerView) {
        new Thread(() -> {
            try {
                Log.d("Fetching Location", "Starting to fetch Flocations from the database...");
                
                // Fetch locations from the "region" table
                JSONArray locationsArray = queryItems("region");
                List<Location> locations = new ArrayList<>();

                Log.d("Fetching Location", "Successfully fetched data from the database. Parsing locations...");

                // Map the JSON data to Location objects
                for (int i = 0; i < locationsArray.length(); i++) {
                    JSONObject locationData = locationsArray.getJSONObject(i);

                    String name = locationData.optString("nom", "Unknown");
                    int villeId = locationData.optInt("ville_id", -1);

                    // Resolve ville name from ville_id
                    String villeName = resolveForeignKey("ville", "id", villeId, "nom");

                    Location location = new Location(name, villeName);
                    locations.add(location);
                    Log.d("Fetching Location", "Parsed location: " + location.getName() + ", Ville: " + location.getVilleName());
                }

                Log.d("Fetching Location", "All locations parsed successfully. Updating RecyclerView...");

                // Update the RecyclerView on the main thread
                runOnUiThread(() -> {
                    LocationAdapter locationAdapter = new LocationAdapter(locations);
                    locationsRecyclerView.setAdapter(locationAdapter);
                    Log.d("Fetching Location", "RecyclerView updated successfully.");
                });
            } catch (Exception e) {
                Log.e("Fetching Location", "Error occurred while fetching locations: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Error fetching locations", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private JSONArray queryItems(String tableName) throws JSONException, IOException {
        // Use the existing SupabaseClient.queryTable method
        return SupabaseClient.queryTable(tableName, null, null, "*");
    }

    private String resolveForeignKey(String tableName, String keyColumn, int keyValue, String targetColumn) throws JSONException, IOException {
        if (keyValue == -1) return "Unknown";
        JSONArray result = SupabaseClient.queryTable(tableName, keyColumn, String.valueOf(keyValue), targetColumn);
        if (result.length() > 0) {
            return result.getJSONObject(0).optString(targetColumn, "Unknown");
        }
        return "Unknown";
    }

    private String calculateTimeDifference(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateString);
            long diffInMillis = System.currentTimeMillis() - date.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            return days + " jours";
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown date";
        }
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView
        RecyclerView categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fetchCategoriesFromDatabase(categoriesRecyclerView);
    
        // Locations RecyclerView
        RecyclerView locationsRecyclerView = findViewById(R.id.locations_recycler_view);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fetchLocationsFromDatabase(locationsRecyclerView);
    
        // Updates RecyclerView
        RecyclerView updatesRecyclerView = findViewById(R.id.updates_recycler_view);
        updatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchItemsFromDatabase(updatesRecyclerView);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_categories) {
            Toast.makeText(this, "Categories", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_locations) {
            // Open MapActivity
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_contribute) {
            Toast.makeText(this, "Contribute", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_profile) {
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_language) {
            Intent intent = new Intent(this, LanguageSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
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