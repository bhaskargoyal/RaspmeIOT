package com.example.bhaskar.raspmeiot;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";
    String success;
    public final static String myURL = "http://192.168.3.4:80/register/";
    EditText editRaspID;
    Button buttonRaspID;
    TextView textBuy;
    TextView modeOfConnection;
    TextView textSignIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editRaspID = (EditText) findViewById(R.id.editRaspID);
        buttonRaspID = (Button) findViewById(R.id.buttonRaspID);
        textBuy = (TextView) findViewById(R.id.textBuy);
        modeOfConnection = (TextView) findViewById(R.id.modeOfConnection);
        textSignIn = (TextView) findViewById(R.id.textSignIn);




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
        Log.d(DEBUG_TAG, "Mobile connected: " + isMobileConn);

        if(isWifiConn)
            modeOfConnection.setText("Wifi Connected");
        else if(isMobileConn)
            modeOfConnection.setText("Mobile Connected");
        else
            modeOfConnection.setText("Connect your device through Wifi or Mobile Data");

        buttonRaspID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String stringUrl =   myURL ;/*"https://httpbin.org/post"*/     /*"http://httpbin.org/get"*/
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
        textSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent i = new Intent( MainActivity.this ,SignInActivity.class);
                String message = "StartUpScreen";
                i.putExtra(EXTRA_MESSAGE,message);
                startActivity(i);
            }
        });
    }


    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String s = "Hello";
            try {
                s = downloadUrl(urls[0]);
                return s;
            } catch (Exception e) {
                e.printStackTrace();
                return s;
            }
        }
        // SetText or Not
        @Override
        protected void onPostExecute(String result) {
            if(result.equalsIgnoreCase("Invalid URL")) {
                // to stuff if NOT working fine
                modeOfConnection.setText(result);
            }
            else {
                // to stuff if working fine
                /*
                try to receive it as json object
                 */
                try {

                    JSONObject form = new JSONObject(result);
                    success = form.getString("success");
                    if(success.equalsIgnoreCase("1")){
                        Intent i = new Intent(MainActivity.this, SignUpActivity.class);
                        String message = "RaspId";
                        i.putExtra(EXTRA_MESSAGE, message);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(MainActivity.this, MainActivity.class);
                        startActivity(i);
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

                String rasp_id = editRaspID.getText().toString();
                if(rasp_id.equalsIgnoreCase("")){
                    Intent i = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(i);
                }
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("RaspId", rasp_id));

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
            }finally {
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
        private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException{
            StringBuilder result = new StringBuilder();
            boolean first = true;

            for (NameValuePair pair : params)
            {
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}