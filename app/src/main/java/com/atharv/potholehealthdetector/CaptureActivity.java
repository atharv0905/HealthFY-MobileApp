package com.atharv.potholehealthdetector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CaptureActivity extends AppCompatActivity {

    boolean isMenuOpen = false;
    ImageButton captureImgBtn, sendImgBtn;
    ImageView imageDrop, imageDropBackground, menu;
    Bitmap capturedImg;

    ListView menuListView;
    private final int CAMERA_REQUEST_CODE = 100;
    double currentLatitude = 0, currentLongitude = 0;

    private LocationHelper locationHelper;

    String userAddress = "";
    String leafStatus = "";
    private String username = "";
    private SharedPreferences userSharedPreferences;
    private String USER = "USER";
    private String USER_PREF_NAME = "USERNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        checkPermission();

        userSharedPreferences = getSharedPreferences(USER_PREF_NAME, MODE_PRIVATE);

        captureImgBtn = (ImageButton) findViewById(R.id.captureImage);
        sendImgBtn = (ImageButton) findViewById(R.id.sendImage);
        imageDrop = (ImageView) findViewById(R.id.imagedrop);
        imageDropBackground = (ImageView) findViewById(R.id.imagedropbackground);
        menu = (ImageView) findViewById(R.id.menu);
        menuListView = (ListView) findViewById(R.id.menulist);

        List<String> menuList = new ArrayList<>();
        menuList.add("Home");
        menuList.add("History");
        menuList.add("Logout");

        MenuAdapter menuAdapter = new MenuAdapter(this, menuList);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuListView.setAdapter(menuAdapter);
                if(!isMenuOpen){
                    menuListView.setVisibility(View.VISIBLE);
                    isMenuOpen = !isMenuOpen;
                }else {
                    menuListView.setVisibility(View.GONE);
                    isMenuOpen = !isMenuOpen;
                }

            }
        });


        captureImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(iCamera, CAMERA_REQUEST_CODE);
            }
        });

        sendImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
                username = userSharedPreferences.getString(USER, "");
                getCurrentLocation();
                getAddress();
                new AddLeafData().uploadImage(capturedImg, ""+currentLatitude, ""+currentLongitude, username, userAddress, leafStatus);
                showToast("Leaf analyzed successfully", Toast.LENGTH_LONG);
                reset();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch (requestCode) { // Getting data from camera intent
                case CAMERA_REQUEST_CODE:
                    capturedImg = (Bitmap) (data.getExtras().get("data"));
                    new Predict().execute(capturedImg);

                    imageDrop.setBackground(null);
                    imageDrop.setImageBitmap(capturedImg); // setting image on image view
                    imageDropBackground.setVisibility(View.VISIBLE);
                    captureImgBtn.setVisibility(View.GONE);
                    sendImgBtn.setVisibility(View.VISIBLE);

                    break;
            }
        }
    }

    public class Predict extends AsyncTask<Bitmap, Void, String> {

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            Bitmap bitmap = (Bitmap) bitmaps[0];
            String apiUrl = "http://4.240.109.125:5000/predict";
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/octet-stream");
                connection.setDoOutput(true);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(byteArray);
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    return response.toString();
                } else {
                    return "Error: " + responseCode;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String res) {
                leafStatus = res;
        }

    }

    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    public void showToast(String message, int length) {
        Toast.makeText(getApplicationContext(), message, length).show();
    }

    public void checkPermission(){
        locationHelper = new LocationHelper(this, this);
        getCurrentLocation();

        // Check location permission when the activity starts
        if (locationHelper.checkLocationPermission()) {
            // Permission already granted, check if location is enabled
            if (locationHelper.isLocationEnabled()) {
                // Get user's current location and add marker
                // Move camera to the current location
                // Implement this part
            } else {
                // Location is not enabled, show dialog to enable it
                locationHelper.showEnableLocationDialog();
            }
        } else {
            // Request location permission if not granted
            locationHelper.requestLocationPermission();
        }
    }
    private void getCurrentLocation(){
        // Implement the OnLocationListener interface
        LocationHelper.OnLocationListener locationListener = new LocationHelper.OnLocationListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                // Handle the received latitude and longitude
                currentLatitude = latitude;
                currentLongitude = longitude;
            }
        };
        locationHelper.getCurrentLocation(locationListener);
    }

    public void getAddress(){
        locationHelper.getAddressFromLocation(currentLatitude, currentLongitude, new LocationHelper.OnAddressListener() {
            @Override
            public void onAddressReceived(String address) {
                userAddress = address;
            }
        });
    }

    public void reset(){
        captureImgBtn.setVisibility(View.VISIBLE);
        sendImgBtn.setVisibility(View.GONE);
        imageDrop.setImageBitmap(null);
        imageDrop.setBackgroundResource(R.drawable.camera);
        imageDropBackground.setVisibility(View.GONE);
    }

}