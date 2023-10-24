package com.example.bookapp.Translate;

import android.os.AsyncTask;

import com.example.bookapp.activities.DocDetailActivity;
import com.example.bookapp.activities.ProfileActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiGetTask extends AsyncTask<Void, Void, String> {

    public ApiGetTask(){

    }

    @Override
    protected String doInBackground(Void... voids) {
        String iamToken = "";

        try {
            URL url = new URL("https://iam.api.cloud.yandex.net/iam/v1/tokens");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String yandexPassportOauthToken = ""; //your yandex cloud auth token
            String body = String.format("{\"yandexPassportOauthToken\":\"%s\"}", yandexPassportOauthToken);

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(body);
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();
            String response = responseBuilder.toString();
            JSONObject jsonResponse = new JSONObject(response);
            iamToken = jsonResponse.getString("iamToken");

            System.out.println(iamToken);
            writer.close();
            DocDetailActivity.apiKey = iamToken;
            ProfileActivity.apiKey = iamToken;



        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
