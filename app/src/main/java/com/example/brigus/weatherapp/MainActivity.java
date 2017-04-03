package com.example.brigus.weatherapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.example.brigus.weatherapp.models.Location;
import com.example.brigus.weatherapp.utils.WeatherAPI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    //http://www.jsonschema2pojo.org

    private static final String WEATHER_URL = "http://api.openweathermap.org";
    private static final String COUNTRY_CODE = "us";

    @BindView(R.id.get_weather_button) Button getWeatherButton;
    @BindView(R.id.high_textview) TextView highTV;
    @BindView(R.id.low_textview) TextView lowTV;
    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;

    @OnClick(R.id.get_weather_button) void getWeatherData() {
        EditText zipET = (EditText) findViewById(R.id.zipText);
        String enteredZip = zipET.getText().toString();

        //loadWeatherRetrofit(enteredZip);
        //loadWeather(enteredZip);
        new MyWeatherTask().execute(enteredZip);
        Snackbar.make(coordinatorLayout, "Downloading weather for: " + enteredZip, Snackbar.LENGTH_SHORT).show();
    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }


    //This is using a Retrofit to call the API
    private void loadWeatherRetrofit(String enteredZip) {

        if(enteredZip.length() != 5) {
            Toast.makeText(this, "Cannot be less than 5 or more than 5", Toast.LENGTH_SHORT).show();
        } else {

            //CertificatePinner certificatePinner = new CertificatePinner.Builder()
                    //.add("Patter", "Pin")
                    //.build();
            //final OkHttpClient client = new OkHttpClient.Builder().certificatePinner(certificatePinner).build();

            final OkHttpClient client = new OkHttpClient();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(WEATHER_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            WeatherAPI service = retrofit.create(WeatherAPI.class);

            Map<String, String> data = new LinkedHashMap<>();
            data.put("zip", enteredZip + "," + COUNTRY_CODE);
            data.put("appid", getString(R.string.api_key));

            Call<Location> call = service.getWeatherInfo(data);

            call.enqueue(new Callback<Location>() {
                @Override
                public void onResponse(Call<Location> call, Response<Location> response) {

                    try {

                        Location location = response.body();

                        highTV.setText(location.getMain().getTempMax().toString());
                        lowTV.setText(location.getMain().getTempMin().toString());
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Something bad happened.  But I got data!!", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Location> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Something bad happened", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    //This is calling the API from an AsyncTask
    private class MyWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            URL url = null;

            String queryString = "/data/2.5/weather?zip=" + params[0] + "," + COUNTRY_CODE + "&appid=" + getString(R.string.api_key);


            try {
                url = new URL(WEATHER_URL + queryString);
                connection = (HttpURLConnection) url.openConnection();

                int responseCode = connection.getResponseCode();
                String responseMsg =  connection.getResponseMessage();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    String responseString = streamToString(connection.getInputStream());
                    Log.v("CatalogClient-Response", responseString);
                    return responseString;

                }else{
                    Log.v("CatalogClient", "Response code:"+ responseCode);
                    Log.v("CatalogClient", "Response message:"+ responseMsg);
                }

            } catch (Exception e) {

                e.printStackTrace();

            }  finally {

                if(connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            //convert string to json
            JsonParser jsonParser = new JsonParser();
            final JsonObject jo = (JsonObject)jsonParser.parse(response);

            final String tempMax = jo.get("main").getAsJsonObject().get("temp_max").getAsString();
            final String tempMin = jo.get("main").getAsJsonObject().get("temp_min").getAsString();

            highTV.setText(tempMax);
            lowTV.setText(tempMin);
        }


        private String streamToString(InputStream inputStream) {

            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
    


    //This is using a thread to call the API
    private void loadWeather(final String enteredZip) {


            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    URL url = null;

                    String queryString = "/data/2.5/weather?zip=" + enteredZip + "," + COUNTRY_CODE + "&appid=" + getString(R.string.api_key);


                    try {
                        url = new URL(WEATHER_URL + queryString);
                        connection = (HttpURLConnection) url.openConnection();

                        int responseCode = connection.getResponseCode();
                        String responseMsg =  connection.getResponseMessage();

                        if(responseCode == HttpURLConnection.HTTP_OK){
                            String responseString = readStream(connection.getInputStream());

                            //convert string to json
                            JsonParser jsonParser = new JsonParser();
                            final JsonObject jo = (JsonObject)jsonParser.parse(responseString);

                            final String tempMax = jo.get("main").getAsJsonObject().get("temp_max").getAsString();
                            final String tempMin = jo.get("main").getAsJsonObject().get("temp_min").getAsString();


                            //update the ui
                            highTV.post(new Runnable() {
                                @Override
                                public void run() {
                                    highTV.setText(tempMax);
                                    lowTV.setText(tempMin);
                                }
                            });

                            Log.v("CatalogClient-Response", responseString);

                        }else{
                            Log.v("CatalogClient", "Response code:"+ responseCode);
                            Log.v("CatalogClient", "Response message:"+ responseMsg);
                        }

                    } catch (Exception e) {

                        e.printStackTrace();

                    }  finally {

                        if(connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            });
            thread.start();


    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}
