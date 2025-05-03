package com.example.lostfoundapp_224385035;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;
import java.util.Map;

// Display items on map
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseHelper dbHelper;
    private Button btnZoomIn;
    private Button btnZoomOut;
    private Button btnResetZoom;
    
    // Default location
    private static final LatLng DEFAULT_LOCATION = new LatLng(-25.274398, 133.775136);
    private static final float DEFAULT_ZOOM = 4.0f;
    private static final float ZOOM_DELTA = 1.0f;
    
    // Map ID for Google Maps
    private static final String MAP_ID = "9d86064e41353758"; // Replace with your actual Map ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_map);

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        
        // Add zoom controls
        addZoomControls();

        // Setup map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            // Load map
            mapFragment.getMapAsync(this);
        }
    }
    
    // Add zoom controls to map
    private void addZoomControls() {
        ConstraintLayout mapLayout = findViewById(R.id.map_layout);
        
        // Create zoom in button
        btnZoomIn = new Button(this);
        btnZoomIn.setId(View.generateViewId());
        btnZoomIn.setText("+");
        btnZoomIn.setTextSize(20);
        btnZoomIn.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnZoomIn.setOnClickListener(v -> zoomIn());
        
        // Create zoom out button
        btnZoomOut = new Button(this);
        btnZoomOut.setId(View.generateViewId());
        btnZoomOut.setText("-");
        btnZoomOut.setTextSize(20);
        btnZoomOut.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnZoomOut.setOnClickListener(v -> zoomOut());
        
        // Create reset button
        btnResetZoom = new Button(this);
        btnResetZoom.setId(View.generateViewId());
        btnResetZoom.setText("Reset");
        btnResetZoom.setTextSize(12);
        btnResetZoom.setBackgroundColor(getResources().getColor(android.R.color.white));
        btnResetZoom.setOnClickListener(v -> resetMap());
        
        // Set layout params
        ConstraintLayout.LayoutParams zoomInParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        ConstraintLayout.LayoutParams zoomOutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        ConstraintLayout.LayoutParams resetParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        
        // Add buttons to layout
        mapLayout.addView(btnZoomIn, zoomInParams);
        mapLayout.addView(btnZoomOut, zoomOutParams);
        mapLayout.addView(btnResetZoom, resetParams);
        
        // Configure constraints
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mapLayout);
        
        // Position zoom in button
        constraintSet.connect(btnZoomIn.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16);
        constraintSet.connect(btnZoomIn.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16);
        
        // Position zoom out button
        constraintSet.connect(btnZoomOut.getId(), ConstraintSet.TOP, btnZoomIn.getId(), ConstraintSet.BOTTOM, 8);
        constraintSet.connect(btnZoomOut.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16);
        
        // Position reset button
        constraintSet.connect(btnResetZoom.getId(), ConstraintSet.TOP, btnZoomOut.getId(), ConstraintSet.BOTTOM, 8);
        constraintSet.connect(btnResetZoom.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16);
        
        constraintSet.applyTo(mapLayout);
    }
    
    // Zoom in map
    private void zoomIn() {
        if (mMap != null) {
            float currentZoom = mMap.getCameraPosition().zoom;
            mMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoom + ZOOM_DELTA));
        }
    }
    
    // Zoom out map
    private void zoomOut() {
        if (mMap != null) {
            float currentZoom = mMap.getCameraPosition().zoom;
            mMap.animateCamera(CameraUpdateFactory.zoomTo(Math.max(1, currentZoom - ZOOM_DELTA)));
        }
    }
    
    // Reset map view
    private void resetMap() {
        if (mMap != null) {
            setupMapWithMarkers();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        try {
            // Set map ID
            // mMap.setMapId(MAP_ID);
            
            // Enable location button
            mMap.setMyLocationEnabled(true);
            
            // Setup markers
            setupMapWithMarkers();
        } catch (SecurityException e) {
            // Handle permissions
            Toast.makeText(this, "Map error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // Handle errors
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // Setup item markers
    private void setupMapWithMarkers() {
        // Get items with location
        List<Map<String, Object>> items = dbHelper.getAllItemsWithLocation();
        
        if (items.isEmpty()) {
            Toast.makeText(this, "No items with location data found", Toast.LENGTH_SHORT).show();
            // Show default view
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
            return;
        }

        // Create marker bounds
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        
        // Clear markers
        mMap.clear();
        
        // Add item markers
        for (Map<String, Object> item : items) {
            Double latitude = (Double) item.get("latitude");
            Double longitude = (Double) item.get("longitude");
            String type = (String) item.get("type");
            String name = (String) item.get("name");
            String description = (String) item.get("description");
            
            if (latitude != null && longitude != null && latitude != 0 && longitude != 0) {
                LatLng position = new LatLng(latitude, longitude);
                
                // Set marker color
                float markerColor = type.equals("Lost") ? 
                        BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_GREEN;
                
                mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(type + ": " + name)
                        .snippet(description)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
                
                // Add position to bounds
                builder.include(position);
            }
        }
        
        // Update camera
        try {
            if (items.size() > 0) {
                LatLngBounds bounds = builder.build();
                
                // Add padding
                int padding = 100; // in pixels
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } else {
                // Show default view
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
            }
        } catch (IllegalStateException e) {
            // Show default view
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
        }
    }
} 