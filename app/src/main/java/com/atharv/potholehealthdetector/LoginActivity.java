package com.atharv.potholehealthdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    Config config = new Config();
    private String IP = config.IP;
    private String PORT = config.PORT;

    private SharedPreferences sharedPreferences;
    private SharedPreferences userSharedPreferences;
    private String TOKEN = "Token";
    private String USER_AUTHENTICATION_PREF_NAME = "USER_AUTHENTICATION";

    private String USER = "USER";
    private String USER_PREF_NAME = "USERNAME";
    TextView signUpLink;
    Button loginBtn;

    EditText usernameET, passwordET;
    String username = "", password = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences(USER_AUTHENTICATION_PREF_NAME, MODE_PRIVATE);
        userSharedPreferences = getSharedPreferences(USER_PREF_NAME, MODE_PRIVATE);

        loginBtn = (Button) findViewById(R.id.login);
        signUpLink = (TextView) findViewById(R.id.signuplink);

        usernameET = (EditText) findViewById(R.id.username);
        passwordET = (EditText) findViewById(R.id.password);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameET.getText().toString();
                password = passwordET.getText().toString();
                reset();
                login(username, password);
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(signup);
            }
        });
    }

    private class LoginApi extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String apiUrl = "http://"+ IP + ":" + PORT + "/user/login";
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

            if(!result.contains("Error")){
                // saving token
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(TOKEN, result);
                editor.apply();

                // saving username
                SharedPreferences.Editor userEditor = userSharedPreferences.edit();
                userEditor.putString(USER, username);
                userEditor.apply();

                Intent login = new Intent(getApplicationContext(), CaptureActivity.class);
                startActivity(login);
            }else {
                showToast("username or password are incorrect", Toast.LENGTH_LONG);
            }

        }
    }
    public void login(String username, String password){
        if(isValidInput(username, password)){
            JSONObject data = new JSONObject();
            try {
                data.put("username", username);
                data.put("password", password);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            new LoginApi().execute(data.toString());
        }
    }
    public boolean isValidInput(String username, String password){
        if(!username.isEmpty() && !password.isEmpty()){
            return true;
        }
        showToast("Enter username and password");
        reset();
        return false;
    }

    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    public void showToast(String message, int length) {
        Toast.makeText(getApplicationContext(), message, length).show();
    }

    public void reset(){
        usernameET.setText("");
        passwordET.setText("");
    }
    @Override
    protected void onResume() {
        super.onResume();
        reset();
    }
}