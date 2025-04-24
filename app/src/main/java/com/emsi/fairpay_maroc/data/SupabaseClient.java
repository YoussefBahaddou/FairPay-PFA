package com.emsi.fairpay_maroc.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {
    private static final String TAG = "SupabaseClient";
    private static OkHttpClient httpClient;
    
    // Replace with your Supabase URL and API key from the Supabase dashboard
    private static final String SUPABASE_URL = "https://wpglbagvinlmthlzxgyy.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndwZ2xiYWd2aW5sbXRobHp4Z3l5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDExNzYyMjksImV4cCI6MjA1Njc1MjIyOX0.ala-XwZ9Zh-6UOQBIruJ76kYRg7MINzIvNjDgk8sMfw";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
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
    public static void deleteFromTable(String tableName, String columnName, String value) throws Exception {
        String url = SUPABASE_URL + "/rest/v1/" + tableName + "?" + columnName + "=eq." + value;

        Log.d(TAG, "Delete URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .delete()
                .build();

        try (Response response = getHttpClient().newCall(request).execute()) {
            String responseData = response.body().string();
            Log.d(TAG, "Response: " + responseData);

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ": " + responseData);
            }

            Log.d(TAG, "Row deleted successfully from table: " + tableName);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting from table: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Query data from a table with filters
     * @param table The table name
     * @param column The column to filter on
     * @param value The value to match
     * @param select The columns to select (comma-separated)
     * @return JSONArray of results
     */
    public static JSONArray queryTable(String table, String column, String value, String select) throws IOException, JSONException {
        String url = SUPABASE_URL + "/rest/v1/" + table + "?select=" + select;
        
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
     * Insert data into a table
     * @param table The table name
     * @param data The data to insert as JSONObject
     * @return The inserted data as JSONObject
     */
    public static JSONObject insertIntoTable(String table, JSONObject data) throws IOException, JSONException {
        String url = SUPABASE_URL + "/rest/v1/" + table;
        RequestBody body = RequestBody.create(data.toString(), JSON);

        Log.d(TAG, "Insert URL: " + url);
        Log.d(TAG, "Insert Data: " + data.toString());

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
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
            return resultArray.getJSONObject(0); // Return the first inserted row
        } catch (Exception e) {
            Log.e(TAG, "Error inserting into table: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Check if a role exists by ID
     * @param roleId The role ID to check
     * @return true if the role exists, false otherwise
     */
    public static boolean roleExists(int roleId) throws IOException, JSONException {
        try {
            JSONArray results = queryTable("role", "id", String.valueOf(roleId), "id");
            return results.length() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if role exists: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Check if a value exists in a column
     * @param table The table name
     * @param column The column to check
     * @param value The value to check for
     * @return true if the value exists, false otherwise
     */
    public static boolean valueExists(String table, String column, String value) throws IOException, JSONException {
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

}
