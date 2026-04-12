package com.example.lmhymvandroid.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class BiorhythmGraphView extends View {

    private static final int COLOR_PHYSICAL = Color.parseColor("#F43F5E");
    private static final int COLOR_EMOTIONAL = Color.parseColor("#3B82F6");
    private static final int COLOR_INTELLECTUAL = Color.parseColor("#10B981");
    private static final int COLOR_GRID = Color.parseColor("#33FFFFFF");
    private static final int COLOR_TEXT = Color.parseColor("#A0AEC0");

    private Paint pathPaint, gridPaint, textPaint;
    private Path physicalPath, emotionalPath, intellectualPath;

    private LocalDate birthDate = LocalDate.of(1995, 1, 1);
    private final int DAYS_TO_SHOW = 30;

    public BiorhythmGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(5f);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(COLOR_GRID);
        gridPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(COLOR_TEXT);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        physicalPath = new Path();
        emotionalPath = new Path();
        intellectualPath = new Path();
    }
    public void setBirthDate(String dateString) {
        if (dateString != null && !dateString.isEmpty()) {
            try {
                this.birthDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                invalidate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float paddingX = 80f;
        float paddingY = 50f;

        float graphWidth = width - (paddingX * 2);
        float graphHeight = height - (paddingY * 2);
        float centerY = height / 2f;

        canvas.drawLine(paddingX, paddingY, width - paddingX, paddingY, gridPaint);
        canvas.drawLine(paddingX, centerY, width - paddingX, centerY, gridPaint);
        canvas.drawLine(paddingX, height - paddingY, width - paddingX, height - paddingY, gridPaint);

        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("+100", paddingX - 10, paddingY + 10, textPaint);
        canvas.drawText("0", paddingX - 10, centerY + 10, textPaint);
        canvas.drawText("-100", paddingX - 10, height - paddingY + 10, textPaint);

        physicalPath.reset();
        emotionalPath.reset();
        intellectualPath.reset();

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(DAYS_TO_SHOW / 2);

        float xStep = graphWidth / DAYS_TO_SHOW;

        for (int i = 0; i <= DAYS_TO_SHOW; i++) {
            LocalDate targetDate = startDate.plusDays(i);
            long daysPassed = ChronoUnit.DAYS.between(birthDate, targetDate);

            float currentX = paddingX + (i * xStep);

            float physY = centerY - (float) (Math.sin(2 * Math.PI * daysPassed / 23) * (graphHeight / 2));
            float emotY = centerY - (float) (Math.sin(2 * Math.PI * daysPassed / 28) * (graphHeight / 2));
            float inteY = centerY - (float) (Math.sin(2 * Math.PI * daysPassed / 33) * (graphHeight / 2));

            if (i == 0) {
                physicalPath.moveTo(currentX, physY);
                emotionalPath.moveTo(currentX, emotY);
                intellectualPath.moveTo(currentX, inteY);
            } else {
                physicalPath.lineTo(currentX, physY);
                emotionalPath.lineTo(currentX, emotY);
                intellectualPath.lineTo(currentX, inteY);
            }

            if (i == 0 || i == DAYS_TO_SHOW / 2 || i == DAYS_TO_SHOW) {
                textPaint.setTextAlign(Paint.Align.CENTER);
                String dateStr = targetDate.getMonthValue() + "/" + targetDate.getDayOfMonth();

                if (i == DAYS_TO_SHOW / 2) {
                    textPaint.setColor(Color.WHITE);
                    textPaint.setFakeBoldText(true);
                } else {
                    textPaint.setColor(COLOR_TEXT);
                    textPaint.setFakeBoldText(false);
                }
                canvas.drawText(dateStr, currentX, height - 10, textPaint);
            }
        }

        pathPaint.setColor(COLOR_PHYSICAL);
        canvas.drawPath(physicalPath, pathPaint);

        pathPaint.setColor(COLOR_EMOTIONAL);
        canvas.drawPath(emotionalPath, pathPaint);

        pathPaint.setColor(COLOR_INTELLECTUAL);
        canvas.drawPath(intellectualPath, pathPaint);
    }
}