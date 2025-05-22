package com.emsi.fairpay_maroc.ui.activities;

import static com.emsi.fairpay_maroc.data.SupabaseClient.getHttpClient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.adapters.CategoryAdapter;
import com.emsi.fairpay_maroc.adapters.ItemAdapter;
import com.emsi.fairpay_maroc.adapters.LocationAdapter;
import com.emsi.fairpay_maroc.utils.LanguageHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.emsi.fairpay_maroc.utils.SharedPreferencesManager;

import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.models.Category;
import com.emsi.fairpay_maroc.models.Location;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private static final int REQUEST_LOCATION_SELECTION = 1001;
    private static final int REQUEST_ADD_PRODUCT = 1002;
    private static final int REQUEST_EDIT_PRODUCT = 1003;
    private static final int REQUEST_PROFILE = 1001;
    private ImageView profileIcon;
    private ActivityResultLauncher<Intent> profileLauncher;
    private int selectedCityId = -1;
    private String selectedCityName = "";
    private int selectedCategoryId = -1;
    private String selectedCategoryName = "";

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView locationsRecyclerView;
    private RecyclerView updatesRecyclerView;
    private EditText searchBar;
    private ImageView clearSearchButton;
    private ChipGroup filterChipGroup;
    private Chip locationFilterChip;
    private Chip categoryFilterChip;

    private List<Item> allItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initializeViews();

        // Set up the toolbar
        setupToolbar();

        // Set up the navigation drawer
        setupNavigationDrawer();

        // Set up the search bar
        setupSearchBar();

        // Set up the filter chips
        setupFilterChips();

        // Set up the recycler views
        setupRecyclerViews();

        // Set up swipe refresh
        setupSwipeRefresh();

        // Load user role and saved location
        loadUserRole();
        loadSavedLocation();

        // Handle back button press
        setupBackPressHandler();

        // Load user profile image
        loadUserProfileImage();

        // Initialize profile launcher
        profileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Refresh user data if needed
                        updateUserInfo();
                    }
                });

        // Set up profile icon click listener
        profileIcon = findViewById(R.id.profile_icon);
        if (profileIcon != null) {
            profileIcon.setOnClickListener(v -> openProfileActivity());
        }

        // IMPORTANT: Directly load data to ensure it happens
        Log.d(TAG, "Directly loading data from onCreate");
        loadInitialData();
    }

    // Add this new method to directly load data
    private void loadInitialData() {
        // Create and set empty adapters first to avoid "No adapter attached" warnings
        if (categoriesRecyclerView != null) {
            categoriesRecyclerView.setAdapter(new CategoryAdapter(new ArrayList<>()));
        }

        if (locationsRecyclerView != null) {
            locationsRecyclerView.setAdapter(new LocationAdapter(new ArrayList<>()));
        }

        if (updatesRecyclerView != null) {
            updatesRecyclerView.setAdapter(new ItemAdapter(new ArrayList<>()));
        }

        // Now fetch the actual data
        fetchCategoriesFromDatabase();
        fetchLocationsFromDatabase();
        fetchItemsFromDatabase();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progress_bar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        locationsRecyclerView = findViewById(R.id.locations_recycler_view);
        updatesRecyclerView = findViewById(R.id.updates_recycler_view);
        searchBar = findViewById(R.id.search_bar);
        clearSearchButton = findViewById(R.id.clear_search);
        filterChipGroup = findViewById(R.id.filter_chip_group);
        locationFilterChip = findViewById(R.id.location_filter_chip);
        categoryFilterChip = findViewById(R.id.category_filter_chip);

        // Ensure RecyclerViews are visible
        if (categoriesRecyclerView != null) {
            categoriesRecyclerView.setVisibility(View.VISIBLE);
            Log.d(TAG, "Categories RecyclerView found and set to VISIBLE");
        } else {
            Log.e(TAG, "Categories RecyclerView is null");
        }

        if (locationsRecyclerView != null) {
            locationsRecyclerView.setVisibility(View.VISIBLE);
            Log.d(TAG, "Locations RecyclerView found and set to VISIBLE");
        } else {
            Log.e(TAG, "Locations RecyclerView is null");
        }

        if (updatesRecyclerView != null) {
            updatesRecyclerView.setVisibility(View.VISIBLE);
            Log.d(TAG, "Updates RecyclerView found and set to VISIBLE");
        } else {
            Log.e(TAG, "Updates RecyclerView is null");
        }

        // Check parent containers
        View categoriesSection = findViewById(R.id.categories_section);
        View locationsSection = findViewById(R.id.locations_section);
        View updatesSection = findViewById(R.id.updates_section);

        if (categoriesSection != null) {
            categoriesSection.setVisibility(View.VISIBLE);
            Log.d(TAG, "Categories section found and set to VISIBLE");
        } else {
            Log.e(TAG, "Categories section is null");
        }

        if (locationsSection != null) {
            locationsSection.setVisibility(View.VISIBLE);
            Log.d(TAG, "Locations section found and set to VISIBLE");
        } else {
            Log.e(TAG, "Locations section is null");
        }

        if (updatesSection != null) {
            updatesSection.setVisibility(View.VISIBLE);
            Log.d(TAG, "Updates section found and set to VISIBLE");
        } else {
            Log.e(TAG, "Updates section is null");
        }

        // Set up "See All" buttons
        TextView seeAllCategories = findViewById(R.id.see_all_categories);
        TextView seeAllLocations = findViewById(R.id.see_all_locations);
        TextView seeAllUpdates = findViewById(R.id.see_all_updates);

        if (seeAllCategories != null) {
            seeAllCategories.setOnClickListener(v -> {
                Toast.makeText(this, R.string.view_all_categories, Toast.LENGTH_SHORT).show();
            });
            Log.d(TAG, "See All Categories button found and set up");
        } else {
            Log.e(TAG, "See All Categories button is null");
        }

        if (seeAllLocations != null) {
            seeAllLocations.setOnClickListener(v -> {
                openLocationMap();
            });
            Log.d(TAG, "See All Locations button found and set up");
        } else {
            Log.e(TAG, "See All Locations button is null");
        }

        if (seeAllUpdates != null) {
            seeAllUpdates.setOnClickListener(v -> {
                Toast.makeText(this, R.string.view_all_products, Toast.LENGTH_SHORT).show();
            });
            Log.d(TAG, "See All Updates button found and set up");
        } else {
            Log.e(TAG, "See All Updates button is null");
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set up the menu button click listener
        ImageButton menuButton = findViewById(R.id.menu_button);
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
    }

    private void forceRefreshUI() {
        Log.d(TAG, "Force refreshing UI");

        // Make sure all sections are visible
        View categoriesSection = findViewById(R.id.categories_section);
        View locationsSection = findViewById(R.id.locations_section);
        View updatesSection = findViewById(R.id.updates_section);

        if (categoriesSection != null) categoriesSection.setVisibility(View.VISIBLE);
        if (locationsSection != null) locationsSection.setVisibility(View.VISIBLE);
        if (updatesSection != null) updatesSection.setVisibility(View.VISIBLE);

        // Make sure all RecyclerViews are visible
        if (categoriesRecyclerView != null) categoriesRecyclerView.setVisibility(View.VISIBLE);
        if (locationsRecyclerView != null) locationsRecyclerView.setVisibility(View.VISIBLE);
        if (updatesRecyclerView != null) updatesRecyclerView.setVisibility(View.VISIBLE);

        // Refresh all data
        fetchCategoriesFromDatabase();
        fetchLocationsFromDatabase();
        fetchItemsFromDatabase();
    }

    private void setupNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, findViewById(R.id.toolbar),
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupSearchBar() {
        searchBar = findViewById(R.id.search_bar);
        clearSearchButton = findViewById(R.id.clear_search);

        // Set up clear button visibility and click listener
        clearSearchButton.setOnClickListener(v -> {
            searchBar.setText("");
            clearSearchButton.setVisibility(View.GONE);
            // Reset search results
            applyCurrentFilters();
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show/hide clear button based on text content
                clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // If search is empty, apply the appropriate filters
                    applyCurrentFilters();
                } else {
                    // Search across all products regardless of filters
                    searchProductsByName(query);
                }
            }
        });
    }

    private void setupFilterChips() {
        // Set up location filter chip
        locationFilterChip.setOnCloseIconClickListener(v -> {
            clearLocationFilter();
            updateFilterChipsVisibility();
        });

        // Set up category filter chip
        categoryFilterChip.setOnCloseIconClickListener(v -> {
            clearCategoryFilter();
            updateFilterChipsVisibility();
        });
    }

    private void updateFilterChipsVisibility() {
        if (filterChipGroup == null || locationFilterChip == null || categoryFilterChip == null) {
            Log.e(TAG, "One or more filter chip views are null");
            return;
        }

        boolean hasAnyFilter = selectedCityId != -1 || selectedCategoryId != -1;
        filterChipGroup.setVisibility(hasAnyFilter ? View.VISIBLE : View.GONE);

        // Update location chip
        if (selectedCityId != -1) {
            locationFilterChip.setText(getString(R.string.location_filter, selectedCityName));
            locationFilterChip.setVisibility(View.VISIBLE);
        } else {
            locationFilterChip.setVisibility(View.GONE);
        }

        // Update category chip
        if (selectedCategoryId != -1) {
            categoryFilterChip.setText(getString(R.string.category_filter, selectedCategoryName));
            categoryFilterChip.setVisibility(View.VISIBLE);
        } else {
            categoryFilterChip.setVisibility(View.GONE);
        }

        Log.d(TAG, "Updated filter chips visibility. Location: " + (selectedCityId != -1) +
              ", Category: " + (selectedCategoryId != -1));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void setupRecyclerViews() {
        try {
            // Check network connectivity first
            if (!isNetworkAvailable()) {
                Toast.makeText(this, "No internet connection. Please check your network settings.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "No internet connection available");
            }

            // Categories RecyclerView - horizontal grid with 2 rows
            if (categoriesRecyclerView != null) {
                GridLayoutManager categoriesLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
                categoriesRecyclerView.setLayoutManager(categoriesLayoutManager);
                categoriesRecyclerView.setHasFixedSize(true);

                // Apply layout animation to categories
                LayoutAnimationController categoryAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation);
                categoriesRecyclerView.setLayoutAnimation(categoryAnimation);

                // Set an empty adapter initially
                categoriesRecyclerView.setAdapter(new CategoryAdapter(new ArrayList<>()));
            } else {
                Log.e(TAG, "Cannot setup categoriesRecyclerView - view is null");
            }

            // Locations RecyclerView - horizontal
            if (locationsRecyclerView != null) {
                LinearLayoutManager locationsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                locationsRecyclerView.setLayoutManager(locationsLayoutManager);
                locationsRecyclerView.setHasFixedSize(true);

                // Apply layout animation to locations
                LayoutAnimationController locationAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation);
                locationsRecyclerView.setLayoutAnimation(locationAnimation);

                // Set an empty adapter initially
                locationsRecyclerView.setAdapter(new LocationAdapter(new ArrayList<>()));
            } else {
                Log.e(TAG, "Cannot setup locationsRecyclerView - view is null");
            }

            // Updates RecyclerView - vertical
            if (updatesRecyclerView != null) {
                LinearLayoutManager updatesLayoutManager = new LinearLayoutManager(this);
                updatesRecyclerView.setLayoutManager(updatesLayoutManager);

                // Apply layout animation to updates
                LayoutAnimationController updateAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation);
                updatesRecyclerView.setLayoutAnimation(updateAnimation);

                // Set an empty adapter initially
                updatesRecyclerView.setAdapter(new ItemAdapter(new ArrayList<>()));
            } else {
                Log.e(TAG, "Cannot setup updatesRecyclerView - view is null");
            }

            // Fetch data
            fetchCategoriesFromDatabase();
            fetchLocationsFromDatabase();

            // Fetch items only if we don't have a saved location filter
            if (selectedCityId == -1 && selectedCategoryId == -1) {
                fetchItemsFromDatabase();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerViews: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up UI components", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout == null) {
            Log.e(TAG, "swipeRefreshLayout is null in setupSwipeRefresh");
            return;
        }

        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark
        );

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe refresh triggered");
            // Refresh all data
            refreshAllData();
        });
    }

    private void refreshAllData() {
        Log.d(TAG, "Refreshing all data");

        // Clear search
        if (searchBar != null) {
            searchBar.setText("");
        }

        // Refresh categories and locations
        fetchCategoriesFromDatabase();
        fetchLocationsFromDatabase();

        // Apply current filters or fetch all items
        applyCurrentFilters();

        // Stop refreshing animation after a delay
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.postDelayed(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }, 1000);
        }
    }

    private void setupBackPressHandler() {
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

    private void applyCurrentFilters() {
        if (selectedCategoryId != -1 && selectedCityId != -1) {
            // Both category and location filters are active
            fetchItemsWithCategoryAndLocationFilter(selectedCategoryId, selectedCityId);
        } else if (selectedCategoryId != -1) {
            // Only category filter is active
            fetchItemsWithCategoryFilter(selectedCategoryId);
        } else if (selectedCityId != -1) {
            // Only location filter is active
            fetchItemsWithLocationFilter(selectedCityId);
        } else {
            // No filters are active
            fetchItemsFromDatabase();
        }
    }

    private void fetchCategoriesFromDatabase() {
        showLoading(true);
        Log.d(TAG, "Starting to fetch categories from database");

        // Create and set an empty adapter first to avoid "No adapter attached" warnings
        if (categoriesRecyclerView != null) {
            categoriesRecyclerView.setAdapter(new CategoryAdapter(new ArrayList<>()));
        }

        new Thread(() -> {
            try {
                // Fetch categories from the "categorie" table
                JSONArray categoriesArray = SupabaseClient.queryTable("categorie", null, null, "*");

                if (categoriesArray == null) {
                    Log.e(TAG, "Categories array is null from database");
                    // Even if there's an error, set an empty adapter
                    if (categoriesRecyclerView != null) {
                        categoriesRecyclerView.setAdapter(new CategoryAdapter(new ArrayList<>()));
                    }
                    Toast.makeText(this, "Error fetching categories", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                }

                Log.d(TAG, "Fetched " + categoriesArray.length() + " categories from database");
                List<Category> categories = new ArrayList<>();

                // Map the JSON data to Category objects
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject categoryData = categoriesArray.getJSONObject(i);

                    int id = categoryData.optInt("id", -1);
                    String name = categoryData.optString("nom", "Unknown");
                    String iconUrl = categoryData.optString("icon", "");

                    Category category = new Category(id, iconUrl, name);
                    categories.add(category);
                    Log.d(TAG, "Parsed category: " + category.getName() + " with ID: " + id);
                }

                // Update the RecyclerView on the main thread
                runOnUiThread(() -> {
                    if (categoriesRecyclerView != null) {
                        CategoryAdapter categoryAdapter = new CategoryAdapter(categories);
                        categoryAdapter.setOnCategoryClickListener((category, position) -> {
                            selectedCategoryId = category.getId();
                            selectedCategoryName = category.getName();
                            updateFilterChipsVisibility();
                            fetchItemsWithCategoryFilter(selectedCategoryId);
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.category_selected, category.getName()),
                                    Toast.LENGTH_SHORT).show();
                        });
                        categoriesRecyclerView.setAdapter(categoryAdapter);
                        categoriesRecyclerView.scheduleLayoutAnimation();
                        Log.d(TAG, "Categories adapter set with " + categories.size() + " items");
                        Log.d(TAG, "Setting up categories adapter with " + categories.size() + " items");
                        Log.e(TAG, "Categories RecyclerView is null when trying to set adapter");
                    }
                    showLoading(false);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error occurred while fetching categories: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    // Even if there's an error, set an empty adapter
                    if (categoriesRecyclerView != null) {
                        categoriesRecyclerView.setAdapter(new CategoryAdapter(new ArrayList<>()));
                    }
                    Toast.makeText(this, "Error fetching categories", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private void fetchLocationsFromDatabase() {
        showLoading(true);
        Log.d(TAG, "Starting to fetch locations from database");

        // Create and set an empty adapter first to avoid "No adapter attached" warnings
        if (locationsRecyclerView != null) {
            locationsRecyclerView.setAdapter(new LocationAdapter(new ArrayList<>()));
        }

        new Thread(() -> {
            try {
                Log.d(TAG, "Starting to fetch locations from the database...");

                // Fetch locations from the "region" table
                JSONArray locationsArray = SupabaseClient.queryTable("region", null, null, "*");
                List<Location> locations = new ArrayList<>();

                if (locationsArray == null) {
                    Log.e(TAG, "Locations array is null from database");
                    runOnUiThread(() -> {
                        // Even if there's an error, set an empty adapter
                        if (locationsRecyclerView != null) {
                            locationsRecyclerView.setAdapter(new LocationAdapter(new ArrayList<>()));
                        }
                        Toast.makeText(this, "Error fetching locations", Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    });
                    return;
                }

                Log.d(TAG, "Successfully fetched data from the database. Parsing locations... Count: " + locationsArray.length());

                // Map the JSON data to Location objects
                for (int i = 0; i < locationsArray.length(); i++) {
                    JSONObject locationData = locationsArray.getJSONObject(i);

                    String regionName = locationData.optString("nom", "Unknown");
                    int villeId = locationData.optInt("ville_id", -1);
                    String imageUrl = locationData.optString("image_url", "");

                    // Resolve ville name from ville_id
                    String villeName = resolveForeignKey("ville", "id", villeId, "nom");

                    Location location = new Location(villeId, regionName, villeName, imageUrl);
                    locations.add(location);
                    Log.d(TAG, "Parsed location: " + location.getName() + ", Ville: " + location.getVilleName() + ", Image: " + location.getImageUrl());
                }

                Log.d(TAG, "All locations parsed successfully. Updating RecyclerView...");

                // Update the RecyclerView on the main thread
                runOnUiThread(() -> {
                    if (locationsRecyclerView != null) {
                        LocationAdapter locationAdapter = new LocationAdapter(locations);

                        // Set click listener for location selection
                        locationAdapter.setOnLocationClickListener((location, position) -> {
                            selectedCityId = location.getVilleId();
                            selectedCityName = location.getVilleName();

                            // Save the selected location
                            saveSelectedLocation(selectedCityId, selectedCityName);

                            // Update filter chips
                            updateFilterChipsVisibility();

                            // Apply ripple animation to the clicked item
                            View itemView = locationsRecyclerView.findViewHolderForAdapterPosition(position).itemView;
                            if (itemView != null) {
                                animateItemSelection(itemView);
                            }

                            // Refresh the items with the selected location filter
                            fetchItemsWithLocationFilter(selectedCityId);

                            Toast.makeText(MainActivity.this,
                                    getString(R.string.location_selected, selectedCityName),
                                    Toast.LENGTH_SHORT).show();
                        });

                        locationsRecyclerView.setAdapter(locationAdapter);
                        locationsRecyclerView.scheduleLayoutAnimation();
                        Log.d(TAG, "Locations adapter set with " + locations.size() + " items");
                    } else {
                        Log.e(TAG, "Locations RecyclerView is null when trying to set adapter");
                    }
                    showLoading(false);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error occurred while fetching locations: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    // Even if there's an error, set an empty adapter
                    if (locationsRecyclerView != null) {
                        locationsRecyclerView.setAdapter(new LocationAdapter(new ArrayList<>()));
                    }
                    Toast.makeText(this, "Error fetching locations", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private void fetchItemsFromDatabase() {
        showLoading(true);
        Log.d(TAG, "Starting to fetch items from database");

        // Create and set an empty adapter first to avoid "No adapter attached" warnings
        if (updatesRecyclerView != null) {
            updatesRecyclerView.setAdapter(new ItemAdapter(new ArrayList<>()));
        }

        new Thread(() -> {
            try {
                Log.d(TAG, "Starting to fetch items from the database...");

                // Fetch items from the "produit_serv" table where status is not 'pending'
                JSONArray itemsArray = SupabaseClient.queryTableWithFilter("produit_serv", "status=neq.pending", "*");
                List<Item> items = new ArrayList<>();

                if (itemsArray == null) {
                    Log.e(TAG, "Items array is null from database");
                    runOnUiThread(() -> {
                        // Even if there's an error, set an empty adapter
                        if (updatesRecyclerView != null) {
                            updatesRecyclerView.setAdapter(new ItemAdapter(new ArrayList<>()));
                        }
                        Toast.makeText(this, "Error fetching products", Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    });
                    return;
                }

                Log.d(TAG, "Successfully fetched data from the database. Parsing items... Count: " + itemsArray.length());

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
                    String image = itemData.optString("image", "");
                    String nom = itemData.optString("nom", "Unknown");
                    String prix = itemData.optString("prix", "N/A");
                    String conseil = itemData.optString("conseil", "No advice");

                    Item item = new Item(id, image, nom, prix, conseil, timeDifference, categorieName, regionName, typeName);
                    items.add(item);
                    Log.d(TAG, "Parsed item: " + item.getNom());
                }

                // Save all items for filtering
                allItems = new ArrayList<>(items);

                Log.d(TAG, "All items parsed successfully. Updating RecyclerView...");

                // Update the RecyclerView on the main thread
                runOnUiThread(() -> {
                    updateItemsRecyclerView(items);
                    showLoading(false);
                });
            } catch (Exception e) {
                Log.e(TAG, "Error occurred while fetching items: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    // Even if there's an error, set an empty adapter
                    if (updatesRecyclerView != null) {
                        updatesRecyclerView.setAdapter(new ItemAdapter(new ArrayList<>()));
                    }
                    Toast.makeText(this, "Error fetching products", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private void updateItemsRecyclerView(List<Item> items) {
        try {
            // Show/hide empty state
            TextView emptyUpdatesText = findViewById(R.id.empty_updates_text);

            if (updatesRecyclerView == null) {
                Log.e(TAG, "updatesRecyclerView is null in updateItemsRecyclerView");
                return;
            }

            if (items.isEmpty()) {
                if (emptyUpdatesText != null) {
                    emptyUpdatesText.setVisibility(View.VISIBLE);
                }
                updatesRecyclerView.setVisibility(View.GONE);
                Log.d(TAG, "No items to display");
            } else {
                if (emptyUpdatesText != null) {
                    emptyUpdatesText.setVisibility(View.GONE);
                }
                updatesRecyclerView.setVisibility(View.VISIBLE);

                // Create and set adapter
                ItemAdapter adapter = new ItemAdapter(items);

                // Set click listener for viewing items
                adapter.setOnItemClickListener((item, position) -> {
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
                updatesRecyclerView.scheduleLayoutAnimation();
                Log.d(TAG, "Displayed " + items.size() + " items in the RecyclerView");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating items RecyclerView: " + e.getMessage(), e);
        }
    }

    private void searchProductsByName(String query) {
        showLoading(true);

        new Thread(() -> {
            try {
                Log.d(TAG, "Searching for products with name containing: " + query);

                // Use the SupabaseClient to search for products by name across all locations
                // The ilike operator performs case-insensitive matching
                String filter = "nom=ilike.*" + query + "*&status=neq.pending";
                JSONArray results = SupabaseClient.queryTableWithFilter("produit_serv", filter, "*");

                List<Item> searchResults = new ArrayList<>();

                for (int i = 0; i < results.length(); i++) {
                    JSONObject itemData = results.getJSONObject(i);

                    // Extract the data from itemData
                    int id = itemData.optInt("id", -1);
                    String image = itemData.optString("image", "");
                    String nom = itemData.optString("nom", "Unknown");
                    String prix = itemData.optString("prix", "N/A");
                    String conseil = itemData.optString("conseil", "No advice");
                    String dateMiseAJour = itemData.optString("datemiseajour", "Unknown date");
                    String timeDifference = calculateTimeDifference(dateMiseAJour);

                    // Resolve foreign keys
                    String categorieName = resolveForeignKey("categorie", "id", itemData.optInt("categorie_id", -1), "nom");
                    String regionName = resolveForeignKey("ville", "id", itemData.optInt("ville_id", -1), "nom");
                    String typeName = resolveForeignKey("type", "id", itemData.optInt("type_id", -1), "name");

                    // Create an Item object with the ID
                    Item item = new Item(id, image, nom, prix, conseil, timeDifference, categorieName, regionName, typeName);
                    searchResults.add(item);
                }

                // Update the UI on the main thread
                runOnUiThread(() -> {
                    updateItemsRecyclerView(searchResults);
                    showLoading(false);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error searching for products: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, R.string.error_searching, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void fetchItemsWithLocationFilter(int cityId) {
        showLoading(true);

        new Thread(() -> {
            try {
                Log.d(TAG, "Starting to fetch items with location filter for city ID: " + cityId);

                // Filter by location and exclude pending status
                String filter = "ville_id=eq." + cityId + "&status=neq.pending";
                JSONArray itemsArray = SupabaseClient.queryTableWithFilter("produit_serv", filter, "*");

                Log.d(TAG, "Query returned " + itemsArray.length() + " items");

                List<Item> items = new ArrayList<>();

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
                    String image = itemData.optString("image", "");
                    String nom = itemData.optString("nom", "Unknown");
                    String prix = itemData.optString("prix", "N/A");
                    String conseil = itemData.optString("conseil", "No advice");

                    Item item = new Item(id, image, nom, prix, conseil, timeDifference, categorieName, regionName, typeName);
                    items.add(item);
                }

                // Update the RecyclerView on the main thread
                runOnUiThread(() -> {
                    updateItemsRecyclerView(items);
                    showLoading(false);

                    if (items.isEmpty()) {
                        Toast.makeText(this, getString(R.string.no_items_in_location, selectedCityName), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error occurred while fetching items: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void fetchItemsWithCategoryFilter(int categoryId) {
        showLoading(true);

        new Thread(() -> {
            try {
                Log.d(TAG, "Starting to fetch items with category filter for category ID: " + categoryId);

                // Build the filter string based on whether we also have a location filter
                String filter = "categorie_id=eq." + categoryId + "&status=neq.pending";

                // Fetch items from the "produit_serv" table with the filter
                JSONArray itemsArray = SupabaseClient.queryTableWithFilter("produit_serv", filter, "*");

                Log.d(TAG, "Query returned " + itemsArray.length() + " items");

                List<Item> items = new ArrayList<>();

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
                    String image = itemData.optString("image", "");
                    String nom = itemData.optString("nom", "Unknown");
                    String prix = itemData.optString("prix", "N/A");
                    String conseil = itemData.optString("conseil", "No advice");

                    Item item = new Item(id, image, nom, prix, conseil, timeDifference, categorieName, regionName, typeName);
                    items.add(item);
                }

                // Update the RecyclerView on the main thread
                runOnUiThread(() -> {
                    updateItemsRecyclerView(items);
                    showLoading(false);

                    if (items.isEmpty()) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.no_items_in_category, selectedCategoryName),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error occurred while fetching items: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void fetchItemsWithCategoryAndLocationFilter(int categoryId, int cityId) {
        showLoading(true);

        new Thread(() -> {
            try {
                Log.d(TAG, "Starting to fetch items with category ID: " + categoryId + " and city ID: " + cityId);

                // Filter by both category and location
                String filter = "categorie_id=eq." + categoryId + "&ville_id=eq." + cityId + "&status=neq.pending";

                // Fetch items from the "produit_serv" table with the filter
                JSONArray itemsArray = SupabaseClient.queryTableWithFilter("produit_serv", filter, "*");

                Log.d(TAG, "Query returned " + itemsArray.length() + " items");

                List<Item> items = new ArrayList<>();

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
                    String image = itemData.optString("image", "");
                    String nom = itemData.optString("nom", "Unknown");
                    String prix = itemData.optString("prix", "N/A");
                    String conseil = itemData.optString("conseil", "No advice");

                    Item item = new Item(id, image, nom, prix, conseil, timeDifference, categorieName, regionName, typeName);
                    items.add(item);
                }

                // Update the RecyclerView on the main thread
                runOnUiThread(() -> {
                    updateItemsRecyclerView(items);
                    showLoading(false);

                    if (items.isEmpty()) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.no_items_in_category_location, selectedCategoryName, selectedCityName),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error occurred while fetching items: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, R.string.error_fetching_data, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String resolveForeignKey(String tableName, String keyColumn, int keyValue, String targetColumn) {
        if (keyValue == -1) return "Unknown";

        try {
            // Handle the type table specifically since it has a different column structure
            if (tableName.equals("type") && targetColumn.equals("nom")) {
                // Try with "name" instead of "nom" for the type table
                targetColumn = "name";
            }

            // For ville table, fetch both nom and image_url
            String columns = tableName.equals("ville") ? "nom,image_url" : targetColumn;
            JSONArray result = SupabaseClient.queryTable(tableName, keyColumn, String.valueOf(keyValue), columns);
            
            if (result != null && result.length() > 0) {
                JSONObject row = result.getJSONObject(0);
                String value = row.optString(targetColumn, "Unknown");
                
                // If this is a ville query and we have an image_url, update the location's image
                if (tableName.equals("ville") && row.has("image_url")) {
                    String imageUrl = row.optString("image_url", "");
                    // Update the location's image URL in the adapter
                    updateLocationImage(keyValue, imageUrl);
                }
                
                return value;
            }
            Log.w(TAG, "No results found for " + tableName + " with " + keyColumn + "=" + keyValue);
            return "Unknown";
        } catch (Exception e) {
            Log.e(TAG, "Error resolving foreign key for " + tableName + ": " + e.getMessage(), e);
            return "Unknown";
        }
    }

    private void updateLocationImage(int villeId, String imageUrl) {
        if (locationsRecyclerView != null && locationsRecyclerView.getAdapter() != null) {
            LocationAdapter adapter = (LocationAdapter) locationsRecyclerView.getAdapter();
            adapter.updateLocationImage(villeId, imageUrl);
        }
    }

    private String calculateTimeDifference(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateString);
            long diffInMillis = System.currentTimeMillis() - date.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (days == 0) {
                // Today
                return getString(R.string.today);
            } else if (days == 1) {
                // Yesterday
                return getString(R.string.yesterday);
            } else if (days < 7) {
                // Less than a week
                return getString(R.string.days_ago, days);
            } else if (days < 30) {
                // Less than a month
                long weeks = days / 7;
                return getString(R.string.weeks_ago, weeks);
            } else {
                // More than a month
                long months = days / 30;
                return getString(R.string.months_ago, months);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating time difference: " + e.getMessage(), e);
            return getString(R.string.unknown_date);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void animateItemSelection(View view) {
        // Create circular reveal animation
        int centerX = view.getWidth() / 2;
        int centerY = view.getHeight() / 2;
        float finalRadius = (float) Math.hypot(centerX, centerY);

        Animator animator = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, 0, finalRadius);
        animator.setDuration(300);

        // Apply a pulse animation
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start()
                )
                .start();
    }

    private void saveSelectedLocation(int cityId, String cityName) {
        // Save the selected location to SharedPreferences
        getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                .edit()
                .putInt("selected_city_id", cityId)
                .putString("selected_city_name", cityName)
                .apply();

        Log.d(TAG, "Saved location: " + cityName + " (ID: " + cityId + ")");
    }

    private void loadSavedLocation() {
        // Load the saved location from SharedPreferences
        selectedCityId = getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                .getInt("selected_city_id", -1);
        selectedCityName = getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                .getString("selected_city_name", "");

        if (selectedCityId != -1) {
            Log.d(TAG, "Loaded saved location: " + selectedCityName + " (ID: " + selectedCityId + ")");

            // Update filter chips
            updateFilterChipsVisibility();

            // Fetch items with the saved location filter
            fetchItemsWithLocationFilter(selectedCityId);
        } else {
            // No saved location, fetch all items
            fetchItemsFromDatabase();
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

        // Apply current filters
        applyCurrentFilters();

        Toast.makeText(this, R.string.location_filter_cleared, Toast.LENGTH_SHORT).show();
    }

    private void clearCategoryFilter() {
        selectedCategoryId = -1;
        selectedCategoryName = "";

        // Apply current filters
        applyCurrentFilters();

        Toast.makeText(this, R.string.category_filter_cleared, Toast.LENGTH_SHORT).show();
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
                                .putInt("user_role_id", Integer.parseInt(roleId))
                                .apply();

                        // Update UI based on role
                        runOnUiThread(this::updateUIBasedOnRole);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading user role: " + e.getMessage(), e);
            }
        }).start();
    }

    private void updateUIBasedOnRole() {
        // Get user role from SharedPreferences
        int userRoleId = getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                .getInt("user_role_id", -1);

        // No FAB-related code here
        Log.d(TAG, "User role ID: " + userRoleId);
    }

    // Add this method to handle location selection from the map
    private void openLocationMap() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivityForResult(intent, REQUEST_LOCATION_SELECTION);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // Add this method to handle the result from MapActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_LOCATION_SELECTION) {
                // Handle location selection result
                selectedCityId = data.getIntExtra("city_id", -1);
                selectedCityName = data.getStringExtra("city_name");

                if (selectedCityId != -1) {
                    Log.d(TAG, "Selected city: " + selectedCityName + " (ID: " + selectedCityId + ")");
                    Toast.makeText(this, getString(R.string.location_selected, selectedCityName), Toast.LENGTH_SHORT).show();

                    // Save the selected location
                    saveSelectedLocation(selectedCityId, selectedCityName);

                    // Update filter chips
                    updateFilterChipsVisibility();

                    // Refresh the items with the selected location filter
                    fetchItemsWithLocationFilter(selectedCityId);
                }
            } else if (requestCode == REQUEST_ADD_PRODUCT || requestCode == REQUEST_EDIT_PRODUCT) {
                // Refresh the product list after adding or editing a product
                applyCurrentFilters();

                Toast.makeText(this, requestCode == REQUEST_ADD_PRODUCT ?
                                R.string.product_added_successfully : R.string.product_updated_successfully,
                        Toast.LENGTH_SHORT).show();
            }
        }
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
        showLoading(true);

        new Thread(() -> {
            try {
                // Delete the product from the database
                String path = "produit_serv?id=eq." + productId;
                SupabaseClient.deleteRecord(path);

                // Update the UI
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, R.string.product_deleted_successfully, Toast.LENGTH_SHORT).show();

                    // Remove the item from the list and update the adapter
                    ItemAdapter adapter = (ItemAdapter) updatesRecyclerView.getAdapter();
                    if (adapter != null) {
                        List<Item> items = new ArrayList<>(adapter.getItems());
                        items.remove(position);
                        adapter.updateItems(items);

                        // Show/hide empty state
                        if (items.isEmpty()) {
                            TextView emptyUpdatesText = findViewById(R.id.empty_updates_text);
                            emptyUpdatesText.setVisibility(View.VISIBLE);
                            updatesRecyclerView.setVisibility(View.GONE);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error deleting product: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, R.string.error_deleting_product, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
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
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
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
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                    // Finish the current activity
                    finish();

                    // Show a toast message
                    Toast.makeText(MainActivity.this, R.string.logged_out_successfully, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home, just close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_categories) {
            // Show categories section
            scrollToSection(findViewById(R.id.categories_section));
        } else if (id == R.id.nav_locations) {
            // Open MapActivity for result to get the selected location
            openLocationMap();
        } else if (id == R.id.nav_collabs) {
            // Open the CollabActivity
            Intent intent = new Intent(MainActivity.this, CollabActivity.class);
            startActivity(intent);
            // Apply custom animation
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_chats) {
            // Open the ChatListActivity
            Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
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
            Toast.makeText(this, R.string.settings, Toast.LENGTH_SHORT).show();
            // Future implementation: open settings activity
        } else if (id == R.id.nav_logout) {
            logout();
        } else if (id == R.id.nav_ai_pricing) {
            startActivity(new Intent(MainActivity.this, AIPricingActivity.class));
            // Apply custom animation
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void scrollToSection(View section) {
        // Scroll to the section with animation
        androidx.core.widget.NestedScrollView scrollView = findViewById(R.id.nested_scroll_view);
        if (scrollView != null && section != null) {
            int[] location = new int[2];
            section.getLocationInWindow(location);

            // Apply a highlight animation to the section
            section.setBackgroundColor(getResources().getColor(R.color.highlight_color, null));
            section.animate()
                    .alpha(0.7f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        section.animate()
                                .alpha(1.0f)
                                .setDuration(300)
                                .withEndAction(() -> {
                                    section.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
                                })
                                .start();
                    })
                    .start();

            // Scroll to the section
            scrollView.smoothScrollTo(0, location[1] - 200);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if we need to refresh data
        boolean needsRefresh = getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                .getBoolean("needs_data_refresh", false);

        if (needsRefresh) {
            // Clear the flag
            getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("needs_data_refresh", false)
                    .apply();

            // Refresh data
            refreshAllData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }

    private boolean isLayoutProperlyLoaded() {
        boolean isProper = true;

        if (categoriesRecyclerView == null) {
            Log.e(TAG, "categoriesRecyclerView is null - layout may be incorrect");
            isProper = false;
        }

        if (locationsRecyclerView == null) {
            Log.e(TAG, "locationsRecyclerView is null - layout may be incorrect");
            isProper = false;
        }

        if (updatesRecyclerView == null) {
            Log.e(TAG, "updatesRecyclerView is null - layout may be incorrect");
            isProper = false;
        }

        return isProper;
    }
    private void openProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);

        // Create transition animation
        ActivityOptionsCompat options = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Pair<View, String> pair = Pair.create(
                    (View) profileIcon,
                    "profile_image_transition"
            );
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pair);
        }

        // Start activity with transition
        if (options != null) {
            profileLauncher.launch(intent, options);
        } else {
            profileLauncher.launch(intent);
        }

        // Apply custom animation if not using shared element transition
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void updateUserInfo() {
        // Refresh user information in the navigation drawer
        TextView userNameView = findViewById(R.id.user_name);
        TextView userEmailView = findViewById(R.id.user_email);

        if (userNameView != null) {
            String userName = SharedPreferencesManager.getInstance(this).getUserName();
            if (userName != null && !userName.isEmpty()) {
                userNameView.setText(userName);
            }
        }

        if (userEmailView != null) {
            String userEmail = SharedPreferencesManager.getInstance(this).getUserEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                userEmailView.setText(userEmail);
            }
        }

        // Refresh the profile image
        loadUserProfileImage();
    }
    private void loadUserProfileImage() {
        // Get user ID from SharedPreferences
        int userId = getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                .getInt("user_id", -1);

        if (userId == -1) {
            return; // User not logged in
        }

        // Find the profile icon ImageView
        de.hdodenhof.circleimageview.CircleImageView profileIcon = findViewById(R.id.profile_icon);
        if (profileIcon == null) {
            Log.e(TAG, "Profile icon view not found");
            return;
        }

        // Fetch user profile image in background
        new Thread(() -> {
            try {
                // Query the user to get their profile image URL
                JSONArray userResult = SupabaseClient.queryTable(
                        "utilisateur",
                        "id",
                        String.valueOf(userId),
                        "image");

                if (userResult.length() > 0) {
                    String profileImageUrl = userResult.getJSONObject(0).optString("image", "");

                    if (!profileImageUrl.isEmpty()) {
                        // Load the image on the UI thread using Glide or similar library
                        runOnUiThread(() -> {
                            // You'll need to add Glide to your dependencies if not already added
                            try {
                                // Load image with Glide
                                com.bumptech.glide.Glide.with(MainActivity.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.user)
                                        .error(R.drawable.user)
                                        .into(profileIcon);
                            } catch (Exception e) {
                                Log.e(TAG, "Error loading profile image with Glide: " + e.getMessage(), e);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading user profile image: " + e.getMessage(), e);
            }
        }).start();
    }
}
