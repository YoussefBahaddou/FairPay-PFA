package com.emsi.fairpay_maroc.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.emsi.fairpay_maroc.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageHelper {
    private static final String TAG = "ImageHelper";
    private static final int MAX_IMAGE_DIMENSION = 1024;
    private static final int COMPRESSION_QUALITY = 80;
    
    /**
     * Load an image into an ImageView
     * @param context The context
     * @param imagePath The image path or Base64 string
     * @param imageView The ImageView to load into
     */
    public static void loadImage(Context context, String imagePath, ImageView imageView) {
        if (imagePath == null || imagePath.isEmpty()) {
            // Load placeholder if no image
            Glide.with(context)
                    .load(R.drawable.placeholder_image)
                    .into(imageView);
            return;
        }
        
        // Check if it's a Base64 image
        if (imagePath.startsWith("data:image") || imagePath.startsWith("/9j/")) {
            try {
                // It's a Base64 image
                String base64Image = imagePath;
                if (imagePath.contains(",")) {
                    base64Image = imagePath.split(",")[1];
                }
                
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Glide.with(context)
                        .load(decodedString)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .into(imageView);
            } catch (Exception e) {
                Log.e(TAG, "Error loading Base64 image: " + e.getMessage());
                // Load placeholder on error
                Glide.with(context)
                        .load(R.drawable.placeholder_image)
                        .into(imageView);
            }
        } else {
            // It's a URL or file path
            Glide.with(context)
                    .load(imagePath)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(imageView);
        }
    }
    
    /**
     * Upload an image and return a Base64 string
     * @param context The context
     * @param imageUri The image URI
     * @return Base64 encoded image string or null if failed
     */
    public static String uploadImage(Context context, Uri imageUri) {
        try {
            // Get input stream from URI
            InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
            if (imageStream == null) {
                Log.e(TAG, "Failed to open input stream for image");
                return null;
            }
            
            // Decode the image
            Bitmap originalBitmap = BitmapFactory.decodeStream(imageStream);
            imageStream.close();
            
            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from input stream");
                return null;
            }
            
            // Resize the image if needed
            Bitmap resizedBitmap = resizeImageIfNeeded(originalBitmap);
            
            // Convert to Base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            
            // Create Base64 string
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
            
            // Clean up
            if (resizedBitmap != originalBitmap) {
                originalBitmap.recycle();
            }
            resizedBitmap.recycle();
            
            return base64Image;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error processing image: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get a bitmap from a Uri, handling different image formats
     */
    private static Bitmap getBitmapFromUri(Context context, Uri imageUri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            
            // Try using MediaStore first (works well for most formats)
            try {
                return MediaStore.Images.Media.getBitmap(resolver, imageUri);
            } catch (Exception e) {
                Log.w(TAG, "MediaStore.Images.Media.getBitmap failed, trying alternative method: " + e.getMessage());
            }
            
            // If MediaStore fails, try using BitmapFactory
            InputStream inputStream = resolver.openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode image using BitmapFactory");
                return null;
            }
            
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error getting bitmap from Uri: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Resize an image if it's too large
     * @param originalBitmap The original bitmap
     * @return Resized bitmap or the original if small enough
     */
    private static Bitmap resizeImageIfNeeded(Bitmap originalBitmap) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        
        // Check if resize is needed
        if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
            return originalBitmap;
        }
        
        // Calculate new dimensions
        float aspectRatio = (float) width / height;
        int newWidth, newHeight;
        
        if (width > height) {
            newWidth = MAX_IMAGE_DIMENSION;
            newHeight = Math.round(newWidth / aspectRatio);
        } else {
            newHeight = MAX_IMAGE_DIMENSION;
            newWidth = Math.round(newHeight * aspectRatio);
        }
        
        // Create and return the resized bitmap
        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }
}