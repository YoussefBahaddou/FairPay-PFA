package com.emsi.fairpay_maroc.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.models.Category;
import com.emsi.fairpay_maroc.utils.ImageHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductActivity extends BaseActivity {
    private static final String TAG = "ProductActivity";
    
    // Constants for operation modes
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_PRODUCT_ID = "product_id";
    public static final int MODE_VIEW = 0;
    public static final int MODE_ADD = 1;
    public static final int MODE_EDIT = 2;
    
    // UI components
    private ImageView ivProductImage;
    private EditText etProductName;
    private EditText etProductPrice;
    private EditText etProductAdvice;
    private Spinner spinnerCategory;
    private Spinner spinnerCity;
    private Spinner spinnerType;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private TextView tvTitle;
    
    // Data
    private int currentMode;
    private int productId = -1;
    private Uri selectedImageUri;
    private boolean imageChanged = false;
    private int userId;
    private int userRoleId;
    private JSONObject productData;
    
    // Category, City, and Type data
    private List<Category> categories = new ArrayList<>();
    private Map<String, Integer> categoryNameToId = new HashMap<>();
    private List<String> cities = new ArrayList<>();
    private Map<String, Integer> cityNameToId = new HashMap<>();
    private List<String> types = new ArrayList<>();
    private Map<String, Integer> typeNameToId = new HashMap<>();
    
    // Activity result launcher for image selection
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        ivProductImage.setImageURI(selectedImageUri);
                        imageChanged = true;
                    }
                }
            });
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        
        // Initialize UI components
        initViews();
        
        // Get user info
        loadUserInfo();
        
        // Get operation mode and product ID if editing
        currentMode = getIntent().getIntExtra(EXTRA_MODE, MODE_VIEW);
        productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        
        // Set up the UI based on mode
        setupUIForMode();
        
        // Load data
        fetchCategories();
        fetchCities();
        fetchTypes();
        
        // If editing or viewing, load product data
        if (currentMode == MODE_ADD) {
            // Add new product
            JSONObject data = new JSONObject();
            String productName = etProductName.getText().toString().trim();
            String productPrice = etProductPrice.getText().toString().trim();
            String productAdvice = etProductAdvice.getText().toString().trim();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());
            int selectedCategoryId = spinnerCategory.getSelectedItemPosition() + 1;
            int selectedCityId = spinnerCity.getSelectedItemPosition() + 1;
            int selectedTypeId = spinnerType.getSelectedItemPosition() + 1;
            try {
                data.put("nom", productName);
                data.put("prix", productPrice);
                data.put("conseil", productAdvice);
                data.put("datemiseajour", currentDate);
                data.put("categorie_id", selectedCategoryId);
                data.put("ville_id", selectedCityId);
                data.put("type_id", selectedTypeId);
                data.put("submitted_by", userId);
                data.put("status", "pending"); // Set status to pending for new products
                
                // Handle image if available
                if (imageChanged && selectedImageUri != null) {
                    // Process and add image data
                    // ...
                }
                
                // Submit the product
                JSONObject result = SupabaseClient.insertIntoTable("produit_serv", data);
                
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, R.string.product_added_successfully, Toast.LENGTH_SHORT).show();
                    
                    // Return to previous screen
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (JSONException e) {
                Log.e("ProductActivity", "Error creating JSON data: " + e.getMessage(), e);
                Toast.makeText(this, "Error saving product data", Toast.LENGTH_SHORT).show();
                return; // Return from the method or handle the error appropriately
            } catch (IOException e) {
                Log.e("ProductActivity", "Network error while saving product: " + e.getMessage(), e);
                Toast.makeText(this, "Network error while saving product", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("ProductActivity", "Error saving product: " + e.getMessage(), e);
                Toast.makeText(this, "Error saving product", Toast.LENGTH_SHORT).show();
            }
        }
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
        
        // Title
        tvTitle = findViewById(R.id.tv_title);
        
        // Product image
        ivProductImage = findViewById(R.id.iv_product_image);
        ImageButton btnSelectImage = findViewById(R.id.btn_select_image);
        btnSelectImage.setOnClickListener(v -> openImagePicker());
        
        // Form fields
        etProductName = findViewById(R.id.et_product_name);
        etProductPrice = findViewById(R.id.et_product_price);
        etProductAdvice = findViewById(R.id.et_product_advice);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerCity = findViewById(R.id.spinner_city);
        spinnerType = findViewById(R.id.spinner_type);
        
        // Submit button and progress bar
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(v -> handleSubmit());
        progressBar = findViewById(R.id.progress_bar);
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
    
    private void setupUIForMode() {
        switch (currentMode) {
            case MODE_VIEW:
                tvTitle.setText(R.string.view_product);
                btnSubmit.setVisibility(View.GONE);
                setFieldsEditable(false);
                break;
                
            case MODE_ADD:
                tvTitle.setText(R.string.add_product);
                btnSubmit.setText(R.string.submit);
                setFieldsEditable(true);
                break;
                
            case MODE_EDIT:
                tvTitle.setText(R.string.edit_product);
                btnSubmit.setText(R.string.update);
                setFieldsEditable(true);
                break;
        }
    }
    
    private void setFieldsEditable(boolean editable) {
        etProductName.setEnabled(editable);
        etProductPrice.setEnabled(editable);
        etProductAdvice.setEnabled(editable);
        spinnerCategory.setEnabled(editable);
        spinnerCity.setEnabled(editable);
        spinnerType.setEnabled(editable);
        
        // Image selection button
        findViewById(R.id.btn_select_image).setVisibility(editable ? View.VISIBLE : View.GONE);
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    private void fetchCategories() {
        progressBar.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            try {
                JSONArray categoriesArray = SupabaseClient.queryTable("categorie", null, null, "*");
                categories.clear();
                categoryNameToId.clear();
                List<String> categoryNames = new ArrayList<>();
                
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject categoryObj = categoriesArray.getJSONObject(i);
                    int id = categoryObj.getInt("id");
                    String name = categoryObj.getString("nom");
                    String icon = categoryObj.optString("icon", "");
                    
                    categories.add(new Category(icon, name));
                    categoryNames.add(name);
                    categoryNameToId.put(name, id);
                }
                
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item, categoryNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching categories: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_fetching_categories, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();
    }
    
    private void fetchCities() {
        new Thread(() -> {
            try {
                JSONArray citiesArray = SupabaseClient.queryTable("ville", null, null, "*");
                cities.clear();
                cityNameToId.clear();
                
                for (int i = 0; i < citiesArray.length(); i++) {
                    JSONObject cityObj = citiesArray.getJSONObject(i);
                    int id = cityObj.getInt("id");
                    String name = cityObj.getString("nom");
                    
                    cities.add(name);
                    cityNameToId.put(name, id);
                }
                
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item, cities);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCity.setAdapter(adapter);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching cities: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_fetching_cities, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void fetchTypes() {
        new Thread(() -> {
            try {
                JSONArray typesArray = SupabaseClient.queryTable("type", null, null, "*");
                types.clear();
                typeNameToId.clear();
                
                for (int i = 0; i < typesArray.length(); i++) {
                    JSONObject typeObj = typesArray.getJSONObject(i);
                    int id = typeObj.getInt("id");
                    String name = typeObj.getString("name");
                    
                    types.add(name);
                    typeNameToId.put(name, id);
                }
                
                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this, android.R.layout.simple_spinner_item, types);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerType.setAdapter(adapter);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching types: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_fetching_types, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void fetchProductData(int productId) {
        progressBar.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            try {
                JSONArray result = SupabaseClient.queryTable(
                        "produit_serv", 
                        "id", 
                        String.valueOf(productId), 
                        "*");
                
                if (result.length() > 0) {
                    productData = result.getJSONObject(0);
                    
                    runOnUiThread(() -> {
                        try {
                            // Set product data to UI
                            etProductName.setText(productData.getString("nom"));
                            etProductPrice.setText(String.valueOf(productData.getDouble("prix")));
                            etProductAdvice.setText(productData.optString("conseil", ""));
                            
                            // Load image
                            String imagePath = productData.optString("image", "");
                            if (!TextUtils.isEmpty(imagePath)) {
                                ImageHelper.loadImage(this, imagePath, ivProductImage);
                            }
                            
                            // Set spinners
                            setSpinnerSelection(spinnerCategory, categoryNameToId, 
                                    productData.getInt("categorie_id"));
                            setSpinnerSelection(spinnerCity, cityNameToId, 
                                    productData.getInt("ville_id"));
                            setSpinnerSelection(spinnerType, typeNameToId, 
                                    productData.getInt("type_id"));
                            
                            progressBar.setVisibility(View.GONE);
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting product data: " + e.getMessage(), e);
                            Toast.makeText(this, R.string.error_loading_product, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, R.string.error_product_not_found, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        finish();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching product: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_loading_product, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                });
            }
        }).start();
    }
    
    private void setSpinnerSelection(Spinner spinner, Map<String, Integer> nameToIdMap, int targetId) {
        for (Map.Entry<String, Integer> entry : nameToIdMap.entrySet()) {
            if (entry.getValue() == targetId) {
                String targetName = entry.getKey();
                ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
                if (adapter != null) {
                    int position = adapter.getPosition(targetName);
                    if (position >= 0) {
                        spinner.setSelection(position);
                    }
                }
                break;
            }
        }
    }
    
    private void handleSubmit() {
        if (!validateForm()) {
            return;
        }
        
        // Disable button and show progress
        btnSubmit.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        
        // Prepare data
        String productName = etProductName.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        double productPrice = Double.parseDouble(priceStr);
        String productAdvice = etProductAdvice.getText().toString().trim();
        
        // Get selected IDs
        String selectedCategory = (String) spinnerCategory.getSelectedItem();
        String selectedCity = (String) spinnerCity.getSelectedItem();
        String selectedType = (String) spinnerType.getSelectedItem();
        
        int selectedCategoryId = categoryNameToId.get(selectedCategory);
        int selectedCityId = cityNameToId.get(selectedCity);
        int selectedTypeId = typeNameToId.get(selectedType);
        
        // Current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        
        // Process in background
        new Thread(() -> {
            try {
                JSONObject data = new JSONObject();
                data.put("nom", productName);
                data.put("prix", productPrice);
                data.put("conseil", productAdvice);
                data.put("datemiseajour", currentDate);
                data.put("categorie_id", selectedCategoryId);
                data.put("ville_id", selectedCityId);
                data.put("type_id", selectedTypeId);
                
                // Handle image if changed
                if (imageChanged && selectedImageUri != null) {
                    // Show progress dialog for image upload
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Processing image...", Toast.LENGTH_SHORT).show();
                    });
                    
                    // Process image using our helper
                    String imageData = ImageHelper.uploadImage(this, selectedImageUri);
                    
                    if (imageData != null) {
                        // Add the image data to the product data
                        data.put("image", imageData);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, R.string.error_processing_image, Toast.LENGTH_SHORT).show();
                        });
                    }
                } else if (currentMode == MODE_EDIT && productData != null) {
                    // Keep existing image if not changed
                    data.put("image", productData.optString("image", ""));
                }
                
                // Add or update based on mode
                if (currentMode == MODE_ADD) {
                    // Add new product
                    JSONObject result = SupabaseClient.insertIntoTable("produit_serv", data);
                    
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnSubmit.setEnabled(true);
                        Toast.makeText(this, R.string.product_added_successfully, Toast.LENGTH_SHORT).show();
                        
                        // Return to previous screen
                        setResult(RESULT_OK);
                        finish();
                    });
                } else if (currentMode == MODE_EDIT) {
                    // Update existing product
                    String path = "produit_serv?id=eq." + productId;
                    JSONObject result = SupabaseClient.updateRecord(path, data);
                    
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnSubmit.setEnabled(true);
                        Toast.makeText(this, R.string.product_updated_successfully, Toast.LENGTH_SHORT).show();
                        
                        // Return to previous screen
                        setResult(RESULT_OK);
                        finish();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error submitting product: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, R.string.error_submitting_product + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    private boolean validateForm() {
        boolean valid = true;
        
        // Validate product name
        String productName = etProductName.getText().toString().trim();
        if (TextUtils.isEmpty(productName)) {
            etProductName.setError(getString(R.string.field_required));
            valid = false;
        } else {
            etProductName.setError(null);
        }
        
        // Validate price
        String priceStr = etProductPrice.getText().toString().trim();
        if (TextUtils.isEmpty(priceStr)) {
            etProductPrice.setError(getString(R.string.field_required));
            valid = false;
        } else {
            try {
                double price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    etProductPrice.setError(getString(R.string.price_must_be_positive));
                    valid = false;
                } else {
                    etProductPrice.setError(null);
                }
            } catch (NumberFormatException e) {
                etProductPrice.setError(getString(R.string.invalid_price));
                valid = false;
            }
        }
        
        // Validate spinners
        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, R.string.please_select_category, Toast.LENGTH_SHORT).show();
            valid = false;
        }
        
        if (spinnerCity.getSelectedItem() == null) {
            Toast.makeText(this, R.string.please_select_city, Toast.LENGTH_SHORT).show();
            valid = false;
        }
        
        if (spinnerType.getSelectedItem() == null) {
            Toast.makeText(this, R.string.please_select_type, Toast.LENGTH_SHORT).show();
            valid = false;
        }
        
        // Validate image for new products
        if (currentMode == MODE_ADD && selectedImageUri == null) {
            Toast.makeText(this, R.string.please_select_image, Toast.LENGTH_SHORT).show();
            valid = false;
        }
        
        return valid;
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
