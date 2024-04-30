package com.example.weatherforcast;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weatherforcast.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import com.google.android.gms.tasks.OnSuccessListener;
import android.location.Location;
import androidx.annotation.NonNull;


public class MainActivity extends AppCompatActivity {
//2c60182e74704daac1e1c0cae9ce80ce

    private ActivityMainBinding binding;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fetchWeatherData();

        SearchCity();
    }
    private void fetchWeatherData() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            return;
        }

        // Fetch the last known location
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Use the user's current location to fetch the weather data
                    fetchWeatherData(location.getLatitude() + "," + location.getLongitude());
                } else {
                    // Handle the case where the user's location is not available
                    Log.d("TAG", "Location is null");
                }
            }
        });
    }

    private void fetchWeatherData(String coordinates) {
        Gson gson = new GsonBuilder().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ApiInterface service = retrofit.create(ApiInterface.class);
        Call<WeatherData> call = service.getWeatherData(coordinates,"2c60182e74704daac1e1c0cae9ce80ce","metric");
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if(response.isSuccessful()){
                    WeatherData weatherData = response.body();
                    if(weatherData != null)
                    {
//                    Log.d("TAG","City: "+ weatherData.name);
//                    Log.d("TAG", "Temp: "+ weatherData.main.temp);
                        binding.cityName.setText(weatherData.name);
                        binding.temp.setText(String.valueOf(weatherData.main.temp));
                        binding.maxTemp.setText(String.valueOf(weatherData.main.temp_max));
                        binding.minTemp.setText(String.valueOf(weatherData.main.temp_min));
                        binding.humidity.setText(String.valueOf(weatherData.main.humidity));
                        binding.windSpeed.setText(String.valueOf(weatherData.wind.speed));


                        long sunriseInMillis = weatherData.sys.sunrise * 1000; // Convert seconds to milliseconds
                        long sunsetInMillis = weatherData.sys.sunset * 1000; // Convert seconds to milliseconds

// Convert milliseconds to hours and minutes
                        int sunriseHour = (int) ((sunriseInMillis / (1000 * 60 * 60)) % 24);
                        int sunriseMinute = (int) ((sunriseInMillis / (1000 * 60)) % 60);

                        int sunsetHour = (int) ((sunsetInMillis / (1000 * 60 * 60)) % 24);
                        int sunsetMinute = (int) ((sunsetInMillis / (1000 * 60)) % 60);

// Display the sunrise and sunset times
                        binding.sunrise.setText(String.format(Locale.getDefault(), "%02d:%02d", sunriseHour, sunriseMinute));
                        binding.sunset.setText(String.format(Locale.getDefault(), "%02d:%02d", sunsetHour, sunsetMinute));



                        binding.condition.setText(String.valueOf(weatherData.weather.get(0).description));
                        binding.weather.setText(String.valueOf(weatherData.weather.get(0).description));


                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

                        String currentDate = dateFormat.format(calendar.getTime());
                        String currentDay = dayFormat.format(calendar.getTime());

                        binding.date.setText(currentDate);
                        binding.day.setText(currentDay);

                        String condition = binding.condition.getText().toString();

                        changeImagesAccordingToWeatherCondition(condition);


                    }
                }
                else{
                    Log.e("TAG","Retrofit Error" + response.message());
                }
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Log.e("TAG","Retrofit Error" + t.getMessage());
            }
        });
    }

    private void changeImagesAccordingToWeatherCondition(String conditions) {
        Log.d("check", "Weather Condition: " + conditions);
        switch (conditions) {
            case "clear sky":
            case "sunny":
            case "clear":
                Log.d("check", "Setting sunny background and sun animation");

                binding.getRoot().setBackgroundResource(R.drawable.newsunny);
                binding.lottieAnimationView.setAnimation(R.raw.sun);
                binding.lottieAnimationView.playAnimation();
                break;
            case "partly clouds":
            case "clouds":
            case "overcast":
            case "mist":
            case "foggy":
            case "haze":
                Log.d("check", "Setting cloud background and cloud animation");
                binding.getRoot().setBackgroundResource(R.drawable.newcloud);
                binding.lottieAnimationView.setAnimation(R.raw.cloud);
                binding.lottieAnimationView.setSpeed(0.5f);
                binding.lottieAnimationView.playAnimation();
                break;
            case "light rain":
            case "drizzle":
            case "moderate rain":
            case "showers":
            case "heavy rain":
                Log.d("check", "Setting rain background and rain animation");
                binding.getRoot().setBackgroundResource(R.drawable.newrainy);
                binding.lottieAnimationView.setAnimation(R.raw.rain);
                binding.lottieAnimationView.setSpeed(0.5f);
                binding.lottieAnimationView.playAnimation();

                break;
            case "light snow":
            case "moderate snow":
            case "heavy snow":
            case "blizzard":
                Log.d("check", "Setting snow background and snow animation");
                binding.getRoot().setBackgroundResource(R.drawable.newsnowy);
                binding.lottieAnimationView.setAnimation(R.raw.snow);
                binding.lottieAnimationView.setSpeed(0.5f);
                binding.lottieAnimationView.playAnimation();
                break;
            default:
                Log.d("check", "Setting default background and default animation");
                // Handle the case where the conditions string is not one of the above options
                binding.getRoot().setBackgroundResource(R.drawable.colud_background);
                binding.lottieAnimationView.setAnimation(R.raw.cloudy);
                binding.lottieAnimationView.setSpeed(0.5f);
                binding.lottieAnimationView.playAnimation();
                break;
        }
    }




    private void SearchCity() {

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                fetchWeatherData(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchWeatherData();
            } else {
                Log.d("TAG", "Location permission denied");
            }
        }
    }
}