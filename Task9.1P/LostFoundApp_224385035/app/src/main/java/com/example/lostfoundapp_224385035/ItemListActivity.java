package com.example.lostfoundapp_224385035;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.database.Cursor;

// Display item list
public class ItemListActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Setup recycler view
        RecyclerView recyclerView = findViewById(R.id.recyclerViewItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Create adapter
        adapter = new ItemAdapter(this, dbHelper);
        recyclerView.setAdapter(adapter);
        loadItems();
    }

    // Load items from database
    private void loadItems() {
        Cursor cursor = dbHelper.getAllItems();
        adapter.swapCursor(cursor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }
} 