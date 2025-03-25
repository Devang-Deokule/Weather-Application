package com.devang.weathertest;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private EditText editTextCity;
    int backgroundImageResource = R.drawable.clear_bgg; // Default background image
    private TextView textViewCelsius, textViewWeatherStatus, textViewTime;
    private Button buttonRefresh;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(android.R.id.content).setBackgroundResource(backgroundImageResource);
        editTextCity = findViewById(R.id.editTextCity);
        textViewCelsius = findViewById(R.id.textViewCelsius);
        textViewWeatherStatus = findViewById(R.id.textViewWeatherStatus);
        textViewTime = findViewById(R.id.textViewTime);
        buttonRefresh = findViewById(R.id.buttonRefresh);
        // Set current time
        updateTime();
        // Update time every second
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
        // Button click listener
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = editTextCity.getText().toString().trim();
                if (!city.isEmpty()) {
                    new GetWeatherTask().execute(city);
                }
            }
        });
    }
    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        textViewTime.setText(currentTime);
    }
    private class GetWeatherTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            String city = params[0];
            String apiKey = "API key"; // Replace 'API key' with your OpenWeatherMap API key
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                connection.disconnect();
                return new JSONObject(response.toString());
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather data", e);
                return null;
            }
        }
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    // Retrieve temperature data from the weather data
                    JSONObject mainObject = jsonObject.getJSONObject("main");
                    double celsiusTemperature = mainObject.getDouble("temp") - 273.15; // Convert Kelvin to Celsius
                    // Update temperature TextView
                    textViewCelsius.setText(String.format("%.1f °C", celsiusTemperature));
                    // Update weather status TextView
                    JSONArray weatherArray = jsonObject.getJSONArray("weather");
                    JSONObject weatherObject = weatherArray.getJSONObject(0);
                    String weatherStatus = weatherObject.getString("main");
                    textViewWeatherStatus.setText(weatherStatus);
                    // Change background image based on weather status
                    switch (weatherStatus) {
                        case "Smoke":
                            backgroundImageResource = R.drawable.smoke_bgg;
                            break;
                        case "Clear":
                            backgroundImageResource = R.drawable.clear_bgg;
                            break;
                        case "Sunny":
                            backgroundImageResource = R.drawable.sunny_background;
                            break;
                        case "Clouds":
                            backgroundImageResource = R.drawable.clound_bg;
                            break;
                        case "Rain":
                            backgroundImageResource = R.drawable.rain_background;
                            break;
                    }
                    findViewById(android.R.id.content).setBackgroundResource(backgroundImageResource);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing weather data", e);
                    Toast.makeText(MainActivity.this, "Error parsing weather data.\nPlease try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Error fetching weather data: result is null");
                Toast.makeText(MainActivity.this, "Error fetching weather data.\nPlease try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}