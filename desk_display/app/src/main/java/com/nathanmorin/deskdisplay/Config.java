package com.nathanmorin.deskdisplay;

import java.util.Locale;
import java.util.TimeZone;

public class Config {
    public static final Locale locale = Locale.US;
    public static final TimeZone tz = TimeZone.getTimeZone("America/Indiana/Indianapolis");
    public static final String weatherLocation = "Indianapolis";
    public static final String units = "imperial";

    // See https://api.weather.gov/points/39.7684,-86.1581 as example for getting coord from lat/long.
    // Also see https://en.wikipedia.org/wiki/List_of_National_Weather_Service_Weather_Forecast_Offices for forcast office id
    public static final String weatherGovMapCoord = "57,68";
    public static final String NWSOffice = "IND";

    // Secrets should be retrieved and set to attributes here.
    // Below values reference SecreteConfig file not included in git.
    // Not the best, but is simple hack.
    public static final String openWeatherAPIKey = SecretConfig.openWeatherAPIKey;
}
