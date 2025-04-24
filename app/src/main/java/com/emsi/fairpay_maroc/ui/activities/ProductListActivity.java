package com.emsi.fairpay_maroc.ui.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.adapters.ItemAdapter;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.models.Item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductListActivity extends BaseActivity {
    private static final String TAG = "ProductListActivity";
    private static final int REQUEST_ADD_PRODUCT = 1001;
    private static final int REQUEST_EDIT_PRODUCT = 1002;
    
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private int userId;
    private int userRoleId;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        
        // Initialize UI components
        initViews();
        
        // Get user info
        loadUserInfo();
        
        // Load products
        loadProducts();
    }
    
    private void initViews() {
        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        // Back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());
        
        // RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Progress bar and empty state
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        
        // FAB for adding new products
        FloatingActionButton fabAddProduct = findViewById(R.id.fab_add_product);
        fabAddProduct.setOnClickListener(v -> addNewProduct());
    }
    
    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("FairPayPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        userRoleId = prefs.getInt("user_role_id", -1);
        
        if (userId == -1) {
            Toast.makeText(this, R.string.error_user_not_logged_in, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        
        new Thread(() -> {
            try {
                // Query products based on user role, excluding pending status
                JSONArray productsArray;
                
                if (userRoleId == 2) { // Producer
                    // For producers, we might want to filter by producer_id if that's in your schema
                    // For now, just get all approved products
                    productsArray = SupabaseClient.queryTableWithFilter("produit_serv", "status=neq.pending", "*");
                } else if (userRoleId == 3) { // Contributor
                    // For contributors, we might want to filter by contributor_id if that's in your schema
                    // For now, just get all approved products
                    productsArray = SupabaseClient.queryTableWithFilter("produit_serv", "status=neq.pending", "*");
                } else {
                    // Regular users shouldn't be here
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.error_unauthorized, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
                
                // Parse products
                List<Item> products = new ArrayList<>();
                
                for (int i = 0; i < productsArray.length(); i++) {
                    JSONObject productData = productsArray.getJSONObject(i);
                    
                    // Get the product ID
                    int id = productData.optInt("id", -1);
                    
                    // Resolve foreign key data
                    String categorieName = resolveForeignKey("categorie", "id", productData.optInt("categorie_id", -1), "nom");
                    String regionName = resolveForeignKey("ville", "id", productData.optInt("ville_id", -1), "nom");
                    String typeName = resolveForeignKey("type", "id", productData.optInt("type_id", -1), "name");
                    
                    // Calculate the difference between current date and "datemiseajour"
                    String datemiseajour = productData.optString("datemiseajour", "Unknown date");
                    String timeDifference = calculateTimeDifference(datemiseajour);
                    
                    // Map the data to an Item object
                    String image = productData.optString("image", "");
                    String nom = productData.optString("nom", "Unknown");
                    String prix = productData.optString("prix", "N/A");
                    String conseil = productData.optString("conseil", "No advice");
                    
                    Item item = new Item(id, image, nom, prix, conseil, timeDifference, categorieName, regionName, typeName);
                    products.add(item);
                }
                
                // Update UI
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (products.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        setupAdapter(products);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading products: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText(R.string.error_loading_products);
                    Toast.makeText(this, R.string.error_loading_products, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void setupAdapter(List<Item> products) {
        ItemAdapter adapter = new ItemAdapter(products);
        
        // Set click listener for viewing items
        adapter.setOnItemClickListener((item, position) -> {
            viewProduct(item.getId());
        });
        
        // Set action listener for editing/deleting items
        adapter.setOnItemActionListener(new ItemAdapter.OnItemActionListener() {
            @Override
            public void onEditItem(Item item, int position) {
                editProduct(item.getId());
            }
            
            @Override
            public void onDeleteItem(Item item, int position) {
                showDeleteConfirmationDialog(item.getId(), position);
            }
        });
        
        recyclerView.setAdapter(adapter);
    }
    
    private String resolveForeignKey(String tableName, String keyColumn, int keyValue, String targetColumn) {
        try {
            if (keyValue == -1) return "Unknown";
            JSONArray result = SupabaseClient.queryTable(tableName, keyColumn, String.valueOf(keyValue), targetColumn);
            if (result.length() > 0) {
                return result.getJSONObject(0).optString(targetColumn, "Unknown");
            }
            return "Unknown";
        } catch (Exception e) {
            Log.e(TAG, "Error resolving foreign key: " + e.getMessage(), e);
            return "Unknown";
        }
    }
    
    private String calculateTimeDifference(String dateString) {
        // Reuse the same method from MainActivity
        // This should be moved to a utility class in a real app
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(dateString);
            long diffInMillis = System.currentTimeMillis() - date.getTime();
            long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillis);
            return days + " jours";
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown date";
        }
    }
    
    private void viewProduct(int productId) {
        Intent intent = new Intent(this, ProductActivity.class);
        intent.putExtra(ProductActivity.EXTRA_MODE, ProductActivity.MODE_VIEW);
        intent.putExtra(ProductActivity.EXTRA_PRODUCT_ID, productId);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    
    private void addNewProduct() {
        Intent intent = new Intent(this, ProductActivity.class);
        intent.putExtra(ProductActivity.EXTRA_MODE, ProductActivity.MODE_ADD);
        startActivityForResult(intent, REQUEST_ADD_PRODUCT);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    
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
                    ItemAdapter adapter = (ItemAdapter) recyclerView.getAdapter();
                    if (adapter != null) {
                        List<Item> items = new ArrayList<>(adapter.getItems());
                        items.remove(position);
                        adapter.updateItems(items);
                        
                        // Show empty state if no items left
                        if (items.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error deleting product: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.error_deleting_product, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_PRODUCT || requestCode == REQUEST_EDIT_PRODUCT) {
                // Refresh the product list after adding or editing a product
                loadProducts();
                
                Toast.makeText(this, requestCode == REQUEST_ADD_PRODUCT ? 
                        R.string.product_added_successfully : R.string.product_updated_successfully, 
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}