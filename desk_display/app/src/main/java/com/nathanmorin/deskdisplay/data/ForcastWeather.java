package com.nathanmorin.deskdisplay.data;

import java.util.Date;
import java.util.List;

public class ForcastWeather {
    /**
     * ForcastWeather holds a number of DayWeather objects
     */

    private DayWeather[] forcast;
    private Date lastUpdate;

    public ForcastWeather(DayWeather[] forcast) {
        this.forcast = forcast;
        this.lastUpdate = new Date();
    }

    public DayWeather[] getForcast() {
        return forcast;
    }

    public void setForcast(DayWeather[] forcast) {
        this.forcast = forcast;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ForcastWeather)){
            return false;
        }
        return ((ForcastWeather) obj).lastUpdate.equals(this.lastUpdate);
    }
}
