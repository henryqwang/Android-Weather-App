package com.example.android.simpleweatherapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    EditText locationEntered;
    TextView weatherInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationEntered = (EditText)findViewById(R.id.locationEntered);
        weatherInfo = (TextView)findViewById(R.id.weatherInfo);
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            try {
                //Connect to web page
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                //Set up byte reader
                InputStream ins = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(ins);

                //Read raw data from web page
                String webContent = "";
                int data = reader.read();

                while(data != -1){
                    webContent += (char)data;
                    data = reader.read();
                }

                return webContent;

            } catch (Exception e){
                Toast.makeText(getApplicationContext(), "Could not find weather.\nMake sure that the city is correcly spelled", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        //Parsing & formatting the web content
        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            String textToOutput = "";
            try {

                //Manipulate content using JSON
                JSONObject jsonObject = new JSONObject(result);
                String weather = jsonObject.getString("weather");

                JSONArray arr = new JSONArray(weather);
                for(int i = 0; i < arr.length(); i++){
                    JSONObject jsonPart = arr.getJSONObject(i);

                    String main = jsonPart.getString("main");
                    String description = jsonPart.getString("description");

                    if(main != "" && description != ""){
                        textToOutput += main+": "+description+"\r\n";
                    }
                }

                //Display if the message is non empty
                if(textToOutput != ""){
                    weatherInfo.setText(textToOutput);
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Could not find weather.\nMake sure that the city is correcly spelled", Toast.LENGTH_LONG).show();
            }
        }
    }
    public void findWeather(View view){

        //Hide keyboard immediately after click
        InputMethodManager mgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(locationEntered.getWindowToken(), 0);

        try {
            //Extra step that takes care of cities with space in their name, e.g. Los Angeles
            String encodedCityName = URLEncoder.encode(locationEntered.getText().toString(), "UTF-8");

            //Then get weather info from web by passing the city name as argument in an instance of DownloadTask
            DownloadTask task = new DownloadTask();
            task.execute("http://api.openweathermap.org/data/2.5/weather?q="+encodedCityName+"&appid=7030cde68b1beb08f596d8523f0bc882");

        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), "Could not find weather.\nMake sure that the city is correcly spelled", Toast.LENGTH_LONG).show();
        }
    }
}