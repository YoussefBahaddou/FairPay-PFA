package com.emsi.fairpay_maroc.ui.activities;

import static com.emsi.fairpay_maroc.data.SupabaseClient.getHttpClient;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;

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
import com.emsi.fairpay_maroc.utils.LanguageHelper;

import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.models.Category;
import com.emsi.fairpay_maroc.models.Location;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emsi.fairpay_maroc.models.Item;

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

import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private static final int REQUEST_LOCATION_SELECTION = 1001;
    private int selectedCityId = -1;
    private String selectedCityName = "";
    private ProgressBar progressBar;
    private RecyclerView cityItemsRecyclerView;
    private RecyclerView itemsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the RecyclerView
        itemsRecyclerView = findViewById(R.id.updates_recycler_view); // Make sure this ID matches your layout XML
        
        // Set layout manager
        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        TextView locationFilterText = findViewById(R.id.location_filter_text);
        if (locationFilterText != null) {
            locationFilterText.setText(getString(R.string.filtering_by_location, selectedCityName));
            locationFilterText.setVisibility(View.VISIBLE);
        }

        // Add this to the onCreate method
        FloatingActionButton fabAddProduct = findViewById(R.id.fab_add_product);

        // Show the FAB only for producers and contributors
        SharedPreferences prefs = getSharedPreferences("FairPayPrefs", MODE_PRIVATE);
        int userRoleId = prefs.getInt("user_role_id", -1);
        if (userRoleId == 2 || userRoleId == 3) { // 2 = producer, 3 = contributor
            fabAddProduct.setVisibility(View.VISIBLE);
            fabAddProduct.setOnClickListener(v -> addNewProduct());
        } else {
            fabAddProduct.setVisibility(View.GONE);
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

        // Set up the recycler views
        setupRecyclerViewsWithoutItems();
        
        loadUserRole();
        loadSavedLocation();

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

    private void loadUserRole() {
        // Get user ID from SharedPreferences
        int userId = getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                .getInt("user_id", -1);
        
        if (userId == -1) {
            return; // User not logged in
        }
        
        // Fetch user role in background
        new Thread(() -> {
            try {
                // Query the user to get their role_id
                JSONArray userResult = SupabaseClient.queryTable(
                        "utilisateur", 
                        "id", 
                        String.valueOf(userId), 
                        "role_id");
                
                if (userResult.length() > 0) {
                    String roleId = userResult.getJSONObject(0).optString("role_id", "1");
                    
                    // Get the role name
                    JSONArray roleResult = SupabaseClient.queryTable(
                            "role", 
                            "id", 
                            roleId, 
                            "nom");
                    
                    if (roleResult.length() > 0) {
                        String roleName = roleResult.getJSONObject(0).optString("nom", "user");
                        
                        // Save role in SharedPreferences
                        getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                                .edit()
                                .putString("user_role", roleName)
                                .apply();
                    }
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error loading user role: " + e.getMessage());
            }
        }).start();
    }

    // Add this method to set up RecyclerViews without fetching items yet
    private void setupRecyclerViewsWithoutItems() {
        // Categories RecyclerView
        RecyclerView categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fetchCategoriesFromDatabase(categoriesRecyclerView);

        // Locations RecyclerView
        RecyclerView locationsRecyclerView = findViewById(R.id.locations_recycler_view);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        fetchLocationsFromDatabase(locationsRecyclerView);

        // Updates RecyclerView - just set up the layout manager
        RecyclerView updatesRecyclerView = findViewById(R.id.updates_recycler_view);
        updatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupItemAdapter(RecyclerView updatesRecyclerView, List<Item> items) {
        ItemAdapter adapter = new ItemAdapter(items);
        
        // Set click listener for viewing items
        adapter.setOnItemClickListener((item, position) -> {
            // Get the product ID from the item
            int productId = item.getId();
            viewProduct(productId);
        });
        
        // Set action listener for editing/deleting items
        adapter.setOnItemActionListener(new ItemAdapter.OnItemActionListener() {
            @Override
            public void onEditItem(Item item, int position) {
                int productId = item.getId();
                editProduct(productId);
            }

            @Override
            public void onDeleteItem(Item item, int position) {
                int productId = item.getId();
                showDeleteConfirmationDialog(productId, position);
            }
        });
        
        updatesRecyclerView.setAdapter(adapter);
    }

    private void showDeleteConfirmationDialog(int productId, int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    deleteProduct(productId, position);
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteProduct(int productId, int position) {
        progressBar.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            try {
                // Delete the product from the database
                String path = "produit_serv?id=eq." + productId;
                SupabaseClient.deleteRecord(path);
                
                // Update the UI
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.product_deleted_successfully, Toast.LENGTH_SHORT).show();
                    
                    // Remove the item from the list and update the adapter
                    RecyclerView updatesRecyclerView = findViewById(R.id.updates_recycler_view);
                    ItemAdapter adapter = (ItemAdapter) updatesRecyclerView.getAdapter();
                    if (adapter != null) {
                        List<Item> items = new ArrayList<>(adapter.getItems());
                        items.remove(position);
                        adapter.updateItems(items);
                    }
                });
            } catch (Exception e) {
                Log.e("MainActivity", "Error deleting product: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.error_deleting_product, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void fetchItemsFromDatabase(RecyclerView updatesRecyclerView) {
        new Thread(() -> {
            try {
                Log.d("Fetching Item", "Starting to fetch items from the database...");
                
                // Fetch items from the "produit_serv" table where status is not 'pending'
                JSONArray itemsArray = SupabaseClient.queryTable("produit_serv", "status", "neq.pending", "*");
                List<Item> items = new ArrayList<>();

                Log.d("Fetching Item", "Successfully fetched data from the database. Parsing items...");

                // Map the JSON data to Item objects
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemData = itemsArray.getJSONObject(i);

                    // Get the item ID
                    int id = itemData.optInt("id", -1);

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
                Log.d("Fetching Location", "Starting to fetch locations from the database...");
                
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

    private JSONArray queryItems(String tableName) throws JSONException, IOException, Exception {
        // Use the existing SupabaseClient.queryTable method
        return SupabaseClient.queryTable(tableName, null, null, "*");
    }

    private String resolveForeignKey(String tableName, String keyColumn, int keyValue, String targetColumn) {
        if (keyValue == -1) return "Unknown";
        
        try {
            // Handle the type table specifically since it has a different column structure
            if (tableName.equals("type") && targetColumn.equals("nom")) {
                // Try with "name" instead of "nom" for the type table
                targetColumn = "name";
            }
            
            JSONArray result = SupabaseClient.queryTable(tableName, keyColumn, String.valueOf(keyValue), targetColumn);
            if (result.length() > 0) {
                return result.getJSONObject(0).optString(targetColumn, "Unknown");
            }
            return "Unknown";
        } catch (Exception e) {
            Log.e("MainActivity", "Error resolving foreign key for " + tableName + ": " + e.getMessage(), e);
            return "Unknown";
        }
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

    // Add this method to handle location selection from the map
    private void openLocationMap() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivityForResult(intent, REQUEST_LOCATION_SELECTION);
    }

    // Add this method to handle the result from MapActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_LOCATION_SELECTION) {
                // Handle location selection result (existing code)
                selectedCityId = data.getIntExtra("city_id", -1);
                selectedCityName = data.getStringExtra("city_name");
                
                if (selectedCityId != -1) {
                    Log.d("MainActivity", "Selected city: " + selectedCityName + " (ID: " + selectedCityId + ")");
                    Toast.makeText(this, "Selected location: " + selectedCityName, Toast.LENGTH_SHORT).show();
                    
                    // Save the selected location
                    saveSelectedLocation(selectedCityId, selectedCityName);
                    
                    // Update UI to show we're filtering by location
                    updateLocationFilterUI();
                    
                    // Refresh the items with the selected location filter
                    fetchItemsWithLocationFilter(selectedCityId);
                }
            } else if (requestCode == REQUEST_ADD_PRODUCT || requestCode == REQUEST_EDIT_PRODUCT) {
                // Refresh the product list after adding or editing a product
                RecyclerView updatesRecyclerView = findViewById(R.id.updates_recycler_view);
                
                if (selectedCityId != -1) {
                    fetchItemsWithLocationFilter(selectedCityId);
                } else {
                    fetchItemsFromDatabase(updatesRecyclerView);
                }
                
                Toast.makeText(this, requestCode == REQUEST_ADD_PRODUCT ? 
                        R.string.product_added_successfully : R.string.product_updated_successfully, 
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Modify the fetchItemsWithLocationFilter method to add more logging
    private void fetchItemsWithLocationFilter(int cityId) {
        RecyclerView updatesRecyclerView = findViewById(R.id.updates_recycler_view);
        
        // Show a loading indicator
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        new Thread(() -> {
            try {
                Log.d("Fetching Item", "Starting to fetch items from the database with location filter for city ID: " + cityId);
                
                // Fetch items from the "produit_serv" table with the filter and exclude pending status
                // We need to use a more complex query here with multiple conditions
                try {
                    JSONArray results = SupabaseClient.queryTableWithFilter("produit_serv", "ville_id=eq." + cityId + "&status=neq.pending", "*");
                    
                    // Process the results here
                    if (results.length() > 0) {
                        // Handle the results
                        runOnUiThread(() -> {
                            // Update your UI with the results
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "No products found for this city", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error querying products by city: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error loading products for this city", Toast.LENGTH_SHORT).show();
                    });
                }
                
                try {
                    // Use the SupabaseClient method to get the data
                    JSONArray results = SupabaseClient.queryTableWithFilter("produit_serv", "ville_id=eq." + cityId + "&status=neq.pending", "*");
                    
                    // Process the results
                    List<Item> cityItems = new ArrayList<>();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject itemData = results.getJSONObject(i);
                        
                        // Extract the data you need from itemData
                        String image = itemData.optString("image", "");
                        String nom = itemData.optString("nom", "Unknown");
                        String prix = itemData.optString("prix", "N/A");
                        String conseil = itemData.optString("conseil", "No advice");
                        String dateMiseAJour = itemData.optString("datemiseajour", "Unknown date");
                        String timeDifference = calculateTimeDifference(dateMiseAJour);
                        
                        // Resolve foreign keys if needed
                        String categorieName = resolveForeignKey("categorie", "id", itemData.optInt("categorie_id", -1), "nom");
                        String regionName = resolveForeignKey("ville", "id", itemData.optInt("ville_id", -1), "nom");
                        String typeName = resolveForeignKey("type", "id", itemData.optInt("type_id", -1), "nom");
                        
                        // Create an Item object and add it to the list
                        Item item = new Item(image, nom, prix, conseil, timeDifference, categorieName, regionName, typeName);
                        cityItems.add(item);
                    }
                    
                    // Update the UI on the main thread
                    runOnUiThread(() -> {
                        // Update your RecyclerView or other UI components with cityItems
                        if (cityItems.isEmpty()) {
                            Toast.makeText(MainActivity.this, "No products found for this city", Toast.LENGTH_SHORT).show();
                        } else {
                            // For example, if you have a RecyclerView:
                            if (itemsRecyclerView != null) {
                                ItemAdapter adapter = new ItemAdapter(cityItems);
                                itemsRecyclerView.setAdapter(adapter);
                            } else {
                                Log.e("MainActivity", "RecyclerView is null in fetchItemsWithLocationFilter");
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    Log.e("MainActivity", "Error loading city items: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error loading products for this city", Toast.LENGTH_SHORT).show();
                    });
                }
                
                // Declare and initialize itemsArray
                JSONArray itemsArray = SupabaseClient.queryTableWithFilter("produit_serv", "ville_id=eq." + cityId + "&status=neq.pending", "*");

                // Then you can use it
                Log.d("Fetching Item", "Query returned " + itemsArray.length() + " items");
                
                List<Item> items = new ArrayList<>();

                // Map the JSON data to Item objects
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemData = itemsArray.getJSONObject(i);
                    Log.d("Fetching Item", "Processing item: " + itemData.toString());

                    // Resolve foreign key data
                    String categorieName = resolveForeignKey("categorie", "id", itemData.optInt("categorie_id", -1), "nom");
                    String regionName = resolveForeignKey("ville", "id", itemData.optInt("ville_id", -1), "nom");
                    String typeName = resolveForeignKey("type", "id", itemData.optInt("type_id", -1), "name");

                    // Calculate the difference between current date and "datemiseajour"
                    String datemiseajour = itemData.optString("datemiseajour", "Unknown date");
                    String timeDifference = calculateTimeDifference(datemiseajour);
                    
                    // Map the data to an Item object
                    String image = itemData.optString("image", ""); 
                    String nom = itemData.optString("nom", "Unknown");
                    String prix = itemData.optString("prix", "N/A");

                    Item item = new Item(image, nom, prix, "No advice", timeDifference, categorieName, regionName, typeName);
                    items.add(item);
                    Log.d("Fetching Item", "Parsed item: " + item.getNom());
                }

                Log.d("Fetching Item", "All items parsed successfully. Updating RecyclerView...");

                // Update the RecyclerView on the main thread
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    
                    if (items.isEmpty()) {
                        Log.w("Fetching Item", "No items found for city ID: " + cityId);
                        Toast.makeText(this, "No items found in " + selectedCityName + ". Adding sample data...", Toast.LENGTH_SHORT).show();
                        
                        // If no items found, add sample data for testing
                        addSampleDataForCity(cityId);
                    } else {
                        ItemAdapter updateAdapter = new ItemAdapter(items);
                        updatesRecyclerView.setAdapter(updateAdapter);
                        Log.d("Fetching Item", "RecyclerView updated successfully with " + items.size() + " items.");
                    }
                });
            } catch (Exception e) {
                Log.e("Fetching Item", "Error occurred while fetching items: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // Add this method to insert sample data for testing
    private void addSampleDataForCity(int cityId) {
        new Thread(() -> {
            try {
                Log.d("Sample Data", "Adding sample data for city ID: " + cityId);
                
                // Create sample products for the selected city
                String cityName = selectedCityName;
                if (cityName.isEmpty()) {
                    cityName = "City " + cityId;
                }
                
                // Add a few sample products
                for (int i = 1; i <= 5; i++) {
                    JSONObject product = new JSONObject();
                    product.put("nom", "Sample Product " + i + " in " + cityName);
                    product.put("prix", 100 * i);
                    product.put("conseil", "This is a sample product added for testing");
                    product.put("datemiseajour", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                    product.put("categorie_id", (i % 4) + 1); // Cycle through categories 1-4
                    product.put("ville_id", cityId);
                    product.put("type_id", (i % 2) + 1); // Alternate between types 1-2
                    product.put("image", "sample_image_" + i + ".jpg");
                    product.put("status", "approved"); // Set status to approved so it shows up
                    
                    // Insert the product
                    SupabaseClient.insertIntoTable("produit_serv", product);
                    Log.d("Sample Data", "Added sample product: " + product.getString("nom"));
                }
                
                // Refresh the data
                runOnUiThread(() -> {
                    Toast.makeText(this, "Sample data added. Refreshing...", Toast.LENGTH_SHORT).show();
                    fetchItemsWithLocationFilter(cityId);
                });
                
            } catch (Exception e) {
                Log.e("Sample Data", "Error adding sample data: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error adding sample data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
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
                // Open MapActivity for result to get the selected location
                openLocationMap();
            } else if (id == R.id.nav_collabs) {
                // Open the CollabActivity
                Intent intent = new Intent(MainActivity.this, CollabActivity.class);
                startActivity(intent);
                // Apply custom animation
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else if (id == R.id.nav_profile) {
                // Launch the ProfileActivity
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                // Apply custom animation
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else if (id == R.id.nav_language) {
                showLanguageSelectionDialog();
            } else if (id == R.id.nav_settings) {
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                logout();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }


    /**
     * Shows a dialog to select language
     */
    private void showLanguageSelectionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_language_selection, null);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group_language);
        RadioButton radioFrench = dialogView.findViewById(R.id.radio_french);
        RadioButton radioEnglish = dialogView.findViewById(R.id.radio_english);
        RadioButton radioArabic = dialogView.findViewById(R.id.radio_arabic);

        // Set the current language selection
        String currentLanguage = LanguageHelper.getLanguage(this);
        if ("ar".equals(currentLanguage)) {
            radioArabic.setChecked(true);
        } else if ("en".equals(currentLanguage)) {
            radioEnglish.setChecked(true);
        } else {
            radioFrench.setChecked(true);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.language_settings)
                .setView(dialogView)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    String languageCode;

                    if (selectedId == R.id.radio_arabic) {
                        languageCode = "ar";
                    } else if (selectedId == R.id.radio_english) {
                        languageCode = "en";
                    } else {
                        languageCode = "fr";
                    }

                    if (!languageCode.equals(currentLanguage)) {
                        LanguageHelper.setLanguage(this, languageCode);
                        Toast.makeText(this, R.string.language_changed, Toast.LENGTH_SHORT).show();
                        // Restart the activity to apply language changes
                        recreateActivity();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.show();
    }

    /**
     * Recreates the activity to apply language changes
     */
    private void recreateActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
                    // Clear user session data
                    getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("is_logged_in", false)
                            .apply();

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

    private void saveSelectedLocation(int cityId, String cityName) {
        // Save the selected location to SharedPreferences
        getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
            .edit()
            .putInt("selected_city_id", cityId)
            .putString("selected_city_name", cityName)
            .apply();
        
        Log.d("MainActivity", "Saved location: " + cityName + " (ID: " + cityId + ")");
    }

    private void loadSavedLocation() {
        // Load the saved location from SharedPreferences
        selectedCityId = getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
            .getInt("selected_city_id", -1);
        selectedCityName = getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
            .getString("selected_city_name", "");
        
        if (selectedCityId != -1) {
            Log.d("MainActivity", "Loaded saved location: " + selectedCityName + " (ID: " + selectedCityId + ")");
            
            // Update UI to show we're filtering by location
            updateLocationFilterUI();
            
            // Fetch items with the saved location filter
            fetchItemsWithLocationFilter(selectedCityId);
        } else {
            // No saved location, fetch all items
            fetchItemsFromDatabase(findViewById(R.id.updates_recycler_view));
        }
    }

    private void updateLocationFilterUI() {
        TextView locationFilterText = findViewById(R.id.location_filter_text);
        if (locationFilterText != null) {
            if (selectedCityId != -1) {
                locationFilterText.setText(getString(R.string.filtering_by_location, selectedCityName));
                locationFilterText.setVisibility(View.VISIBLE);
            } else {
                locationFilterText.setVisibility(View.GONE);
            }
        }
    }

    private void clearLocationFilter() {
        // Clear the saved location
        getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
            .edit()
            .remove("selected_city_id")
            .remove("selected_city_name")
            .apply();
        
        selectedCityId = -1;
        selectedCityName = "";
        
        // Update UI
        updateLocationFilterUI();
        
        // Fetch all items
        fetchItemsFromDatabase(findViewById(R.id.updates_recycler_view));
        
        Toast.makeText(this, "Location filter cleared", Toast.LENGTH_SHORT).show();
    }

    /**
     * Opens the ProductActivity to view a product
     */
    private void viewProduct(int productId) {
        Intent intent = new Intent(this, ProductActivity.class);
        intent.putExtra(ProductActivity.EXTRA_MODE, ProductActivity.MODE_VIEW);
        intent.putExtra(ProductActivity.EXTRA_PRODUCT_ID, productId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Opens the ProductActivity to add a new product
     */
    private void addNewProduct() {
        Intent intent = new Intent(this, ProductActivity.class);
        intent.putExtra(ProductActivity.EXTRA_MODE, ProductActivity.MODE_ADD);
        startActivityForResult(intent, REQUEST_ADD_PRODUCT);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Opens the ProductActivity to edit an existing product
     */
    private void editProduct(int productId) {
        Intent intent = new Intent(this, ProductActivity.class);
        intent.putExtra(ProductActivity.EXTRA_MODE, ProductActivity.MODE_EDIT);
        intent.putExtra(ProductActivity.EXTRA_PRODUCT_ID, productId);
        startActivityForResult(intent, REQUEST_EDIT_PRODUCT);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private static final int REQUEST_ADD_PRODUCT = 1002;
    private static final int REQUEST_EDIT_PRODUCT = 1003;
}

                    
