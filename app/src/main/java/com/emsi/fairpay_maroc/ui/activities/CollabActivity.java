package com.emsi.fairpay_maroc.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.adapters.SubmissionAdapter;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.models.Submission;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CollabActivity extends BaseActivity {

    private static final String TAG = "CollabActivity";
    
    private LinearLayout normalUserView;
    private LinearLayout collaboratorView;
    private LinearLayout producerView;
    private ProgressBar progressBar;
    
    private int userId;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collab);
        
        // Initialize UI components
        initializeViews();
        
        // Set up toolbar and back button
        setupToolbar();
        
        // Load user data and determine role
        loadUserData();
        
        // Set up form submission
        Button submitFormButton = findViewById(R.id.btn_submit_collab_form);
        submitFormButton.setOnClickListener(v -> submitCollabForm());
    }

    private void initializeViews() {
        normalUserView = findViewById(R.id.normal_user_view);
        collaboratorView = findViewById(R.id.collaborator_view);
        producerView = findViewById(R.id.producer_view);
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
    
    private void loadUserData() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        
        // Get user ID from SharedPreferences
        userId = getSharedPreferences("FairPayPrefs", MODE_PRIVATE)
                .getInt("user_id", -1);
        
        if (userId == -1) {
            // User not logged in
            showNormalUserView();
            progressBar.setVisibility(View.GONE);
            return;
        }
        
        // Fetch user data from database to determine role
        new Thread(() -> {
            try {
                // Query the user to get their role_id
                String roleId = getUserRoleId(userId);
                
                // Get the role name based on role_id
                userRole = getRoleName(roleId);
                
                // Update UI based on role
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    updateUIBasedOnRole(userRole);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading user data: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CollabActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                    showNormalUserView();
                });
            }
        }).start();
    }
    
    private String getUserRoleId(int userId) throws JSONException, IOException {
        try {
            return SupabaseClient.queryTable("utilisateur", "id", String.valueOf(userId), "role_id")
                    .getJSONObject(0)
                    .optString("role_id", "1"); // Default to regular user (role_id = 1)
        } catch (Exception e) {
            Log.e(TAG, "Error getting user role: " + e.getMessage());
            return "1"; // Default to regular user
        }
    }
    
    private String getRoleName(String roleId) throws JSONException, IOException {
        try {
            return SupabaseClient.queryTable("role", "id", roleId, "nom")
                    .getJSONObject(0)
                    .optString("nom", "user");
        } catch (Exception e) {
            Log.e(TAG, "Error getting role name: " + e.getMessage());
            return "user"; // Default role name
        }
    }
    
    private void updateUIBasedOnRole(String role) {
        // Hide all views first
        normalUserView.setVisibility(View.GONE);
        collaboratorView.setVisibility(View.GONE);
        producerView.setVisibility(View.GONE);
        
        // Show appropriate view based on role
        switch (role.toLowerCase()) {
            case "collaborator":
                collaboratorView.setVisibility(View.VISIBLE);
                break;
            case "producer":
                producerView.setVisibility(View.VISIBLE);
                break;
            default:
                showNormalUserView();
                break;
        }
    }
    
    private void showNormalUserView() {
        normalUserView.setVisibility(View.VISIBLE);
        collaboratorView.setVisibility(View.GONE);
        producerView.setVisibility(View.GONE);
    }
    
    private void submitCollabForm() {
        // Get form data
        TextInputEditText etCompanyName = findViewById(R.id.et_company_name);
        TextInputEditText etContactEmail = findViewById(R.id.et_contact_email);
        TextInputEditText etContactPhone = findViewById(R.id.et_contact_phone);
        TextInputEditText etMessage = findViewById(R.id.et_message);
        
        String companyName = etCompanyName.getText().toString().trim();
        String contactEmail = etContactEmail.getText().toString().trim();
        String contactPhone = etContactPhone.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        
        // Basic validation
        if (companyName.isEmpty() || contactEmail.isEmpty() || contactPhone.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        
        // Submit form in background thread
        new Thread(() -> {
            try {
                // Create JSON object with form data
                JSONObject formData = new JSONObject();
                formData.put("company_name", companyName);
                formData.put("contact_email", contactEmail);
                formData.put("contact_phone", contactPhone);
                formData.put("message", message);
                formData.put("status", "pending");
                formData.put("date_submitted", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                
                // Add user_id if user is logged in
                if (userId != -1) {
                    formData.put("user_id", userId);
                }
                
                // Insert into collab_request table
                SupabaseClient.insertIntoTable("collab_request", formData);
                
                // Update UI on success
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CollabActivity.this, "Collaboration request submitted successfully!", Toast.LENGTH_LONG).show();
                    
                    // Clear form
                    etCompanyName.setText("");
                    etContactEmail.setText("");
                    etContactPhone.setText("");
                    etMessage.setText("");
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error submitting form: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CollabActivity.this, "Error submitting form: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Set up button click listeners for product submission
        Button submitProductButton = findViewById(R.id.btn_submit_product);
        if (submitProductButton != null) {
            submitProductButton.setOnClickListener(v -> openProductSubmission());
        }
        
        Button publishProductButton = findViewById(R.id.btn_publish_product);
        if (publishProductButton != null) {
            publishProductButton.setOnClickListener(v -> openProductSubmission());
        }
        
        // Refresh data if needed
        if (userId != -1 && !"user".equals(userRole)) {
            loadUserSubmissions();
        }
    }

    private void openProductSubmission() {
        Intent intent = new Intent(CollabActivity.this, ProductSubmissionActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void loadUserSubmissions() {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        
        // Get the appropriate RecyclerView based on user role
        RecyclerView recyclerView;
        if ("producer".equals(userRole)) {
            recyclerView = findViewById(R.id.products_recycler_view);
        } else {
            recyclerView = findViewById(R.id.submissions_recycler_view);
        }
        
        if (recyclerView == null) {
            progressBar.setVisibility(View.GONE);
            return;
        }
        
        // Set layout manager if not already set
        if (recyclerView.getLayoutManager() == null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        
        new Thread(() -> {
            try {
                List<Submission> submissions = new ArrayList<>();
                
                // Query products based on submitted_by field - include ALL statuses
                JSONArray productsArray;
                boolean isAdmin = "admin".equals(userRole);
                
                if (isAdmin) {
                    // Admins can see all pending products
                    productsArray = SupabaseClient.queryTable(
                            "produit_serv", 
                            "status", 
                            "pending", 
                            "*");
                } else {
                    // Regular users see only their own submissions
                    productsArray = SupabaseClient.queryTable(
                            "produit_serv", 
                            "submitted_by", 
                            String.valueOf(userId), 
                            "*");
                }
                
                Log.d(TAG, "Found " + productsArray.length() + " products");
                
                for (int i = 0; i < productsArray.length(); i++) {
                    JSONObject product = productsArray.getJSONObject(i);
                    
                    String id = product.getString("id");
                    String name = product.getString("nom");
                    float price = (float) product.getDouble("prix");
                    String status = product.optString("status", "pending");
                    String date = product.optString("datemiseajour", "Unknown");
                    
                    Submission submission = new Submission(id, name, price, status, date, "");
                    submissions.add(submission);
                    Log.d(TAG, "Added product: " + name + " with status: " + status);
                }
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (submissions.isEmpty()) {
                        // Show empty state message
                        TextView emptyView;
                        if ("producer".equals(userRole)) {
                            emptyView = findViewById(R.id.empty_view_producer);
                        } else {
                            emptyView = findViewById(R.id.empty_view);
                        }
                        
                        if (emptyView != null) {
                            emptyView.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                        
                        Log.d(TAG, "No submissions found, showing empty state");
                    } else {
                        // Create adapter with appropriate functionality
                        SubmissionAdapter adapter;
                        
                        if (isAdmin) {
                            // Admin can approve/reject submissions
                            adapter = new SubmissionAdapter(
                                    submissions,
                                    this::openEditProductActivity,
                                    this::updateProductStatus,
                                    true);
                        } else {
                            // Regular users can only view their submissions
                            adapter = new SubmissionAdapter(
                                    submissions,
                                    this::openEditProductActivity);
                        }
                        
                        recyclerView.setAdapter(adapter);
                        
                        // Hide empty state if visible
                        TextView emptyView;
                        if ("producer".equals(userRole)) {
                            emptyView = findViewById(R.id.empty_view_producer);
                        } else {
                            emptyView = findViewById(R.id.empty_view);
                        }
                        
                        if (emptyView != null) {
                            emptyView.setVisibility(View.GONE);
                        }
                        recyclerView.setVisibility(View.VISIBLE);
                        
                        Log.d(TAG, "Displaying " + submissions.size() + " submissions");
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading submissions: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CollabActivity.this, 
                            "Error loading submissions: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // Method to open the edit product activity
    private void openEditProductActivity(String productId) {
        Intent intent = new Intent(this, ProductEditActivity.class);
        intent.putExtra("product_id", productId);
        startActivity(intent);
    }

    // Add this method to open the ProductListActivity
    private void openProductListActivity() {
        Intent intent = new Intent(this, ProductListActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Update the status of a product
     * @param productId The product ID
     * @param newStatus The new status ("approved" or "rejected")
     */
    private void updateProductStatus(String productId, String newStatus) {
        progressBar.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            try {
                boolean success = SupabaseClient.updateProductStatus(productId, newStatus);
                
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (success) {
                        Toast.makeText(this, 
                                "Product " + ("approved".equals(newStatus) ? "approved" : "rejected"), 
                                Toast.LENGTH_SHORT).show();
                        
                        // Refresh the list
                        loadUserSubmissions();
                    } else {
                        Toast.makeText(this, 
                                "Failed to update product status", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating product status: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, 
                            "Error updating status: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}