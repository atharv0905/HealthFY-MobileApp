package com.atharv.potholehealthdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageDetailsActivity extends AppCompatActivity {

    String docID = "";
    Config config = new Config();
    private String IP = config.IP;
    private String PORT = config.PORT;
    boolean isMenuOpen = false;
    ImageView menu, image;
    ListView menuListView;
    ArrayList<String> valueList;
    ListView listView;
    DetailsAdapter adapter;
    private final String BASE_URL = "http://"+ IP + ":" + PORT + "/leaf/image/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        docID = getIntent().getExtras().getString("id");
        menu = (ImageView) findViewById(R.id.menu);
        image = (ImageView) findViewById(R.id.image);

        new DownloadImageTask().execute(BASE_URL+docID);
        new GetLeafDetailsTask().execute(docID);

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

        // Sample data
        ArrayList<String> statusList = new ArrayList<>();
        statusList.add("Status");
        statusList.add("Co-ordinates");
        statusList.add("Geo Code");
        statusList.add("Time");

        valueList = new ArrayList<>();

        // Initialize custom adapter
        adapter = new DetailsAdapter(this, statusList, valueList);

        // Attach the adapter to a ListView
        listView = findViewById(R.id.listView);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];

            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            if (result != null) {
                image.setImageBitmap(result);
            } else {
                Toast.makeText(getApplicationContext(), ""+result, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class GetLeafDetailsTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String leafId = params[0];
            JSONObject jsonResponse = null;
            try {
                URL url = new URL("http://"+ IP + ":" + PORT + "/leaf/details/" + leafId); // Replace with your API endpoint
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse JSON response
                    jsonResponse = new JSONObject(response.toString());
                } else {
                    // Handle HTTP error
                }
                connection.disconnect();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject jsonResponse) {
            super.onPostExecute(jsonResponse);
            if (jsonResponse != null) {
                try {
                    String status = jsonResponse.getString("status");
                    String latitude = String.valueOf(jsonResponse.getDouble("latitude"));
                    String longitude = String.valueOf(jsonResponse.getDouble("longitude"));
                    String region = jsonResponse.getString("region");
                    String detectedAt = jsonResponse.getString("detectedAt");

                    valueList.add(status);
                    valueList.add(latitude+", "+longitude);
                    valueList.add(region);
                    valueList.add(detectedAt);
                    listView.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Handle null response
            }
        }
    }
}