package com.nathanmorin.deskdisplay.views;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nathanmorin.deskdisplay.Config;
import com.nathanmorin.deskdisplay.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CalendarView extends android.support.v4.view.ViewPager {
    int NUM_ITEMS = 1000;
    int START_POS = NUM_ITEMS / 2;
    int currentDayOfYear;
    private Context context;

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        currentDayOfYear = Calendar.getInstance(Config.tz, Config.locale).get(Calendar.DAY_OF_YEAR);
        drawCalendar();

    }

    private void drawCalendar(){
        super.setAdapter(new CustomPagerAdapter(context));

        super.setCurrentItem(START_POS);
    }

    public void refreshDay() {
        if (Calendar.getInstance(Config.tz, Config.locale).get(Calendar.DAY_OF_YEAR) != currentDayOfYear){
            drawCalendar();
        }
    }

    public class CustomPagerAdapter extends PagerAdapter {

        private Context mContext;

        public CustomPagerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            List<String[]> weeks = new ArrayList<>();
            weeks.add(new String[]{"Su", "Mo", "Tu", "We", "Th", "Fi", "Sa"});
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.calendar_view, collection, false);
            int monthRelativeToCurrent = position - START_POS;

            //Setup Calendar Page
            Calendar currentDay = Calendar.getInstance(Config.tz, Config.locale);
            int currentDayOfMonth = currentDay.get(Calendar.DAY_OF_MONTH);
            int currentYear = currentDay.get(Calendar.YEAR);
            int currentMonth = currentDay.get(Calendar.MONTH);

            Calendar displayedCalendar = Calendar.getInstance(Config.tz, Config.locale);
            displayedCalendar.add(Calendar.MONTH, monthRelativeToCurrent);
            int numDaysInMonth = displayedCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            displayedCalendar.setFirstDayOfWeek(Calendar.SUNDAY);
            displayedCalendar.set(Calendar.DAY_OF_MONTH,1);
            int calViewYear = displayedCalendar.get(Calendar.YEAR);
            int calViewMonth = displayedCalendar.get(Calendar.MONTH);


            String[] week = new String[7];
            int dayOfWeek = displayedCalendar.get(Calendar.DAY_OF_WEEK);
            int dayOfMonth = 1;

            while (dayOfMonth <= numDaysInMonth) {
                if (dayOfWeek > 7) {
                    weeks.add(week);
                    week = new String[7];
                    dayOfWeek = 1;
                }
                String day;
                if (calViewYear == currentYear &&
                        calViewMonth == currentMonth &&
                        dayOfMonth == currentDayOfMonth){
                    day = "<font color=\"red\">" + Integer.toString(dayOfMonth) + "</font>";
                } else {
                    day = Integer.toString(dayOfMonth);
                }
                week[dayOfWeek-1] = day;

                dayOfWeek++;
                dayOfMonth++;
            }

            weeks.add(week);

            StringBuilder calOutput = new StringBuilder();
            for (String[] w : weeks) {
                String[] formattedWeek = Arrays.stream(w).map((s) -> {
                    if (s == null || s.equals("")){
                        return "&nbsp;&nbsp;";
                    } else if (s.length() <= 1){
                        return "&nbsp;" + s;
                    } else {
                        return s;
                    }
                }).toArray(String[]::new);
                calOutput.append(String.join(" ", formattedWeek)).append("<br>");
            }


            //calendarOutput += Integer.toString(monthRelativeToCurrent) + " " + Integer.toString(numDays) + "\n";
            TextView tv_main = layout.findViewById(R.id.tv_calendar_view_text);
            tv_main.setText(Html.fromHtml(calOutput.toString(),Html.FROM_HTML_MODE_COMPACT));


            TextView tv_title = layout.findViewById(R.id.tv_calendar_view_title);
            String monthName = displayedCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Config.locale);
            String yearName = Integer.toString(displayedCalendar.get(Calendar.YEAR));
            tv_title.setText(monthName + ", " + yearName);
            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }


}


