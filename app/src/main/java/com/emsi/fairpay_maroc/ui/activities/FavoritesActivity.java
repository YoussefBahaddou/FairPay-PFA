package com.emsi.fairpay_maroc.ui.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emsi.fairpay_maroc.R;
import com.emsi.fairpay_maroc.adapters.ItemAdapter;
import com.emsi.fairpay_maroc.data.SupabaseClient;
import com.emsi.fairpay_maroc.models.Item;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private Set<Integer> favoriteIds = new HashSet<>(); // Track favorite IDs

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter with an empty list and attach it to the RecyclerView
        itemAdapter = new ItemAdapter(new ArrayList<>(), getCurrentUserId(), true);
        recyclerView.setAdapter(itemAdapter);

        // Fetch favorite items
        fetchFavoriteItems();
    }

    private int getCurrentUserId() {
        return getSharedPreferences("FairPayPrefs", MODE_PRIVATE).getInt("user_id", -1);
    }

    private void fetchFavoriteItems() {
        new Thread(() -> {
            try {
                // Fetch favorite items from the "favoris" table
                JSONArray favoritesArray = SupabaseClient.queryTable(
                        "favoris",
                        "utilisateur_id",
                        String.valueOf(getCurrentUserId()),
                        "*"
                );

                List<Item> favoriteItems = new ArrayList<>();
                for (int i = 0; i < favoritesArray.length(); i++) {
                    JSONObject favorite = favoritesArray.getJSONObject(i);
                    int itemId = favorite.getInt("produit_serv_id");
                    favoriteIds.add(itemId); // Add to favorite IDs set

                    // Fetch item details
                    JSONArray itemArray = SupabaseClient.queryTable(
                            "produit_serv",
                            "id",
                            String.valueOf(itemId),
                            "*"
                    );

                    if (itemArray.length() > 0) {
                        JSONObject itemData = itemArray.getJSONObject(0);
                        Item item = new Item(
                                itemData.getInt("id"),
                                itemData.getString("image"),
                                itemData.getString("nom"),
                                itemData.getString("prix"),
                                itemData.getString("conseil"),
                                itemData.getString("datemiseajour"),
                                "Category",
                                "Location",
                                "Type"
                        );
                        favoriteItems.add(item);
                    }
                }

                // Update the adapter on the main thread
                runOnUiThread(() -> {
                    itemAdapter.setFavoriteIds(favoriteIds); // Pass favorite IDs to the adapter
                    itemAdapter.updateItems(favoriteItems);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to fetch favorites", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}