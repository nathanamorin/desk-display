package com.nathanmorin.deskdisplay.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.nathanmorin.deskdisplay.Config;
import com.nathanmorin.deskdisplay.data.DayWeather;
import com.nathanmorin.deskdisplay.data.ForcastWeather;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Optional;

public class WeatherForcastView extends View {
    private Context context;

    private Optional<ForcastWeather> forcast = Optional.empty();
    private Optional<DayWeather> currentWeather = Optional.empty();
    private int graphHeight;
    Paint weatherForcastPaint;
    int textSize = 20;
    int textPadding = textSize / 4;
    int iconSize = 40;
    int textSizePadded = textSize + textSize;
    int topOffset;
    int height;
    int width;


    public WeatherForcastView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        weatherForcastPaint = new Paint();
        weatherForcastPaint.setAntiAlias(true);
    }

    public void updateForcast(ForcastWeather forcast){
        if (! this.forcast.isPresent() || ! this.forcast.get().equals(forcast)){
            this.forcast = Optional.of(forcast);
            this.invalidate();
        }
    }
    public void updateCurrentWeather(DayWeather currentWeather){
        if (! this.currentWeather.isPresent() || ! this.currentWeather.get().equals(currentWeather)){
            this.currentWeather = Optional.of(currentWeather);
            this.invalidate();
        }
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.textSize = h/10;
        this.textPadding = textSize / 4;
        this.textSizePadded = textSize + textPadding;
        this.topOffset = iconSize + textSizePadded;
        this.graphHeight = h - this.topOffset;
        this.height = h;
        this.width = w;
    }

    interface ScaledTemp {
        int calcuateScale(Double x);
    }
    interface OffsetCalc {
        int calcuateOffset(String x);
    }

    private void drawTempGraph(Canvas canvas, Paint paint,
                              int pos, int numDays, int viewWidth,
                              DayWeather currentDay,
                              DayWeather nextDay,
                              ScaledTemp scaleTemp) {

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        Path path = new Path();
        int dayWindowWidth = viewWidth / (numDays - 1);
        int yStart = graphHeight + topOffset;
        path.moveTo(pos * dayWindowWidth, yStart - scaleTemp.calcuateScale(currentDay.getTemperatureMax()));
        path.lineTo((pos+1) * dayWindowWidth, yStart - scaleTemp.calcuateScale(nextDay.getTemperatureMax()));
        path.lineTo((pos+1) * dayWindowWidth, yStart - scaleTemp.calcuateScale(nextDay.getTemperatureMin()));
        path.lineTo(pos * dayWindowWidth, yStart - scaleTemp.calcuateScale(currentDay.getTemperatureMin()));

        canvas.drawPath(path, paint);


    }

    private void drawTempInfo(Canvas canvas, Paint paint,
                              int pos, int numDays, int viewWidth,
                              DayWeather currentDay,
                              ScaledTemp scaleTemp){

        int dayWindowWidth = viewWidth / (numDays - 1);
        int yStart = graphHeight + topOffset;

        paint.setTextSize(textSize);

        Double offsetMultiplier;
        if (pos >= numDays - 1) {
            offsetMultiplier = 1.0;
        } else if (pos > 0) {
            offsetMultiplier = 0.5;
        } else {
            offsetMultiplier = 0.0;
        }

        OffsetCalc offsetCalc = (String x) -> (int) Math.ceil(paint.measureText(x) * offsetMultiplier);
        paint.setColor(Color.BLUE);
        canvas.drawText(currentDay.getTemperatureMin().toString(),
                pos*dayWindowWidth - offsetCalc.calcuateOffset(currentDay.getTemperatureMin().toString()),
                yStart - scaleTemp.calcuateScale(currentDay.getTemperatureMin()) + textSizePadded,
                paint);
        paint.setColor(Color.RED);
        canvas.drawText(currentDay.getTemperatureMax().toString(),
                pos*dayWindowWidth - offsetCalc.calcuateOffset(currentDay.getTemperatureMax().toString()),
                yStart - scaleTemp.calcuateScale(currentDay.getTemperatureMax()) - textPadding,
                paint);

        //Draw Day Text + Icons
        int iconOffset = (int) Math.ceil(iconSize * offsetMultiplier);

        Calendar date = Calendar.getInstance(Config.locale);
        date.setTime(currentDay.getDate());
        date.setTimeZone(Config.tz);
        paint.setColor(Color.BLACK);
        canvas.drawText(date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Config.locale),pos*dayWindowWidth - iconOffset,textSize,paint);
        int resourceId = getResources().getIdentifier(currentDay.getIcon(),"drawable", context.getPackageName());
        Drawable drawable = ContextCompat.getDrawable(context, resourceId);
        drawable.setBounds(pos*dayWindowWidth - iconOffset, textSizePadded - 5,pos*dayWindowWidth + iconSize - iconOffset,  textSizePadded - 5 + iconSize);
        drawable.draw(canvas);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (!forcast.isPresent()) {return;}
        super.onDraw(canvas);
//        weatherForcastPaint.setColor(Color.GRAY);
//        canvas.drawRect(0,0,this.getMeasuredWidth(), this.getMeasuredHeight(), weatherForcastPaint);
        Double totalMin = Arrays.stream(forcast.get().getForcast()).min(Comparator.comparing(DayWeather::getTemperatureMin)).get().getTemperatureMin();
        Double totalMax = Arrays.stream(forcast.get().getForcast()).max(Comparator.comparing(DayWeather::getTemperatureMax)).get().getTemperatureMax();

        DayWeather[] days = forcast.get().getForcast();

        ScaledTemp scaleTemp = (Double x) -> (int) Math.round((((graphHeight - textSizePadded*2) * (x - totalMin)) / (totalMax - totalMin)) + textSizePadded);
        //Draw Graph
        for (int pos = 0; pos < days.length-1; pos++) {
            drawTempGraph(canvas, weatherForcastPaint,
                         pos, days.length, this.getMeasuredWidth(),
                         days[pos],
                         days[pos+1],
                         scaleTemp);
        }

        for (int pos = 0; pos < days.length; pos++) {
            drawTempInfo(canvas, weatherForcastPaint,
                    pos, days.length, this.getMeasuredWidth(),
                    days[pos],
                    scaleTemp);
        }
    }
}


