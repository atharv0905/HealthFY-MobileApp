package com.atharv.potholehealthdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class SignupActivity extends AppCompatActivity {

    Config config = new Config();
    private String IP = config.IP;
    private String PORT = config.PORT;
    TextView loginLink;
    Button signUpBtn;
    EditText usernameET, passwordET, confirmPasswordET;
    String username = "", password = "", confirmPassword = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signUpBtn = (Button) findViewById(R.id.signup);
        loginLink = (TextView) findViewById(R.id.loginlink);

        usernameET = (EditText) findViewById(R.id.username);
        passwordET = (EditText) findViewById(R.id.password);
        confirmPasswordET = (EditText) findViewById(R.id.confirmpassword);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = usernameET.getText().toString();
                password = passwordET.getText().toString();
                confirmPassword = confirmPasswordET.getText().toString();
                reset();
                signUp(username, password, confirmPassword);
//                Intent signup = new Intent(getApplicationContext(), LoginActivity.class);
//                startActivity(signup);
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(login);
            }
        });
    }

    private class SignUpApi extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String apiUrl = "http://"+ IP + ":" + PORT + "/user/create";
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
            if (result.contains("Error: 301")) {
                showToast("username already taken");
            } else {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        }
    }
    public void signUp(String username, String password, String confirmPassword){
        if(isValidInput(username, password, confirmPassword)){
            JSONObject data = new JSONObject();
            try {
                data.put("username", username);
                data.put("password", password);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            new SignUpApi().execute(data.toString());
        }
    }
    public boolean isValidInput(String username, String password, String confirmPassword){
        if(!username.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()){
            if(username.equals(password)){
                showToast("username and password cannot be same");
                reset();
                return false;
            }
            else if(password.length() < 8){
                showToast("password should be of minimum 8 characters");
                reset();
                return false;
            }
            else if(!password.equals(confirmPassword)){
                showToast("password and confirm password doesn't match");
                reset();
                return false;
            }
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
        confirmPasswordET.setText("");
    }
    @Override
    protected void onResume() {
        super.onResume();
        reset();
    }
}