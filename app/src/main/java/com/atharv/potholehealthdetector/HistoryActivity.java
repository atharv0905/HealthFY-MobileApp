package com.atharv.potholehealthdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    Config config = new Config();
    private String IP = config.IP;
    private String PORT = config.PORT;
    private final String BASE_URL = "http://"+ IP + ":" + PORT + "/leaf/image/";
    private final String ID_BASE_URL = "http://"+ IP + ":" + PORT + "/leaf/ids/";
    List<String> idList;

    Bitmap[] images;
    boolean isMenuOpen = false;

    ImageView menu;
    ListView menuListView;
    GridView gridView;
    private String username = "";
    private SharedPreferences userSharedPreferences;
    private String USER = "USER";
    private String USER_PREF_NAME = "USERNAME";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        userSharedPreferences = getSharedPreferences(USER_PREF_NAME, MODE_PRIVATE);
        username = userSharedPreferences.getString(USER, "");
//        new ImageUrlsAPI().execute(ID_BASE_URL + username);
        idList = new ArrayList<>();

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

        new ImageUrlsAPI().execute(ID_BASE_URL + username);
//        gridView = findViewById(R.id.gridview);
    }
    private class DownloadImagesTask extends AsyncTask<List<String>, Void, Bitmap[]> {
        @Override
        protected Bitmap[] doInBackground(List<String>... params) {
            List<Bitmap> bitmaps = new ArrayList<>();
            List<String> imageUrls = params[0];

            for (String imageUrl : imageUrls) {
                try {
                    URL url = new URL(BASE_URL + imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    bitmaps.add(bitmap);
                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return bitmaps.toArray(new Bitmap[0]);
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            super.onPostExecute(bitmaps);
            if (bitmaps.length > 0) {
                images = bitmaps;
                gridView = findViewById(R.id.gridview);
                gridView.setAdapter(new ImageAdapter(getApplicationContext(), images));
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Start the ImageDetailsActivity with the selected image
                        Intent intent = new Intent(HistoryActivity.this, ImageDetailsActivity.class);

                        intent.putExtra("id", idList.get(position));
                        startActivity(intent);
                    }
                });

            } else {
                showToast("Failed to download images");
            }
        }
    }
    private class ImageUrlsAPI extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... urls) {
            List<String> responses = new ArrayList<>();

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                StringBuilder response = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Separate individual URLs
                String[] urlsArray = response.toString().split("\n");
                responses.addAll(Arrays.asList(urlsArray));

                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responses;
        }

        @Override
        protected void onPostExecute(List<String> responses) {
            super.onPostExecute(responses);
            if (!responses.isEmpty()) {
                String responseString = responses.get(0);
                if (responseString.startsWith("[") && responseString.endsWith("]")) {
                    String innerListString = responseString.substring(2, responseString.length() - 2);
                    String[] urlArray = innerListString.replaceAll("\"", "").split(",\\s*");
                    List<String> extractedList = Arrays.asList(urlArray);
                    idList = extractedList;
                    new DownloadImagesTask().execute(idList);
                } else {
                    showToast("Invalid response format");
                }
            } else {
                showToast("Failed to fetch data from server");
            }
        }
    }


    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    public void showToast(String message, int length) {
        Toast.makeText(getApplicationContext(), message, length).show();
    }
}