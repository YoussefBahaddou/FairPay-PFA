package com.emsi.fairpay_maroc.ui.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.utils.ImageHelper;
import com.google.android.material.textfield.TextInputEditText;

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
import java.util.UUID;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class ProductSubmissionActivity extends BaseActivity {

    private static final String TAG = "ProductSubmissionActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private TextInputEditText etProductName, etPrice, etAdvice;
    private Spinner spinnerCategory, spinnerLocation, spinnerType;
    private Button btnUploadImage, btnSubmit;
    private TextView tvImageName, toolbarTitle;
    private ImageView imgPreview;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private int userId;
    private String userRole;
    private Map<String, Integer> categoryMap = new HashMap<>();
    private Map<String, Integer> locationMap = new HashMap<>();
    private Map<String, Integer> typeMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_submission);

        // Initialize UI components
        initializeViews();
        
        // Set up toolbar and back button
        setupToolbar();
        
        // Get user info
        loadUserInfo();
        
        // Load spinners data
        loadSpinnersData();
        
        // Set up button click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        etProductName = findViewById(R.id.et_product_name);
        etPrice = findViewById(R.id.et_price);
        etAdvice = findViewById(R.id.et_advice);
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerLocation = findViewById(R.id.spinner_location);
        spinnerType = findViewById(R.id.spinner_type);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        btnSubmit = findViewById(R.id.btn_submit);
        tvImageName = findViewById(R.id.tv_image_name);
        imgPreview = findViewById(R.id.img_preview);
        progressBar = findViewById(R.id.progress_bar);
        toolbarTitle = findViewById(R.id.toolbar_title);
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
    
    private void loadUserInfo() {
        // Get user ID and role from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("FairPayPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
        userRole = prefs.getString("user_role", "user");
        
        // Update UI based on role
        if ("producer".equals(userRole)) {
            toolbarTitle.setText("Publish Product/Service");
            btnSubmit.setText("Publish");
        } else {
            toolbarTitle.setText("Submit Product/Service");
            btnSubmit.setText("Submit for Review");
        }
    }
    
    private void loadSpinnersData() {
        progressBar.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            try {
                // Load categories
                loadCategories();
                
                // Load locations
                loadLocations();
                
                // Load types
                loadTypes();
                
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            } catch (Exception e) {
                Log.e(TAG, "Error loading spinner data: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProductSubmissionActivity.this, 
                            "Error loading data: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    private void loadCategories() throws JSONException, IOException {
        try {
            JSONArray categoriesArray = SupabaseClient.queryTable("categorie", null, null, "*");
            List<String> categories = new ArrayList<>();
            
            for (int i = 0; i < categoriesArray.length(); i++) {
                JSONObject category = categoriesArray.getJSONObject(i);
                int id = category.getInt("id");
                String name = category.getString("nom");
                categories.add(name);
                categoryMap.put(name, id);
            }
            
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ProductSubmissionActivity.this,
                        android.R.layout.simple_spinner_item,
                        categories);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(adapter);
            });
        } catch (Exception e) {
            Log.e("ProductSubmission", "Error loading categories: " + e.getMessage(), e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void loadLocations() throws JSONException, IOException {
        try {
            JSONArray locationsArray = SupabaseClient.queryTable("ville", null, null, "*");
            List<String> locations = new ArrayList<>();
            
            for (int i = 0; i < locationsArray.length(); i++) {
                JSONObject location = locationsArray.getJSONObject(i);
                int id = location.getInt("id");
                String name = location.getString("nom");
                locations.add(name);
                locationMap.put(name, id);
            }
            
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ProductSubmissionActivity.this,
                        android.R.layout.simple_spinner_item,
                        locations);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerLocation.setAdapter(adapter);
            });
        } catch (Exception e) {
            Log.e("ProductSubmission", "Error loading locations: " + e.getMessage(), e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load locations", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void loadTypes() throws JSONException, IOException {
        try {
            JSONArray typesArray = SupabaseClient.queryTable("type", null, null, "*");
            List<String> types = new ArrayList<>();
            
            for (int i = 0; i < typesArray.length(); i++) {
                JSONObject type = typesArray.getJSONObject(i);
                int id = type.getInt("id");
                String name = type.getString("name");
                types.add(name);
                typeMap.put(name, id);
            }
            
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ProductSubmissionActivity.this,
                        android.R.layout.simple_spinner_item,
                        types);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerType.setAdapter(adapter);
            });
        } catch (Exception e) {
            Log.e("ProductSubmission", "Error loading types: " + e.getMessage(), e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Failed to load types", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void setupClickListeners() {
        btnUploadImage.setOnClickListener(v -> openImagePicker());
        
        btnSubmit.setOnClickListener(v -> validateAndSubmitProduct());
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            
            // Display the selected image name
            String imageName = getImageNameFromUri(selectedImageUri);
            tvImageName.setText(imageName);
            
            // Show image preview
            imgPreview.setImageURI(selectedImageUri);
            imgPreview.setVisibility(View.VISIBLE);
        }
    }
    
    private String getImageNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting image name: " + e.getMessage());
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    
    private void validateAndSubmitProduct() {
        // Get input values
        String productName = etProductName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String advice = etAdvice.getText().toString().trim();
        
        // Get selected spinner values
        String selectedCategory = spinnerCategory.getSelectedItem() != null ? 
                spinnerCategory.getSelectedItem().toString() : "";
        String selectedLocation = spinnerLocation.getSelectedItem() != null ? 
                spinnerLocation.getSelectedItem().toString() : "";
        String selectedType = spinnerType.getSelectedItem() != null ? 
                spinnerType.getSelectedItem().toString() : "";
        
        // Basic validation
        if (productName.isEmpty() || priceStr.isEmpty() || advice.isEmpty() || 
                selectedCategory.isEmpty() || selectedLocation.isEmpty() || selectedType.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate price
        float price;
        try {
            price = Float.parseFloat(priceStr);
            if (price <= 0) {
                Toast.makeText(this, "Price must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if image is selected
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);
        
        // Submit product in background thread
        new Thread(() -> {
            try {
                // Get IDs from the maps
                int categoryId = categoryMap.get(selectedCategory);
                int locationId = locationMap.get(selectedLocation);
                int typeId = typeMap.get(selectedType);
                
                // Create JSON object with product data
                JSONObject productData = new JSONObject();
                productData.put("nom", productName);
                productData.put("prix", price);
                productData.put("conseil", advice);
                productData.put("datemiseajour", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                productData.put("categorie_id", categoryId);
                productData.put("ville_id", locationId);
                productData.put("type_id", typeId);
                productData.put("submitted_by", userId);
                productData.put("status", "pending"); // Set status to pending by default
                
                // Handle image
                String imageUrl = "";
                if (selectedImageUri != null) {
                    // Show progress dialog for image upload
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
                    });
                    
                    // Upload image using our helper
                    imageUrl = ImageHelper.uploadImage(this, selectedImageUri);
                    
                    if (imageUrl == null) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnSubmit.setEnabled(true);
                            Toast.makeText(this, "Failed to upload image. Please try again.", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                }

                // Add the image URL to the product data
                productData.put("image", imageUrl);
                
                // Set status based on user role
                if ("producer".equals(userRole)) {
                    productData.put("status", "approved");
                    productData.put("is_verified", true);
                } else {
                    productData.put("status", "pending");
                    productData.put("is_verified", false);
                }
                
                // Insert product into database
                JSONObject result = SupabaseClient.insertIntoTable("produit_serv", productData);
                
                // If user is a collaborator, create a submission record
                if ("collaborator".equals(userRole)) {
                    int insertedProductId = result.getInt("id");
                    
                    JSONObject submissionData = new JSONObject();
                    submissionData.put("produit_serv_id", insertedProductId);
                    submissionData.put("submitted_by", userId);
                    submissionData.put("status", "pending");
                    submissionData.put("date_submitted", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                    
                    SupabaseClient.insertIntoTable("product_submission", submissionData);
                }
                
                // Update UI on success
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    
                    if ("producer".equals(userRole)) {
                        Toast.makeText(ProductSubmissionActivity.this, 
                                "Product published successfully!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ProductSubmissionActivity.this, 
                                "Product submitted for review!", Toast.LENGTH_LONG).show();
                    }
                    
                    // Return to previous screen
                    finish();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error submitting product: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(ProductSubmissionActivity.this, 
                            "Error submitting product: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
    
    // Replace the existing image handling code with this:

    private String uploadImageToStorage(Uri imageUri) throws IOException, JSONException {
        // Get the file extension
        String fileExtension = getFileExtension(imageUri);
        if (fileExtension == null) {
            fileExtension = "jpg"; // Default to jpg if we can't determine the extension
        }
        
        // Generate a unique filename using UUID
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        
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
        
        // Determine content type
        String contentType = "image/jpeg"; // Default
        if (fileExtension.equalsIgnoreCase("png")) {
            contentType = "image/png";
        } else if (fileExtension.equalsIgnoreCase("gif")) {
            contentType = "image/gif";
        }
        
        // Upload to Supabase Storage
        return SupabaseClient.uploadFile("images", fileName, imageBytes, contentType);
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
