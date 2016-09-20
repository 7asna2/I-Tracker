package com.example.hasna2.soccifillia;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hasna2 on 30-Aug-16.
 */
public abstract class AsyncTask extends android.os.AsyncTask<String,Boolean,LatLng> {
    final String LOG_TAG="AsyncTask";

    String postalCode;
    Uri.Builder builder = new Uri.Builder();
    Uri uri;
    String myUrl;

    public abstract void updateMap(LatLng lastLatLang);
    public abstract void onProgressUpdate(boolean b);

    @Override
    protected void onProgressUpdate(Boolean... values) {
        super.onProgressUpdate(values);
        onProgressUpdate(values[0]);
    }

    @Override
    protected void onPostExecute(LatLng result) {
        if(result!=null) {
            Log.v(LOG_TAG, "on post excute");
            updateMap(result);
        }
    }

    @Override
    protected LatLng doInBackground(String... params) {

        publishProgress(false);
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String JsonStr = null;

        try {

            Log.v(LOG_TAG, "InBackground");
            final String BASE_URL = "http://data.sparkfun.com/output/ZGEnKxa5DLUvr3mmAVWb.json";
            uri = Uri.parse(BASE_URL)
                    .buildUpon()
                    .build();
            myUrl = uri.toString();
            Log.v(LOG_TAG, "Buld URI: " + myUrl);
            URL url = new URL(myUrl);
            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                Log.v(LOG_TAG, "NO INPUTSTREAM");
                publishProgress(true);
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                publishProgress(true);
                return null;
            }
            JsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            publishProgress(true);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        Log.v(LOG_TAG, "JSON STRING: " + JsonStr);
        try {
            publishProgress(true);
            return getLatestLatLng (JsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        publishProgress(true);
        return null;

    }
    private LatLng getLatestLatLng (String JsonStr) throws JSONException {
        final String LATITUDE = "latitude";
        final String LONGITUDE = "longitude";

        JSONArray jsonArray = new JSONArray(JsonStr);
        JSONObject crntJson = jsonArray.getJSONObject(0);

        LatLng crnt = new LatLng(crntJson.getDouble(LATITUDE),crntJson.getDouble(LONGITUDE));
        return crnt;
    }
}


