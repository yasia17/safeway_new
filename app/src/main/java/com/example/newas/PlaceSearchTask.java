package com.example.newas;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class PlaceSearchTask extends AsyncTask<String, Void, JSONArray> {
    private static final String TAG = "PlaceSearchTask";
    private final String apiKey;
    private final PlacesSearchListener listener;

    public interface PlacesSearchListener {
        void onPlacesSearchResult(JSONArray predictions);

        void onPlacesSearchError(String errorMessage);
    }

    public PlaceSearchTask(PlacesSearchListener listener, String apiKey) {
        this.listener = listener;
        this.apiKey = apiKey;
    }

    @Override
    protected JSONArray doInBackground(String... strings) {
        String input = strings[0];
        String urlStr = "https://maps.googleapis.com/maps/api/place/autocomplete/json?" +
                "input=" + URLEncoder.encode(input) +
                "&key=" + apiKey;

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            JSONObject response = new JSONObject(sb.toString());
            return response.getJSONArray("predictions");
        } catch (Exception e) {
            Log.e(TAG, "Error during autocomplete API request: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONArray predictions) {
        if (predictions != null) {
            listener.onPlacesSearchResult(predictions);
        } else {
            listener.onPlacesSearchError("Failed to get search results");
        }
    }
}
