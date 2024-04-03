package com.atharv.potholehealthdetector;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddLeafData {

    Config config = new Config();
    private String IP = config.IP;
    private String PORT = config.PORT;
    public void uploadImage(Bitmap imageBitmap,String latitude,String longitude, String username, String region, String status) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://"+ IP + ":" + PORT + "/leaf/add";
        // Convert Bitmap to byte array output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(CompressFormat.JPEG, 100, outputStream);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("photo", "image.jpg",
                        RequestBody.create(MediaType.parse("image/*"), outputStream.toByteArray()))
                .addFormDataPart("latitude",latitude)
                .addFormDataPart("longitude",longitude)
                .addFormDataPart("username", username)
                .addFormDataPart("region", region)
                .addFormDataPart("status", status)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                // Handle successful response
                System.out.println(response.body().string());
            }
        });
    }
}