package com.example.lostfoundapp_224385035;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import android.content.Intent;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// To create new lost or found item
public class CreateAdvertActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private AutoCompleteTextView editTextLocation;
    private double latitude = 0;
    private double longitude = 0;
    private static final String TAG = "CreateAdvertActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1002;
    
    // You should fill your own Google Maps API key to use it
    private static final String MAPS_API_KEY = "";
    
    // Handle location permission requests
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION, false);
                
                if (fineLocationGranted != null && fineLocationGranted) {
                    getCurrentLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    getCurrentLocation();
                } else {
                    // No location permission granted
                    Toast.makeText(this, "Location permission is required to use this feature", 
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //To hide th action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        setContentView(R.layout.activity_create_advert);

        // Initialize Google Places API
        try {
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), MAPS_API_KEY);
                Log.d(TAG, "Places API initialized successfully with key: " + MAPS_API_KEY);
            } else {
                Log.d(TAG, "Places API was already initialized");
            }
            
            // To create Places client instance
            PlacesClient placesClient = Places.createClient(this);
            Log.d(TAG, "Places client created successfully");
            
            // To verify API key works
            Toast.makeText(this, "Google Maps and Places API initialized", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Places API: " + e.getMessage());
            Toast.makeText(this, "Error initializing Places API: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get all the input fields from layout
        RadioGroup radioGroupType = findViewById(R.id.radioGroupType);
        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextPhone = findViewById(R.id.editTextPhone);
        EditText editTextDescription = findViewById(R.id.editTextDescription);
        EditText editTextDate = findViewById(R.id.editTextDate);
        editTextLocation = findViewById(R.id.editTextLocation);
        Button btnGetCurrentLocation = findViewById(R.id.btnGetCurrentLocation);
        Button btnSave = findViewById(R.id.btnSave);

        setupPlacesAutocomplete();
        
        btnGetCurrentLocation.setOnClickListener(v -> {
            requestLocationPermission();
        });

        // Handle the save button
        btnSave.setOnClickListener(v -> {
            // Get input values
            String type = radioGroupType.getCheckedRadioButtonId() == R.id.radioLost ? "Lost" : "Found";
            String name = editTextName.getText().toString();
            String phone = editTextPhone.getText().toString();
            String description = editTextDescription.getText().toString();
            String date = editTextDate.getText().toString();
            String location = editTextLocation.getText().toString();

            // Validate all fields
            if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save to database
            long result = dbHelper.insertItem(type, name, phone, description, date, location, latitude, longitude);
            if (result != -1) {
                Toast.makeText(this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Setup Google Places autocomplete
    private void setupPlacesAutocomplete() {
        editTextLocation.setOnClickListener(v -> {
            try {
                // Check if the Places API is initialized
                if (!Places.isInitialized()) {
                    Places.initialize(getApplicationContext(), MAPS_API_KEY);
                    Log.d(TAG, "Re-initializing Places API on click");
                }
                
                // Set return fields
                List<Place.Field> fields = Arrays.asList(
                        Place.Field.ID, 
                        Place.Field.NAME, 
                        Place.Field.ADDRESS, 
                        Place.Field.LAT_LNG);
                
                Log.d(TAG, "Launching Places Autocomplete with fields: " + fields);
                
                // To create Places autocomplete intent
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .build(this);
                
                // TO start activity
                Log.d(TAG, "Starting activity for Autocomplete");
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            } catch (Exception e) {
                Log.e(TAG, "Places API error: " + e.getMessage(), e);
                Toast.makeText(this, "Places API error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                
                editTextLocation.setShowSoftInputOnFocus(true);
                
                // Setup geocoding
                editTextLocation.setOnFocusChangeListener((view, hasFocus) -> {
                    if (!hasFocus && editTextLocation.getText().length() > 0) {
                        String address = editTextLocation.getText().toString();
                        geocodeAddressManually(address);
                    }
                });
            }
        });
        
        // Hide keyboard 
        editTextLocation.setShowSoftInputOnFocus(false);
        
        // Enable manual entry
        editTextLocation.setOnLongClickListener(v -> {
            editTextLocation.setShowSoftInputOnFocus(true);
            Toast.makeText(this, "You can now manually enter a location", Toast.LENGTH_SHORT).show();
            
            // Setup geocoding
            editTextLocation.setOnFocusChangeListener((view, hasFocus) -> {
                if (!hasFocus && editTextLocation.getText().length() > 0) {
                    String address = editTextLocation.getText().toString();
                    geocodeAddressManually(address);
                }
            });
            
            return true;
        });
    }
    
    // When google maps doesn't work, Geocode manually entered addresses
    private void geocodeAddressManually(String addressText) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            Log.d(TAG, "Attempting to geocode address: " + addressText);
            // Get multiple results to improve accuracy
            List<Address> addresses = geocoder.getFromLocationName(addressText, 5);
            if (addresses != null && !addresses.isEmpty()) {
                // Use first result
                Address address = addresses.get(0);
                latitude = address.getLatitude();
                longitude = address.getLongitude();
                
                Log.d(TAG, "Manually geocoded: " + addressText);
                Log.d(TAG, "Result address: " + address.getAddressLine(0));
                Log.d(TAG, "Result coordinates: lat=" + latitude + ", lng=" + longitude);
                
                // Display found address
                if (address.getMaxAddressLineIndex() >= 0) {
                    editTextLocation.setText(address.getAddressLine(0));
                }
                
                Toast.makeText(this, "Location coordinates found", Toast.LENGTH_SHORT).show();
                
                // Log multiple results
                if (addresses.size() > 1) {
                    Log.d(TAG, "Found " + addresses.size() + " possible locations for: " + addressText);
                }
            } else {
                Log.w(TAG, "No addresses found for: " + addressText);
                Toast.makeText(this, "No location found. Please try a different search term.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error: " + e.getMessage(), e);
            Toast.makeText(this, "Error finding location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // Process Places Autocomplete results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                // Set selected place
                editTextLocation.setText(place.getAddress());
                
                // Save coordinates
                if (place.getLatLng() != null) {
                    latitude = place.getLatLng().latitude;
                    longitude = place.getLatLng().longitude;
                    
                    Log.d(TAG, "Selected place: " + place.getName());
                    Log.d(TAG, "Address: " + place.getAddress());
                    Log.d(TAG, "Place ID: " + place.getId());
                    Log.d(TAG, "Coordinates: " + latitude + ", " + longitude);
                    
                    Toast.makeText(this, "Location selected: " + place.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.w(TAG, "Selected place has no coordinates");
                    Toast.makeText(this, "Warning: Selected place has no coordinates", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                String errorMessage = status.getStatusMessage();
                Log.e(TAG, "Places API Error Code: " + status.getStatusCode());
                Log.e(TAG, "Places API Error Message: " + errorMessage);
                Toast.makeText(this, "Error selecting location: " + errorMessage, Toast.LENGTH_LONG).show();
                
                // Enable manual input
                editTextLocation.setShowSoftInputOnFocus(true);
                Toast.makeText(this, "You can now manually enter a location instead", Toast.LENGTH_SHORT).show();
                
                // Setup geocoding for manual entry
                editTextLocation.setOnFocusChangeListener((view, hasFocus) -> {
                    if (!hasFocus && editTextLocation.getText().length() > 0) {
                        String address = editTextLocation.getText().toString();
                        geocodeAddressManually(address);
                    }
                });
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Autocomplete cancelled by user");
            }
        }
    }
    
    // Request location permissions
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }
    
    // Get current device location
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        Toast.makeText(this, "Getting current location...", Toast.LENGTH_SHORT).show();
        
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                
                // change to address
                reverseGeocode(location);
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error getting location: " + e.getMessage());
            Toast.makeText(this, "Error getting location", Toast.LENGTH_SHORT).show();
        });
    }
    
    // To change coordinates to address
    private void reverseGeocode(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
                    
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }
                
                String addressText = sb.toString();
                editTextLocation.setText(addressText);
                
                Log.d(TAG, "Current location: " + addressText);
                Log.d(TAG, "Coordinates: " + latitude + ", " + longitude);
                
                Toast.makeText(this, "Current location retrieved", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Reverse geocoding error: " + e.getMessage());
            // Show the coordinates if geocoding fails
            String coordinates = latitude + ", " + longitude;
            editTextLocation.setText(coordinates);
        }
    }
} 