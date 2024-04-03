package com.atharv.potholehealthdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FlashActivity extends AppCompatActivity {

    private SharedPreferences userSharedPreferences;
    SharedPreferences sharedPreferences;
    private String TOKEN = "Token";
    private String USER_AUTHENTICATION_PREF_NAME = "USER_AUTHENTICATION";
    private String USER = "USER";
    private String USER_PREF_NAME = "USERNAME";

    Config config = new Config();
    private String IP = config.IP;
    private String PORT = config.PORT;
    AnimationDrawable animationDrawable;

    ImageView logo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);

        sharedPreferences = getSharedPreferences(USER_AUTHENTICATION_PREF_NAME, MODE_PRIVATE);
        userSharedPreferences = getSharedPreferences(USER_PREF_NAME, MODE_PRIVATE);

        logo = (ImageView) findViewById(R.id.logo);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String token = sharedPreferences.getString("Token", "");
                if (token != "") {
                    JSONObject data = new JSONObject();
                    try {
                        data.put("token", token);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    new VerifyUserApi().execute(data.toString());
                } else {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }

    private class VerifyUserApi extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String apiUrl = "http://"+ IP + ":" + PORT + "/user/protected";
            String postData = params[0];
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(postData.getBytes());
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
        protected void onPostExecute(String result) {
            SharedPreferences.Editor userEditor = userSharedPreferences.edit();
            userEditor.putString(USER, result);
            userEditor.apply();
            if(result.contains("Error")){
                logo.setBackgroundResource(R.drawable.error_anim);
                animationDrawable = (AnimationDrawable) logo.getBackground();
                animationDrawable.start();
            }
            else if(result != ""){
                Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
                startActivity(intent);
                finish();
            }else {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}