package com.nathanmorin.deskdisplay;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.nathanmorin.deskdisplay.data.DayWeather;
import com.nathanmorin.deskdisplay.data.WeatherInterface;
import com.nathanmorin.deskdisplay.views.CalendarView;
import com.nathanmorin.deskdisplay.views.WeatherForcastView;

import java.util.Calendar;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Desktop display for Raspberry Pi Display
 */
public class MainActivity extends Activity {
    TextView tv_weekday;
    TextView tv_day;
    TextView tv_month_year;
    TextView tv_weather_description;
    TextView tv_temperature;
    ImageView iv_weather_icon;
    CalendarView calendarView;
    Handler uiUpdateHandler = new Handler();
    WeatherInterface weatherInterface;
    WeatherForcastView weatherForcastView;

    private GestureDetectorCompat mDetector;
    private BluetoothAdapter mBluetoothAdapter;
    float displayBrightness = 0.1f;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BluetoothReciever", "Action");
//            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//
//            int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
//
//            Log.d("Bluetooth Device", device.getName() + " " + Integer.toString(rssi));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen

        setContentView(R.layout.main_activity);
        tv_weekday = findViewById(R.id.tv_weekday);
        tv_day = findViewById(R.id.tv_day);
        tv_month_year = findViewById(R.id.tv_month_year);
        iv_weather_icon = findViewById(R.id.iv_weather_icon);
        tv_weather_description = findViewById(R.id.tv_weather_description);
        tv_temperature = findViewById(R.id.tv_temperature);
        weatherInterface = new WeatherInterface(getApplicationContext());
        calendarView = findViewById(R.id.calendarView);
        weatherForcastView = findViewById(R.id.weatherForcastView);

        tv_weather_description.setMovementMethod(new ScrollingMovementMethod());

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        executor.scheduleAtFixedRate(this::updateView, 0,100, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::updateWeather, 0,1, TimeUnit.HOURS);
//        executor.execute(this::scanBluetooth);


        mDetector = new GestureDetectorCompat(this, new MyGestureListener());

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Log.d("BluetoothEnabled", Boolean.toString(mBluetoothAdapter.isEnabled()));

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
//        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//        startActivity(discoverableIntent);

        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        mBluetoothAdapter.startDiscovery();

        displayOn();
    }

    public void displayOn(){
        Window window = getWindow();
        WindowManager.LayoutParams layoutpars = window.getAttributes();
        layoutpars.screenBrightness = displayBrightness;
        window.setAttributes(layoutpars);
    }

    public void displayOff(){
        Window window = getWindow();
        WindowManager.LayoutParams layoutpars = window.getAttributes();
        layoutpars.screenBrightness = 0;
        window.setAttributes(layoutpars);
    }

    public void setDisplayBrightness(float brightness) {
        displayBrightness = brightness;
        displayOn();
    }

    private void updateView(){
        Calendar mCalendar = Calendar.getInstance(Config.tz,Config.locale);
        String month = mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Config.locale);
        String year = Integer.toString(mCalendar.get(Calendar.YEAR));
        String weekday = mCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Config.locale);
        String day = Integer.toString(mCalendar.get(Calendar.DAY_OF_MONTH));

        uiUpdateHandler.post(() -> {
            tv_weekday.setText(weekday);
            tv_day.setText(day);
            tv_month_year.setText(month + " " + year);

            calendarView.refreshDay();

            Optional<DayWeather> currentWeather = weatherInterface.getCurrentWeather();
            if (currentWeather.isPresent()){
                tv_weather_description.setText(currentWeather.get().getDescription());
                tv_temperature.setText(currentWeather.get().getFormattedTemperature());
                //Set Weather Icon
                int resourceId = getResources().getIdentifier(currentWeather.get().getIcon(),"drawable", getPackageName());
                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), resourceId);
                iv_weather_icon.setImageDrawable(drawable);
            }

            weatherInterface.getForcast().ifPresent(weatherForcastView::updateForcast);

            weatherInterface.getCurrentWeather().ifPresent(weatherForcastView::updateCurrentWeather);

        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
    }

    private void updateWeather() {
        try {
            weatherInterface.updateWeather();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (event.getPointerCount() == 2) {
            float input = Math.min(Math.max(event.getY(), 100), 400);
            float brightness = Math.abs((input - 100f) / 300f - 1f);
            setDisplayBrightness(brightness);
        } else {
            this.mDetector.onTouchEvent(event);
        }

        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (distanceY > 0) {
                displayOn();
            } else {
                displayOff();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);


        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d("Action", "longpress");
            super.onLongPress(e);
        }
    }
}
