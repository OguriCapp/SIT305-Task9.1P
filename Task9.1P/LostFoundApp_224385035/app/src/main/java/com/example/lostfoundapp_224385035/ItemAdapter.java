package com.example.lostfoundapp_224385035;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Connect data with views
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private final Context context;
    private final DatabaseHelper dbHelper;
    private Cursor cursor;

    public ItemAdapter(Context context, DatabaseHelper dbHelper) {
        this.context = context;
        this.dbHelper = dbHelper;
    }

    // View holder for items
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewItemType;
        public TextView textViewItemDescription;
        public TextView textViewItemDate;
        public TextView textViewItemLocation;
        public Button btnRemove;

        public ItemViewHolder(View view) {
            super(view);
            // Initialize views
            textViewItemType = view.findViewById(R.id.textViewItemType);
            textViewItemDescription = view.findViewById(R.id.textViewItemDescription);
            textViewItemDate = view.findViewById(R.id.textViewItemDate);
            textViewItemLocation = view.findViewById(R.id.textViewItemLocation);
            btnRemove = view.findViewById(R.id.btnRemove);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create item view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lost_found, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            // Get data from cursor
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));

            // Bind data to views
            holder.textViewItemType.setText(type);
            holder.textViewItemDescription.setText(description);
            holder.textViewItemDate.setText(date);
            holder.textViewItemLocation.setText("At " + location);

            // Handle remove action
            holder.btnRemove.setOnClickListener(v -> {
                int result = dbHelper.deleteItem(id);
                if (result > 0) {
                    Toast.makeText(context, "Item removed successfully", Toast.LENGTH_SHORT).show();
                    swapCursor(dbHelper.getAllItems());
                } else {
                    Toast.makeText(context, "Error removing item", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        // Return item count
        return cursor != null ? cursor.getCount() : 0;
    }

    // Update data source
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }
} 