package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lmhymvandroid.TokenManager;

import java.util.Calendar;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_splash); // 스플래시 화면 XML이 있다면 주석 해제

        TokenManager tokenManager = new TokenManager(this);

        new Handler().postDelayed(() -> {
            if (tokenManager.getRefreshToken() != null) {
                checkLocalEmotionAndNavigate();
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, 1000); // 1초 대기
    }

    // 내부 저장소(SharedPreferences)를 확인하여 화면 분기 처리
    private void checkLocalEmotionAndNavigate() {
        // EmotionSelectionActivity에서 저장했던 파일명("UserEmotionPref")과 키값을 동일하게 사용해야 합니다.
        SharedPreferences prefs = getSharedPreferences("UserEmotionPref", MODE_PRIVATE);

        // 마지막으로 저장된 시간 가져오기 (없으면 0)
        long savedTime = prefs.getLong("emotion_saved_time", 0);

        if (isToday(savedTime)) {
            // 오늘 날짜로 저장된 기록이 있음 -> 메인 화면으로
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // 오늘 기록이 없음 (처음이거나 날짜가 지남) -> 감정 선택 화면으로
            startActivity(new Intent(SplashActivity.this, EmotionSelectionActivity.class));
        }
        finish(); // 스플래시 종료
    }

    // 저장된 시간이 '오늘'인지 확인하는 헬퍼 함수
    private boolean isToday(long timeInMillis) {
        if (timeInMillis == 0) return false; // 저장된 적 없음

        Calendar savedCal = Calendar.getInstance();
        savedCal.setTimeInMillis(timeInMillis);

        Calendar currentCal = Calendar.getInstance();

        // 연도(Year)와 1년 중 몇 번째 날(Day of Year)이 모두 같아야 "오늘"로 판정
        return savedCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
                savedCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR);
    }
}