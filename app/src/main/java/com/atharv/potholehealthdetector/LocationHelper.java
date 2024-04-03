package com.atharv.potholehealthdetector;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM = 15f;

    private final Context context;
    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GoogleMap mMap;

    public LocationHelper(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        fusedLocationClient = new FusedLocationProviderClient(context);
        createLocationCallback();
    }

    public boolean checkLocationPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    public boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void showEnableLocationDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setMessage("Location is disabled. Enable it?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(myIntent);
            }
        });
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // Do nothing
            }
        });
        dialog.show();
    }

    public void getCurrentLocation(final OnLocationListener listener) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(activity, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                        } else {
                            requestNewLocationData(listener);
                        }
                    }
                });
    }

    private void requestNewLocationData(final OnLocationListener listener) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(0);
        locationRequest.setFastestInterval(0);
        locationRequest.setNumUpdates(1);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                listener.onLocationReceived(location.getLatitude(), location.getLongitude());
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    public void startLocationUpdates(final OnLocationListener listener) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(100); // Update every 1 second
        locationRequest.setFastestInterval(100);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                }
            }
        }, Looper.getMainLooper());
    }
    public interface OnLocationListener {
        void onLocationReceived(double latitude, double longitude);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                addMarkerAndMoveCamera(latLng, DEFAULT_ZOOM);
            }
        };
    }

    public void getAddressFromLocation(double latitude, double longitude, final OnAddressListener listener) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                String city = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();
                String knownName = address.getFeatureName();

                // Construct the complete address string
                StringBuilder stringBuilder = new StringBuilder();
                if (fullAddress != null)
                    stringBuilder.append(fullAddress).append(", ");

                String completeAddress = stringBuilder.toString();

                listener.onAddressReceived(completeAddress);
            } else {
                listener.onAddressReceived("Address not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            listener.onAddressReceived("Error retrieving address");
        }
    }

    public interface OnAddressListener {
        void onAddressReceived(String address);
    }

    public String getArea(String input) {
        // Split the string into words
        String[] words = input.split("\\s+");

        // Calculate the index of the fifth word from the end
        int index = words.length - 5;

        // Check if the index is valid
        if (index >= 0 && index < words.length) {
            // Retrieve the word at the calculated index
            String area = words[index];

            // Remove punctuation marks from the extracted word
            area = area.replaceAll("[^a-zA-Z]", ""); // This will remove all non-letter characters

            // Return the cleaned area name
            return area;
        } else {
            // If the index is out of bounds, return null or handle it accordingly
            return null;
        }
    }



    public void addMarkerAndMoveCamera(LatLng latLng, float zoomLevel) {
        if (mMap != null) {
            // Add marker to the map
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location");
            mMap.addMarker(markerOptions);

            // Move camera to the specified location with the specified zoom level
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);
            mMap.animateCamera(cameraUpdate);
        }
    }
}