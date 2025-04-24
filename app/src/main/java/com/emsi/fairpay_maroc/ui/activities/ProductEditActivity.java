package com.emsi.fairpay_maroc.ui.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.utils.ImageHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import android.os.Handler;
import android.os.Looper;
import android.graphics.Bitmap;
import okhttp3.MediaType;
public class ProductEditActivity extends AppCompatActivity {
    private static final String TAG = "ProductEditActivity";
    private static final int REQUEST_IMAGE_PICK = 1001;

    // UI components
    private EditText etProductName, etPrice, etAdvice;
    private Spinner spinnerCategory, spinnerLocation, spinnerType;
    private ImageView ivProductImage;
    private Button btnUpdate;
    private ProgressBar progressBar;

    // Data
    private String productId;
    private Uri selectedImageUri;
    private boolean imageChanged = false;
    private Map<String, Integer> categoryMap = new HashMap<>();
    private Map<String, Integer> locationMap = new HashMap<>();
    private Map<String, Integer> typeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);

        // Get product ID from intent
        productId = getIntent().getStringExtra("product_id");
        if (productId == null) {
            Toast.makeText(this, "Error: Product ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI components
        initializeViews();

        // Set up toolbar
        setupToolbar();

        // Load categories, locations, and types
        loadSpinnerData();

        // Load product data
        loadProductData();

        // Set up image selection
        ivProductImage.setOnClickListener(v -> openImagePicker());

        // Set up update button
        btnUpdate.setOnClickListener(v -> validateAndUpdateProduct());
    }

    private void initializeViews() {
        etProductName = findViewById(R.id.et_product_name);
        etPrice = findViewById(R.id.et_price);
        etAdvice = findViewById(R.id.et_advice);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerLocation = findViewById(R.id.spinner_location);
        spinnerType = findViewById(R.id.spinner_type);
        ivProductImage = findViewById(R.id.iv_product_image);
        btnUpdate = findViewById(R.id.btn_update);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadSpinnerData() {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Set a timeout handler to prevent indefinite waiting
        final Handler timeoutHandler = new Handler(Looper.getMainLooper());
        final Runnable timeoutRunnable = () -> {
            Log.e(TAG, "Spinner data loading timed out");
            Toast.makeText(this, "Loading timed out. Please try again.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            finish();
        };

        // Set a 10-second timeout
        timeoutHandler.postDelayed(timeoutRunnable, 10000);

        new Thread(() -> {
            try {
                // Load categories
                Log.d(TAG, "Loading categories...");
                JSONArray categoriesArray = SupabaseClient.queryTable("categorie", null, null, "*");
                String[] categories = new String[categoriesArray.length()];

                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject category = categoriesArray.getJSONObject(i);
                    String name = category.optString("nom", "Category " + i);
                    int id = category.optInt("id", -1);
                    categories[i] = name;
                    categoryMap.put(name, id);
                }

                // Load locations (cities)
                Log.d(TAG, "Loading locations...");
                JSONArray locationsArray = SupabaseClient.queryTable("ville", null, null, "*");
                String[] locations = new String[locationsArray.length()];

                for (int i = 0; i < locationsArray.length(); i++) {
                    JSONObject location = locationsArray.getJSONObject(i);
                    String name = location.optString("nom", "Location " + i);
                    int id = location.optInt("id", -1);
                    locations[i] = name;
                    locationMap.put(name, id);
                }

                // Load types
                Log.d(TAG, "Loading types...");
                JSONArray typesArray = SupabaseClient.queryTable("type", null, null, "*");
                String[] types = new String[typesArray.length()];

                for (int i = 0; i < typesArray.length(); i++) {
                    JSONObject type = typesArray.getJSONObject(i);
                    String name = type.optString("name", "Type " + i);
                    int id = type.optInt("id", -1);
                    types[i] = name;
                    typeMap.put(name, id);
                }

                // Cancel the timeout since we got all the data
                timeoutHandler.removeCallbacks(timeoutRunnable);

                // Update UI on main thread
                final String[] finalCategories = categories;
                final String[] finalLocations = locations;
                final String[] finalTypes = types;

                runOnUiThread(() -> {
                    try {
                        // Set up category spinner
                        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_spinner_item, finalCategories);
                        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerCategory.setAdapter(categoryAdapter);

                        // Set up location spinner
                        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_spinner_item, finalLocations);
                        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerLocation.setAdapter(locationAdapter);

                        // Set up type spinner
                        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                                this, android.R.layout.simple_spinner_item, finalTypes);
                        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerType.setAdapter(typeAdapter);

                        // Now that spinners are set up, load the product data
                        loadProductData();
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting up spinners: " + e.getMessage(), e);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProductEditActivity.this,
                                "Error setting up form: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                // Cancel the timeout since we got an error
                timeoutHandler.removeCallbacks(timeoutRunnable);

                Log.e(TAG, "Error loading spinner data: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProductEditActivity.this,
                            "Error loading data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity on error
                });
            }
        }).start();
    }

    private void loadProductData() {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Set a timeout handler to prevent indefinite waiting
        final Handler timeoutHandler = new Handler(Looper.getMainLooper());
        final Runnable timeoutRunnable = () -> {
            Log.e(TAG, "Product data loading timed out");
            Toast.makeText(this, "Loading timed out. Please try again.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            finish();
        };

        // Set a 10-second timeout
        timeoutHandler.postDelayed(timeoutRunnable, 10000);

        new Thread(() -> {
            try {
                // Query the product
                Log.d(TAG, "Querying product with ID: " + productId);
                JSONArray productArray = SupabaseClient.queryTable(
                        "produit_serv",
                        "id",
                        productId,
                        "*");

                // Cancel the timeout since we got a response
                timeoutHandler.removeCallbacks(timeoutRunnable);

                if (productArray.length() == 0) {
                    Log.e(TAG, "Product not found with ID: " + productId);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        finish();
                    });
                    return;
                }

                JSONObject product = productArray.getJSONObject(0);
                Log.d(TAG, "Product data retrieved: " + product.toString());

                // Extract product data with safe getters to prevent crashes
                final String name = product.optString("nom", "");
                final double price = product.optDouble("prix", 0.0);
                final String advice = product.optString("conseil", "");
                final int categoryId = product.optInt("categorie_id", -1);
                final int locationId = product.optInt("ville_id", -1);
                final int typeId = product.optInt("type_id", -1);
                final String imagePath = product.optString("image", "");

                // Find the category, location, and type names
                final String categoryName = getCategoryNameById(categoryId);
                final String locationName = getLocationNameById(locationId);
                final String typeName = getTypeNameById(typeId);

                // Update UI on main thread
                runOnUiThread(() -> {
                    try {
                        // Set text fields
                        etProductName.setText(name);
                        etPrice.setText(String.valueOf(price));
                        etAdvice.setText(advice);

                        // Set spinners if values are valid
                        if (!categoryName.isEmpty()) {
                            setSpinnerSelection(spinnerCategory, categoryName);
                        }

                        if (!locationName.isEmpty()) {
                            setSpinnerSelection(spinnerLocation, locationName);
                        }

                        if (!typeName.isEmpty()) {
                            setSpinnerSelection(spinnerType, typeName);
                        }

                        // Load image if available
                        if (!imagePath.isEmpty()) {
                            loadProductImage(imagePath);
                        } else {
                            ivProductImage.setImageResource(R.drawable.placeholder_image);
                        }

                        progressBar.setVisibility(View.GONE);
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating UI with product data: " + e.getMessage(), e);
                        Toast.makeText(ProductEditActivity.this,
                                "Error displaying product data: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });

            } catch (Exception e) {
                // Cancel the timeout since we got an error
                timeoutHandler.removeCallbacks(timeoutRunnable);

                Log.e(TAG, "Error loading product data: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProductEditActivity.this,
                            "Error loading product: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity on error
                });
            }
        }).start();
    }

    private void loadProductImage(String imagePath) {
        // Use our helper to load the image
        ImageHelper.loadImage(this, imagePath, ivProductImage);
    }

    private String getCategoryNameById(int id) {
        try {
            if (id == -1) return "";

            for (Map.Entry<String, Integer> entry : categoryMap.entrySet()) {
                if (entry.getValue() == id) {
                    return entry.getKey();
                }
            }

            Log.w(TAG, "Category ID not found: " + id);
            return "";
        } catch (Exception e) {
            Log.e(TAG, "Error getting category name: " + e.getMessage(), e);
            return "";
        }
    }

    private String getLocationNameById(int id) {
        try {
            if (id == -1) return "";

            for (Map.Entry<String, Integer> entry : locationMap.entrySet()) {
                if (entry.getValue() == id) {
                    return entry.getKey();
                }
            }

            Log.w(TAG, "Location ID not found: " + id);
            return "";
        } catch (Exception e) {
            Log.e(TAG, "Error getting location name: " + e.getMessage(), e);
            return "";
        }
    }

    private String getTypeNameById(int id) {
        try {
            if (id == -1) return "";

            for (Map.Entry<String, Integer> entry : typeMap.entrySet()) {
                if (entry.getValue() == id) {
                    return entry.getKey();
                }
            }

            Log.w(TAG, "Type ID not found: " + id);
            return "";
        } catch (Exception e) {
            Log.e(TAG, "Error getting type name: " + e.getMessage(), e);
            return "";
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        try {
            if (value == null || value.isEmpty() || spinner == null || spinner.getAdapter() == null) {
                Log.w(TAG, "Cannot set spinner selection: invalid parameters");
                return;
            }

            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                Object item = adapter.getItem(i);
                if (item != null && item.toString().equals(value)) {
                    spinner.setSelection(i);
                    return;
                }
            }

            Log.w(TAG, "Value not found in spinner: " + value);
        } catch (Exception e) {
            Log.e(TAG, "Error setting spinner selection: " + e.getMessage(), e);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageChanged = true;

            // Display the selected image
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(ivProductImage);
        }
    }

    private void validateAndUpdateProduct() {
        // Get input values
        String productName = etProductName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String advice = etAdvice.getText().toString().trim();
        String selectedCategory = spinnerCategory.getSelectedItem().toString();
        String selectedLocation = spinnerLocation.getSelectedItem().toString();
        String selectedType = spinnerType.getSelectedItem().toString();

        // Validate inputs
        if (productName.isEmpty()) {
            etProductName.setError("Product name is required");
            etProductName.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }

        float price;
        try {
            price = Float.parseFloat(priceStr);
            if (price <= 0) {
                etPrice.setError("Price must be greater than zero");
                etPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            etPrice.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        // Update product in background thread
        new Thread(() -> {
            try {
                // Get IDs from the maps
                int categoryId = categoryMap.get(selectedCategory);
                int locationId = locationMap.get(selectedLocation);
                int typeId = typeMap.get(selectedType);

                // First, get the current status from the product data
                String currentStatus = "pending"; // Default to pending if not found
                try {
                    JSONArray productArray = SupabaseClient.queryTable(
                            "produit_serv",
                            "id",
                            productId,
                            "status");
                    
                    if (productArray.length() > 0) {
                        currentStatus = productArray.getJSONObject(0).optString("status", "pending");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting current product status: " + e.getMessage());
                }

                // Then include it in the update data
                JSONObject productData = new JSONObject();
                productData.put("nom", productName);
                productData.put("prix", price);
                productData.put("conseil", advice);
                productData.put("datemiseajour", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                productData.put("categorie_id", categoryId);
                productData.put("ville_id", locationId);
                productData.put("type_id", typeId);
                productData.put("status", currentStatus); // Preserve the current status

                // Handle image if changed
                if (imageChanged && selectedImageUri != null) {
                    // Show progress dialog for image upload
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Processing image...", Toast.LENGTH_SHORT).show();
                    });
                    
                    // Process image using our helper
                    String imageData = ImageHelper.uploadImage(this, selectedImageUri);
                    
                    if (imageData == null) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnUpdate.setEnabled(true);
                            Toast.makeText(this, "Failed to process image. Continuing with update without changing the image.", Toast.LENGTH_LONG).show();
                        });
                        
                        // Continue with the update without changing the image
                    } else {
                        // Add the image data to the product data
                        productData.put("image", imageData);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating product: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(ProductEditActivity.this,
                            "Error updating product: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String encodeImageToBase64(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Could not open input stream for image");
        }

        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        inputStream.close();

        byte[] imageBytes = byteBuffer.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private String uploadImageToStorage(Uri imageUri) throws IOException, JSONException {
        // Check if the bucket exists and create it if needed
        SupabaseClient.ensureBucketExists("images");
        
        // Get the file extension
        String fileExtension = getFileExtension(imageUri);
        String contentType = getMimeType(imageUri);
        
        // Convert HEIC to JPEG if needed
        if ("heic".equalsIgnoreCase(fileExtension) || "heif".equalsIgnoreCase(fileExtension)) {
            // Convert HEIC to JPEG
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream jpegStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, jpegStream);
            byte[] jpegBytes = jpegStream.toByteArray();
            
            // Generate a unique filename with jpeg extension
            String fileName = UUID.randomUUID().toString() + ".jpg";
            
            // Upload to Supabase Storage
            return SupabaseClient.uploadFile("images", fileName, jpegBytes, "image/jpeg");
        } else {
            // For other image formats, proceed normally
            // Generate a unique filename
            String fileName = UUID.randomUUID().toString() + "." + (fileExtension != null ? fileExtension : "jpg");
            
            // Convert the image to bytes
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            inputStream.close();
            byte[] imageBytes = byteBuffer.toByteArray();
            
            // Upload to Supabase Storage
            return SupabaseClient.uploadFile("images", fileName, imageBytes, contentType != null ? contentType : "image/jpeg");
        }
    }
    
    private String getMimeType(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String mimeType = contentResolver.getType(uri);
        if (mimeType == null) {
            // Try to guess from the file extension
            String fileExtension = getFileExtension(uri);
            if (fileExtension != null) {
                if ("heic".equalsIgnoreCase(fileExtension) || "heif".equalsIgnoreCase(fileExtension)) {
                    return "image/heic";
                }
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
            }
            return "image/jpeg"; // Default
        }
        return mimeType;
    }
    

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        if (extension == null) {
            // Try to get it from the last path segment
            String path = uri.getPath();
            if (path != null) {
                int i = path.lastIndexOf('.');
                if (i > 0) {
                    extension = path.substring(i + 1);
                }
            }
        }
        return extension;
    }
}
