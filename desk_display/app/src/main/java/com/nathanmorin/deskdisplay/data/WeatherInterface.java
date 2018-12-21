package com.nathanmorin.deskdisplay.data;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nathanmorin.deskdisplay.Config;
import com.nathanmorin.deskdisplay.R;
import com.nathanmorin.deskdisplay.Utils;
import com.nathanmorin.deskdisplay.exceptions.WeatherFetchException;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

public class WeatherInterface {

    private static final String openWeatherWeatherUrl = "https://api.openweathermap.org/data/2.5/weather";
    private static final String openWeatherForcastUrl = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String weatherGovUrl = MessageFormat.format("https://api.weather.gov/gridpoints/{0}/{1}/forecast",Config.NWSOffice, Config.weatherGovMapCoord);
    private Map<String, String> openWeatherParams = new HashMap<String,String>() {{
        put("appid", Config.openWeatherAPIKey);
        put("q", Config.weatherLocation);
        put("units", Config.units);
    }};
    private HashMap<String,String> openWeatherMapConversions;
    private Optional<DayWeather> currentWeather = Optional.empty();
    private Optional<ForcastWeather> forcast = Optional.empty();

    public WeatherInterface(Context context){
        openWeatherMapConversions = new HashMap<>();
        InputStream is = context.getResources().openRawResource(R.raw.open_weather_map_conversions);
        try {
            openWeatherMapConversions =
                    new ObjectMapper().readValue(IOUtils.toString(is, Charset.defaultCharset()),
                            HashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private JsonNode readJSON(String url) throws WeatherFetchException {
        try {
            return readJSON(new URL(url));
        } catch (MalformedURLException e) {
            throw new WeatherFetchException(e);
        }
    }
    private JsonNode readJSON(URL url) throws WeatherFetchException {

        try{
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Accept-Encoding", "gzip");

            InputStream inputStream;
            if ("gzip".equals(con.getContentEncoding())) {
                inputStream = new GZIPInputStream(con.getInputStream());
            }
            else {
                inputStream = con.getInputStream();
            }

            return  (new ObjectMapper()).readTree(inputStream);
        } catch (IOException e) {
            throw new WeatherFetchException(e);
        }
    }


    public void updateWeather() throws WeatherFetchException {

        //Update current weather from OpenWeatherMap
        JsonNode rawWeatherOpenWeatherMap;
        try{
            rawWeatherOpenWeatherMap = readJSON(new URL(openWeatherWeatherUrl + "?" + Utils.getParamString(openWeatherParams)));

        } catch (IOException e) {
            throw new WeatherFetchException(e);
        }

        if (rawWeatherOpenWeatherMap.get("cod").asInt(-1) != 200){
            throw new WeatherFetchException("Error Fetching Weather Data");
        }


        //Update weather / forcast from Weather.gov
        JsonNode rawWeatherWeatherGov = readJSON(weatherGovUrl);


        //Set Current Weather
        currentWeather = Optional.of(new DayWeather(
                new Date(),
                openWeatherMapConversions.get(rawWeatherOpenWeatherMap.get("weather").get(0).get("id").asText("800")),
                rawWeatherWeatherGov.get("properties").get("periods").get(0).get("detailedForecast").asText("Got nothing, look out the window?"),
                rawWeatherOpenWeatherMap.get("main").get("temp").asDouble(0),
                rawWeatherOpenWeatherMap.get("main").get("temp_min").asDouble(0),
                rawWeatherOpenWeatherMap.get("main").get("temp_max").asDouble(0),
                new Date(1000 * (long)rawWeatherOpenWeatherMap.get("sys").get("sunrise").asInt(0)),
                new Date(1000 * (long)rawWeatherOpenWeatherMap.get("sys").get("sunset").asInt(0))

        ));


        //Update forecast
        JsonNode rawForecastOpenWeatherMap;
        try{
            rawForecastOpenWeatherMap = readJSON(new URL(openWeatherForcastUrl + "?" + Utils.getParamString(openWeatherParams)));

        } catch (IOException e) {
            throw new WeatherFetchException(e);
        }

        if (rawForecastOpenWeatherMap.get("cod").asInt(-1) != 200){
            throw new WeatherFetchException("Error Fetching Weather Data");
        }

        JsonNode rawForcast = rawForecastOpenWeatherMap.get("list");
        Map<Integer,DayWeather> days = new HashMap<>();
        rawForcast.forEach((JsonNode prediction) -> {
            Calendar date = Calendar.getInstance(Config.locale);
            date.setTimeInMillis(prediction.get("dt").asLong(0) * 1000);
            date.setTimeZone(Config.tz);

            int dayNum = date.get(Calendar.DAY_OF_YEAR);
            Double tempMax = prediction.get("main").get("temp_max").asDouble(-1000);
            Double tempMin = prediction.get("main").get("temp_min").asDouble(-1000);
            DayWeather weather;
            if (days.containsKey(dayNum)) {
                weather = days.get(dayNum);

                if (tempMin < weather.getTemperatureMin()) {
                    weather.setTemperatureMin(tempMin);
                }
                if (tempMax > weather.getTemperatureMax()) {
                    weather.setTemperatureMax(tempMax);
                }
            } else {
                weather = new DayWeather(
                        date.getTime(),
                        openWeatherMapConversions.get(prediction.get("weather").get(0).get("id").asText("800")),
                        prediction.get("weather").get(0).get("main").asText("Got nothing, look out the window?"),
                        prediction.get("main").get("temp").asDouble(0),
                        tempMin,
                        tempMax,
                        null,
                        null);
            }
            days.put(dayNum,weather);
        });

        forcast = Optional.of(new ForcastWeather(days.values().toArray(new DayWeather[0])));
    }


    public Optional<DayWeather> getCurrentWeather() {
        return currentWeather;
    }

    public Optional<ForcastWeather> getForcast() {return forcast;}

}
