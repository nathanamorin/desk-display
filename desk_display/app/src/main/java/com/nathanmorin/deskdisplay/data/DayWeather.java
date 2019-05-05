package com.nathanmorin.deskdisplay.data;

import java.util.Date;

public class DayWeather implements Comparable<DayWeather> {
    /**
     * DayWeather object holding a given day's weather
     */
    private Date date;
    private String icon;
    private String description;
    private Double temperature;
    private Double temperatureMax;
    private Double temperatureMin;
    private Date sunrise;
    private Date sunset;
    private int dayOfYear;

    public DayWeather(Date date, String icon, String description,
                      Double temperature, Double temperatureMin, Double temperatureMax,
                      Date sunrise, Date sunset, int dayOfYear) {
        this.date = date;
        this.icon = icon;
        this.description = description;
        this.temperature = temperature;
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.dayOfYear = dayOfYear;
    }

    public Date getDate() {
        return date;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedTemperature() {
        Double tmp;
        if (temperature == null) {
            tmp = (temperatureMax + temperatureMin) / 2.0;
        } else{
            tmp = temperature;
        }
        return String.format("%.0f°", tmp);
    }


    public Date getSunset() {
        return sunset;
    }

    public Date getSunrise() {
        return sunrise;
    }

    public Double getTemperatureMin() {
        return temperatureMin;
    }

    public Double getTemperatureMax() {
        return temperatureMax;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public void setTemperatureMax(Double temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public void setTemperatureMin(Double temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    @Override
    public int compareTo(DayWeather dayWeather) {
        return Integer.compare(this.dayOfYear, dayWeather.dayOfYear);
    }
}
