package com.example.lmhymvandroid.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;
import androidx.core.content.ContextCompat;

import com.example.lmhymvandroid.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

public class EventDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;
    private final Drawable drawable;


    public EventDecorator(Context context, Collection<CalendarDay> dates) {
        this.dates = new HashSet<>(dates);
        this.drawable = ContextCompat.getDrawable(context, R.drawable.bg_cal_box_black);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(drawable);
        view.addSpan(new ForegroundColorSpan(Color.WHITE));
    }


}