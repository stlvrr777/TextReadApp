package com.example.bookapp.Translate;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.bookapp.activities.Fb2ViewActivity;
import com.example.bookapp.activities.PdfViewActivity;
import com.example.bookapp.activities.activity_fav_words;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class TranslateTask extends AsyncTask<Void, Void, String> {

    private String iamToken;
    private String lang;
    private String folderId = ""; //your Yandex cloud folder id
    private String text;
    private Context mContext;

    public TranslateTask(Context context, String lang, String text, String iamToken) {
        //this.iamToken = iamToken;
        this.lang = lang;
        this.text = text;
        this.iamToken = iamToken;
        mContext = context;
    }

    @Override
    protected String doInBackground(Void... voids) {


        String result = "";
        try {
            URL url = new URL("https://translate.api.cloud.yandex.net/translate/v2/translate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + iamToken);
            conn.setRequestProperty("Content-Type", "application/json");

            text = text.replace("\"", "|||||");
            String body = String.format("{\"targetLanguageCode\":\"%s\",\"texts\":\"%s\",\"folderId\":\"%s\"}", lang, text, folderId);
            byte[] input = body.getBytes("utf-8");
            OutputStream os = conn.getOutputStream();
            os.write(input, 0, input.length);

            Log.d("translatetag", conn.getResponseMessage());
            System.out.println(conn.getResponseMessage());
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();
            String response = responseBuilder.toString();
            Log.d("translatetag", response);

            JSONObject jsonResponse = new JSONObject(response);
            JSONArray translationsArray = jsonResponse.getJSONArray("translations");
            JSONObject translationObj = translationsArray.getJSONObject(0);
            result = translationObj.getString("text").replace("|||||","\"");
            PdfViewActivity.transText = result;
            Fb2ViewActivity.transText = result;
            activity_fav_words.transText = result;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        PdfViewActivity pdfViewActivity = new PdfViewActivity();
        //pdfViewActivity.toGo();
    }
}


