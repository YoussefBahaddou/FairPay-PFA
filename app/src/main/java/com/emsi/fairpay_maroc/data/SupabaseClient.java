package com.emsi.fairpay_maroc.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {
    private static final String TAG = "SupabaseClient";
    // Replace with your actual Supabase URL and key
    private static final String SUPABASE_URL = "https://wpglbagvinlmthlzxgyy.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndwZ2xiYWd2aW5sbXRobHp4Z3l5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDExNzYyMjksImV4cCI6MjA1Njc1MjIyOX0.ala-XwZ9Zh-6UOQBIruJ76kYRg7MINzIvNjDgk8sMfw";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private static OkHttpClient httpClient;
    
    private SupabaseClient() {
        // Private constructor to enforce singleton pattern
    }
    
    public static synchronized OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
        return httpClient;
    }
    
    /**
     * Query a table in Supabase
     * 
     * @param table The table name
     * @param column The column to filter by (can be null for no filter)
     * @param value The value to filter by (can be null for no filter)
     * @param select The columns to select (comma-separated)
     * @return JSONArray of results
     * @throws IOException If an error occurs
     * @throws Exception If any other error occurs
     */
    public static JSONArray queryTable(String table, String column, String value, String select) throws IOException, Exception {
        // Make sure we have the correct URL format with /rest/v1/
        String url = SUPABASE_URL;
        if (!url.endsWith("/")) {
            url += "/";
        }
        if (!url.endsWith("rest/v1/")) {
            url += "rest/v1/";
        }
        
        url += table + "?select=" + select;
        
        if (column != null && value != null) {
            url += "&" + column + "=eq." + value;
        }
        
        Log.d(TAG, "Query URL: " + url);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .build();
        
        try (Response response = getHttpClient().newCall(request).execute()) {
            String responseData = response.body().string();
            Log.d(TAG, "Response: " + responseData);
            
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ": " + responseData);
            }
            
            return new JSONArray(responseData);
        } catch (Exception e) {
            Log.e(TAG, "Error querying table: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Query a table in Supabase with a custom filter
     * 
     * @param table The table name
     * @param filter The custom filter to apply
     * @param select The columns to select (comma-separated)
     * @return JSONArray of results
     * @throws IOException If an error occurs
     * @throws Exception If any other error occurs
     */
    public static JSONArray queryTableWithFilter(String table, String filter, String select) throws IOException, Exception {
        // Make sure we have the correct URL format with /rest/v1/
        String url = SUPABASE_URL;
        if (!url.endsWith("/")) {
            url += "/";
        }
        if (!url.endsWith("rest/v1/")) {
            url += "rest/v1/";
        }
        
        url += table + "?select=" + select;
        
        if (filter != null && !filter.isEmpty()) {
            url += "&" + filter;
        }
        
        Log.d(TAG, "Query URL with filter: " + url);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .build();
        
        try (Response response = getHttpClient().newCall(request).execute()) {
            String responseData = response.body().string();
            Log.d(TAG, "Response: " + responseData);
            
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ": " + responseData);
            }
            
            return new JSONArray(responseData);
        } catch (Exception e) {
            Log.e(TAG, "Error querying table with filter: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Insert data into a table
     * 
     * @param table The table name
     * @param data The data to insert
     * @return JSONObject of the inserted row
     * @throws IOException If an error occurs
     * @throws Exception If any other error occurs
     */
    public static JSONObject insertIntoTable(String table, JSONObject data) throws IOException, Exception {
        // Make sure we have the correct URL format with /rest/v1/
        String url = SUPABASE_URL;
        if (!url.endsWith("/")) {
            url += "/";
        }
        if (!url.endsWith("rest/v1/")) {
            url += "rest/v1/";
        }
        url += table;
        
        RequestBody body = RequestBody.create(data.toString(), JSON);
        
        Log.d(TAG, "Insert URL: " + url);
        Log.d(TAG, "Insert Data: " + data.toString());
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .addHeader("Prefer", "resolution=ignore-duplicates,return=representation")
                .post(body)
                .build();
        
        try (Response response = getHttpClient().newCall(request).execute()) {
            String responseData = response.body().string();
            Log.d(TAG, "Response: " + responseData);
            
            if (!response.isSuccessful()) {
                Log.e(TAG, "Error response: " + responseData);
                throw new IOException("Unexpected code " + response + ": " + responseData);
            }
            
            JSONArray resultArray = new JSONArray(responseData);
            return resultArray.getJSONObject(0);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting into table: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Check if a role exists
     * 
     * @param roleId The role ID to check
     * @return true if the role exists, false otherwise
     * @throws Exception If an error occurs
     */
    public static boolean roleExists(int roleId) throws Exception {
        try {
            JSONArray results = queryTable("role", "id", String.valueOf(roleId), "id");
            return results.length() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if role exists: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Check if a value exists in a table
     * 
     * @param table The table name
     * @param column The column to check
     * @param value The value to check
     * @return true if the value exists, false otherwise
     * @throws Exception If an error occurs
     */
    public static boolean valueExists(String table, String column, String value) throws Exception {
        try {
            JSONArray results = queryTable(table, column, value, "id");
            return results.length() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if value exists: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get the next available ID for a table
     * @param table The table name
     * @return The next available ID
     */
    public static int getNextId(String table) throws IOException, JSONException {
        String url = SUPABASE_URL + "/rest/v1/" + table + "?select=id&order=id.desc&limit=1";
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .build();
        
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            
            String responseData = response.body().string();
            JSONArray results = new JSONArray(responseData);
            
            if (results.length() > 0) {
                JSONObject lastRecord = results.getJSONObject(0);
                return lastRecord.getInt("id") + 1;
            } else {
                // If no records exist, start with ID 1
                return 1;
            }
        }
    }

    /**
     * Update a record in a table
     * @param path The path to the record (table?column=eq.value)
     * @param data The data to update as JSONObject
     * @return The updated data as JSONObject
     */
    public static boolean updateRecord(String table, int id, JSONObject data) throws IOException, JSONException {
        // Make sure we have the correct URL format with /rest/v1/
        String url = SUPABASE_URL;
        if (!url.endsWith("/")) {
            url += "/";
        }
        if (!url.endsWith("rest/v1/")) {
            url += "rest/v1/";
        }

        url += table + "?id=eq." + id;

        Log.d(TAG, "Update URL: " + url);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(data.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(body)
                .build();
        
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseData = response.body().string();
                Log.e(TAG, "Error updating record: " + responseData);
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating record: " + e.getMessage(), e);
            throw e;
        }
    }

    public static JSONObject updateRecord(String path, JSONObject data) throws IOException, JSONException {
        // Make sure we have the correct URL format
        String url = SUPABASE_URL;
        if (!url.endsWith("/")) {
            url += "/";
        }
        if (!url.endsWith("rest/v1/")) {
            url += "rest/v1/";
        }

        url += path;

        Log.d(TAG, "Update URL: " + url);
        Log.d(TAG, "Update Data: " + data.toString());

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(data.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .patch(body)
                .build();

        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseData = response.body() != null ? response.body().string() : "No response body";
                Log.e(TAG, "Error updating record: " + responseData);
                throw new IOException("Unexpected code " + response + ": " + responseData);
            }

            // Get response body
            String responseData = response.body() != null ? response.body().string() : "";
            Log.d(TAG, "Update response: " + responseData);

            // If response is empty, return an empty success JSONObject
            if (responseData == null || responseData.trim().isEmpty()) {
                return new JSONObject();
            }

            // Try to parse as JSONArray first (Supabase often returns arrays)
            try {
                JSONArray jsonArray = new JSONArray(responseData);
                if (jsonArray.length() > 0) {
                    return jsonArray.getJSONObject(0);
                } else {
                    return new JSONObject();
                }
            } catch (JSONException e) {
                // If not an array, try to parse as JSONObject
                return new JSONObject(responseData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating record: " + e.getMessage(), e);
            throw e;
        }
    }

    public static boolean updateRecordBoolean(String table, JSONObject data) throws IOException, JSONException {
        try {
            JSONObject result = updateRecord(table, data);
            return result != null && !result.has("error");
        } catch (Exception e) {
            Log.e(TAG, "Error in updateRecordBoolean: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Uploads a file to Supabase Storage
     * @param bucketName The name of the storage bucket (e.g., "images")
     * @param fileName The name to give the file in storage
     * @param fileData The byte array of the file data
     * @param contentType The MIME type of the file (e.g., "image/jpeg")
     * @return The URL of the uploaded file
     */
    public static String uploadFile(String bucketName, String fileName, byte[] fileData, String contentType) throws IOException, JSONException {
        String url = SUPABASE_URL + "/storage/v1/object/" + bucketName + "/" + fileName;
        
        Log.d(TAG, "Uploading file to: " + url);
        Log.d(TAG, "Content type: " + contentType);
        Log.d(TAG, "File size: " + fileData.length + " bytes");
        
        RequestBody requestBody = RequestBody.create(MediaType.parse(contentType), fileData);
        
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", contentType)
                .build();
        
        try (Response response = getHttpClient().newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "No response body";

            if (!response.isSuccessful()) {
                Log.e(TAG, "Error uploading file: " + response.code() + " - " + responseBody);
                throw new IOException("Error uploading file: " + response.code() + " - " + responseBody);
            }
            
            Log.d(TAG, "File uploaded successfully: " + responseBody);

            // Return the public URL for the file
            return SUPABASE_URL + "/storage/v1/object/public/" + bucketName + "/" + fileName;
        }
    }

    /**
     * Gets the public URL for a file in Supabase Storage
     * @param bucketName The name of the storage bucket
     * @param fileName The name of the file
     * @return The public URL of the file
     */
    public static String getFileUrl(String bucketName, String fileName) {
        return SUPABASE_URL + "/storage/v1/object/public/" + bucketName + "/" + fileName;
    }

    /**
     * Ensures that a bucket exists in Supabase Storage
     * @param bucketName The name of the bucket to check/create
     */
    public static void ensureBucketExists(String bucketName) throws IOException, JSONException {
        // First, check if the bucket exists
        String url = SUPABASE_URL + "/storage/v1/bucket/" + bucketName;
        
        Request checkRequest = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .build();
        
        try (Response response = getHttpClient().newCall(checkRequest).execute()) {
            if (response.code() == 404) {
                // Bucket doesn't exist, create it
                JSONObject bucketConfig = new JSONObject();
                bucketConfig.put("name", bucketName);
                bucketConfig.put("public", true); // Make it publicly accessible
                
                RequestBody body = RequestBody.create(
                        bucketConfig.toString(), 
                        MediaType.parse("application/json"));
                
                Request createRequest = new Request.Builder()
                        .url(SUPABASE_URL + "/storage/v1/bucket")
                        .post(body)
                        .addHeader("apikey", SUPABASE_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                        .addHeader("Content-Type", "application/json")
                        .build();
                
                try (Response createResponse = getHttpClient().newCall(createRequest).execute()) {
                    if (!createResponse.isSuccessful()) {
                        throw new IOException("Failed to create bucket: " + createResponse.code());
                    }
                    Log.d(TAG, "Created bucket: " + bucketName);
                }
            } else if (!response.isSuccessful() && response.code() != 404) {
                throw new IOException("Failed to check bucket: " + response.code());
            } else {
                Log.d(TAG, "Bucket already exists: " + bucketName);
            }
        }
    }

    /**
     * Delete a record from a table
     * @param path The path to the record to delete (e.g., "table_name?id=eq.123")
     * @return true if successful, false otherwise
     */
    public static boolean deleteRecord(String path) throws IOException, JSONException {
        String url = SUPABASE_URL + "/rest/v1/" + path;
        
        Log.d(TAG, "Delete URL: " + url);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .delete()
                .build();
        
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseData = response.body().string();
                Log.e(TAG, "Error response: " + responseData);
                throw new IOException("Unexpected code " + response + ": " + responseData);
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting record: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Update the status of a product
     * @param productId The product ID
     * @param newStatus The new status (e.g., "approved", "pending", "rejected")
     * @return True if successful, false otherwise
     */
    public static boolean updateProductStatus(String productId, String newStatus) {
        try {
            JSONObject statusData = new JSONObject();
            statusData.put("status", newStatus);
            
            String path = "produit_serv?id=eq." + productId;
            JSONObject result = updateRecord(path, statusData);

            return result != null;
        } catch (Exception e) {
            Log.e(TAG, "Error updating product status: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if a column exists in a table
     * @param table The table name
     * @param column The column name to check
     * @return true if the column exists, false otherwise
     */
    public static boolean columnExists(String table, String column) {
        try {
            // Query the table structure
            String url = SUPABASE_URL;
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "rest/v1/" + table + "?limit=1";
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("apikey", SUPABASE_KEY)
                    .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                    .build();
            
            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return false;
                }
                
                String responseData = response.body().string();
                JSONArray result = new JSONArray(responseData);
                
                // If we got at least one row, check if the column exists
                if (result.length() > 0) {
                    JSONObject row = result.getJSONObject(0);
                    return row.has(column);
                }
                
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking if column exists: " + e.getMessage(), e);
            return false;
        }
    }

    public static boolean deleteFromTable(String path) throws Exception {
        String url = SUPABASE_URL + "/rest/v1/" + path;
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("apikey", SUPABASE_KEY);
        connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Prefer", "return=minimal");

        int responseCode = connection.getResponseCode();
        return responseCode >= 200 && responseCode < 300;
    }
}
