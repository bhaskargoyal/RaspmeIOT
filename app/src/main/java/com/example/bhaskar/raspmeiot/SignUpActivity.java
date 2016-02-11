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

public class SignUpActivity extends AppCompatActivity {

    public final static String myURL = "http://192.168.3.4:80/signup/";
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";
    TextView textRaspId;
    String success;
    EditText editFirstName,editLastName,editUsername,editPassword,editEmailId,editMobileNumber;
    Button buttonSubmit;
    String RaspId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editFirstName = (EditText) findViewById(R.id.editFirstName);
        editLastName = (EditText) findViewById(R.id.editLastName);
        editUsername = (EditText) findViewById(R.id.editUsername);
        editPassword = (EditText) findViewById(R.id.editPassword);
        editEmailId = (EditText) findViewById(R.id.editEmailId);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        editMobileNumber = (EditText) findViewById(R.id.editMobileNumber);



        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        RaspId = message;

        textRaspId = (TextView)findViewById(R.id.textRaspId);
        textRaspId.setText("RaspId : " + message);

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



        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stringUrl = myURL; /*"http://httpbin.org/post"*/     /*"http://httpbin.org/get"*/
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
                Intent i = new Intent( SignUpActivity.this ,SignUpActivity.class);
                startActivity(i);
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
                        String message = "RaspId" + ":" + editUsername.getText().toString();
                        Intent i = new Intent(SignUpActivity.this, HomeActivity.class);
                        i.putExtra(EXTRA_MESSAGE, message);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(SignUpActivity.this, SignUpActivity.class);
                        startActivity(i);
                    }

                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + result + "\"");
                }

                String message = RaspId + ":" +editUsername.getText().toString();
                Intent i = new Intent( SignUpActivity.this ,HomeActivity.class);
                i.putExtra(EXTRA_MESSAGE,message);
                startActivity(i);
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
                String user = editUsername.getText().toString();
                String pass = editPassword.getText().toString();
                String firstName = editFirstName.getText().toString();
                String lastName = editLastName.getText().toString();
                String emailId= editEmailId.getText().toString();
                String mobileNo = editMobileNumber.getText().toString();
                if (user.equalsIgnoreCase("") || pass.equalsIgnoreCase("") ||
                        firstName.equalsIgnoreCase("") || lastName.equalsIgnoreCase("") ||
                        emailId.equalsIgnoreCase("") || mobileNo.equalsIgnoreCase("") ) {
                    Intent i = new Intent(SignUpActivity.this, SignUpActivity.class);
                    startActivity(i);
                }
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("RaspId", "RaspId"));
                params.add(new BasicNameValuePair("Username", user));
                params.add(new BasicNameValuePair("Password", pass));
                params.add(new BasicNameValuePair("First Name", firstName));
                params.add(new BasicNameValuePair("Last Name", lastName));
                params.add(new BasicNameValuePair("Email Id", emailId));
                params.add(new BasicNameValuePair("Mobile Number", mobileNo));


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
