package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lmhymvandroid.TokenManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        TokenManager tokenManager = new TokenManager(this);

        new Handler().postDelayed(() -> {
            // 리프레시 토큰이 있는지 확인
            if (tokenManager.getRefreshToken() != null) {
                // 토큰이 있으면 MainActivity로 이동
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // 토큰이 없으면 LoginActivity로 이동
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish(); // 스플래시 액티비티 종료
        }, 1000); // 1초 대기
    }
}