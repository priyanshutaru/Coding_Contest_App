package com.example.codingcontestreminderapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import com.example.codingcontestreminderapp.Model.dataList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {

    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRVcontest;
    private Adaptercontest mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        mSwipeRefreshLayout.setOnRefreshListener(() -> new AsyncFetch().execute());
        new AsyncFetch().execute();
    }

    private class AsyncFetch extends AsyncTask<String, Void, String> {
        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\t Updating Contests...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                url = new URL("https://kontests.net/api/v1/all");
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            }

            try {
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");
                conn.connect();
                // setDoOutput to true as we recieve data from json file
                // conn.setDoOutput(true);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return e1.toString();
            }
            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    return (result.toString());

                } else {
                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //this method will be running on UI thread
            pdLoading.dismiss();
            if (result != null) {
                List<dataList> data = new ArrayList<>();
                JSONArray jArray;
                //Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                try {
                    jArray = new JSONArray(result);
                    // Extract data from json and store into ArrayList as class objects
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject json_data = jArray.getJSONObject(i);
                        dataList listData = new dataList();
                        listData.setName(json_data.getString("name"));
                        listData.setUrls(json_data.getString("url"));
                        listData.setPlatform(json_data.getString("site"));
                        listData.setStartTime("Starts at:" + json_data.getString("start_time"));
                        double duration = (double)(Double.parseDouble(json_data.getString("duration"))/60.0);
                        listData.setDuration("Duration :" + duration +" mins");
                        listData.setEndTime(json_data.getString("end_time"));



                        data.add(listData);

                    }
                    LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
                    llm.setOrientation(LinearLayoutManager.VERTICAL);
                    mRVcontest.setLayoutManager(llm);
                    mRVcontest.setHasFixedSize(true);
                    mRVcontest.setItemViewCacheSize(50);
                    mRVcontest.setDrawingCacheEnabled(true);
                    mRVcontest.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                    mAdapter = new Adaptercontest(MainActivity.this, data);
                    mRVcontest.setAdapter(mAdapter);
                    mSwipeRefreshLayout.setRefreshing(false);
                } catch (JSONException e) {
                    //Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    mSwipeRefreshLayout.setRefreshing(false);
                }


            }

        }
    }

    public void init() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mRVcontest = findViewById(R.id.contestList);
    }
}
