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

    // 4번째 사진 기준 색상
    private static final int COLOR_PHYSICAL = Color.parseColor("#F43F5E"); // 핑크/레드
    private static final int COLOR_EMOTIONAL = Color.parseColor("#3B82F6"); // 블루
    private static final int COLOR_INTELLECTUAL = Color.parseColor("#10B981"); // 그린
    private static final int COLOR_GRID = Color.parseColor("#33FFFFFF"); // 연한 회색 (격자)
    private static final int COLOR_TEXT = Color.parseColor("#A0AEC0"); // 회색 텍스트

    private Paint pathPaint, gridPaint, textPaint;
    private Path physicalPath, emotionalPath, intellectualPath;

    private LocalDate birthDate = LocalDate.of(1995, 1, 1); // 기본값 (API에서 받아와 업데이트)
    private final int DAYS_TO_SHOW = 30; // 그래프에 보여줄 총 일수 (오늘 기준 앞뒤 15일)

    public BiorhythmGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(5f); // 그래프 선 두께

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

    // 외부(Fragment)에서 생일을 주입받는 메서드
    public void setBirthDate(String dateString) {
        if (dateString != null && !dateString.isEmpty()) {
            try {
                // API에서 넘어오는 날짜 형식에 맞춰 수정 (예: "yyyy-MM-dd")
                this.birthDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                invalidate(); // 데이터가 바뀌었으니 뷰를 다시 그림
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

        // 상하좌우 여백
        float paddingX = 80f;
        float paddingY = 50f;

        float graphWidth = width - (paddingX * 2);
        float graphHeight = height - (paddingY * 2);
        float centerY = height / 2f;

        // 1. 격자 및 X/Y축 텍스트 그리기
        canvas.drawLine(paddingX, paddingY, width - paddingX, paddingY, gridPaint); // 상단선 (+100)
        canvas.drawLine(paddingX, centerY, width - paddingX, centerY, gridPaint); // 중앙선 (0)
        canvas.drawLine(paddingX, height - paddingY, width - paddingX, height - paddingY, gridPaint); // 하단선 (-100)

        // Y축 텍스트 (+100, 0, -100)
        textPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("+100", paddingX - 10, paddingY + 10, textPaint);
        canvas.drawText("0", paddingX - 10, centerY + 10, textPaint);
        canvas.drawText("-100", paddingX - 10, height - paddingY + 10, textPaint);

        // 2. 그래프 그리기 준비
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

            // 사인 곡선 계산식: Y = sin(2π * t / P) * 진폭
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

            // X축 날짜 텍스트 (시작일, 오늘, 마지막일)
            if (i == 0 || i == DAYS_TO_SHOW / 2 || i == DAYS_TO_SHOW) {
                textPaint.setTextAlign(Paint.Align.CENTER);
                String dateStr = targetDate.getMonthValue() + "/" + targetDate.getDayOfMonth();

                // 오늘은 글씨를 하얗고 굵게
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

        // 3. 캔버스에 경로 그리기
        pathPaint.setColor(COLOR_PHYSICAL);
        canvas.drawPath(physicalPath, pathPaint);

        pathPaint.setColor(COLOR_EMOTIONAL);
        canvas.drawPath(emotionalPath, pathPaint);

        pathPaint.setColor(COLOR_INTELLECTUAL);
        canvas.drawPath(intellectualPath, pathPaint);
    }
}