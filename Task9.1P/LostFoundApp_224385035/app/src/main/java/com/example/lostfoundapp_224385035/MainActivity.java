package com.example.lostfoundapp_224385035;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

// Main application screen
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_main);

        // Get buttons
        Button btnCreateNewAdvert = findViewById(R.id.btnCreateNewAdvert);
        Button btnShowAllItems = findViewById(R.id.btnShowAllItems);
        Button btnShowOnMap = findViewById(R.id.btnShowOnMap);

        // Handle create button
        btnCreateNewAdvert.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateAdvertActivity.class);
            startActivity(intent);
        });

        // Handle list button
        btnShowAllItems.setOnClickListener(v -> {
            Intent intent = new Intent(this, ItemListActivity.class);
            startActivity(intent);
        });

        // Handle map button
        btnShowOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        });
    }
}