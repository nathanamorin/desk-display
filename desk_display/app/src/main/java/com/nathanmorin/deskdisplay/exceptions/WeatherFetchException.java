package com.nathanmorin.deskdisplay.exceptions;

public class WeatherFetchException extends Exception{
    public WeatherFetchException(String s){super(s);}
    public WeatherFetchException(Exception ex){super(ex);}
}
