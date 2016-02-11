package com.example.bhaskar.raspmeiot;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    ToggleButton toggleLight;
    ToggleButton toggleFan;
    public static String myURL = "http://192.168.3.4:80/toggle/";
    TextView textUsername,textRaspId,textSignOutHome,textConnectionHome;
    String RaspId,Username;
    String success;
    int flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toggleFan = (ToggleButton) findViewById(R.id.toggleFanHome);
        toggleLight = (ToggleButton) findViewById(R.id.toggleLightHome);
        textUsername = (TextView) findViewById(R.id.textUsernameHome);
        textRaspId = (TextView) findViewById(R.id.textRaspIdHome);
        textSignOutHome = (TextView) findViewById(R.id.textSignOutHome);
        textConnectionHome = (TextView) findViewById(R.id.textConnectionHome);



        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String[] parts = message.split(":");
        RaspId = parts[0];
        Username = parts[1];
        textUsername.setText("" + Username);
        textRaspId.setText("" + RaspId);


        textSignOutHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i  = new Intent(HomeActivity.this,MainActivity.class);
                startActivity(i);
            }
        });



        /*
        Connection Code
         */
        final String DEBUG_TAG = "NetworkStatusExample";

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();
        Log.d(DEBUG_TAG, "Wifi connected: " + isWifiConn);
        if(isWifiConn) {
            textConnectionHome.setText("Wifi Connected");
        }
        Log.d(DEBUG_TAG, "Mobile connected: " + isMobileConn);
        if(isMobileConn){
            textConnectionHome.setText("Mobile Data Connected");
        }


        toggleLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String stringUrl = myURL ;/*"http://httpbin.org/post"*/      /*"http://httpbin.org/get"*/
                flag =1;
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    DownloadWebpageTask d = new DownloadWebpageTask();
                    d.execute(stringUrl);
                } else {
                    final String DEBUG_TAG = "NetworkStatusExample";
                    Log.d(DEBUG_TAG, "Not Connected");
                }
            }
        });

        //   device Fan

        toggleFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String stringUrl = myURL;/*"http://httpbin.org/post"*/     /*"http://httpbin.org/get"*/
                flag = 2;
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    DownloadWebpageTask d = new DownloadWebpageTask();
                    d.execute(stringUrl);
                } else {
                    final String DEBUG_TAG = "NetworkStatusExample";
                    Log.d(DEBUG_TAG, "Not Connected");
                }
            }
        });

    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            try {
                return downloadUrl(urls[0]);
            } catch (Exception e) {
                return "Invalid";
            }
        }
        // SetText or Not
        @Override
        protected void onPostExecute(String result) {               // result is string as json object
            if(result.equalsIgnoreCase("Invalid")) {
                // to stuff if NOT working fine
                if(flag == 0)
                    textConnectionHome.setText("An ERROR has Occurred fetching All Data");
                else if(flag ==1)
                    textConnectionHome.setText("An ERROR has Occurred : Device 1 Data");
                else if(flag ==2)
                    textConnectionHome.setText("An ERROR has Occurred : Device 2 Data");

            }
            else {
                // to stuff if working fine
                /*
                try to receive it as json object
                 */
                try {

                    JSONObject form = new JSONObject(result);
                    success = form.getString("success");
                    if (success.equalsIgnoreCase("1")) {
                        if(flag == 0){
                            //testing
                        } else if (flag == 1){
                            String deviceState1;
                            deviceState1 = form.getString("status");
                            if (deviceState1.equalsIgnoreCase("1")) {
                                toggleLight.setChecked(true);
                            } else if (deviceState1.equalsIgnoreCase("0")) {
                                toggleLight.setChecked(false);
                            }

                        } else if (flag == 2){
                            String deviceState2;

                            deviceState2 = form.getString("status");

                            if (deviceState2.equalsIgnoreCase("1")) {
                                toggleFan.setChecked(true);
                            } else if (deviceState2.equalsIgnoreCase("0")) {
                                toggleFan.setChecked(false);
                            }
                        }

                    } else {
                        if(flag == 0)
                            textConnectionHome.setText("DATABASE ERROR has Occurred fetching All Data");
                        else if(flag ==1)
                            textConnectionHome.setText("DATABASE ERROR has Occurred : Device 1 Data");
                        else if(flag ==2)
                            textConnectionHome.setText("DATABASE ERROR has Occurred : Device 2 Data");
                    }

                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + result + "\"");
                }


            }




        }
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            int len = 1000;         // length of incoming response

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                if(flag == 0){
                    /*String deviceServer1, deviceServer2;
                    deviceServer1 = "0";
                    deviceServer2 = "0";
                    params.add(new BasicNameValuePair("Success", "1"));
                    params.add(new BasicNameValuePair("RaspId", RaspId));
                    params.add(new BasicNameValuePair("devicestate1", deviceServer1));
                    params.add(new BasicNameValuePair("devicestate2", deviceServer2));*/
                } else {
                    String deviceToggle1, deviceServer1, deviceToggle2, deviceServer2;
                    if (flag == 1) {
                        params.add(new BasicNameValuePair("deviceid", "1"));
                    } else if (flag == 2) {
                        params.add(new BasicNameValuePair("deviceid","2"));

                    }
                }

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                conn.connect();
                int response = conn.getResponseCode();
                final String BUG_TAG = "NetworkStatusExample";
                Log.d(BUG_TAG, "The response is: " + response);
                is = conn.getInputStream();
                String contentAsString = readIt(is, len);
                return contentAsString;

            } finally {
                if (is != null) {
                    is.close();
                }
            }


        }

        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }

        private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;

            for (NameValuePair pair : params) {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            }

            return result.toString();
        }
    }
}
