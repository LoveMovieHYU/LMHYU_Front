package com.example.lmhymvandroid.Adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import com.example.lmhymvandroid.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class MySelectorDecorator implements DayViewDecorator {

    private final Drawable drawable;

    public MySelectorDecorator(Activity context) {
        drawable = ContextCompat.getDrawable(context, R.drawable.selector_cal_box);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return true;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(drawable);
    }
}