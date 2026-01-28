package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.TokenManager;

public class LoginActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);


        findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            String backendUrl = "http://10.0.2.2.nip.io:8080/oauth2/authorization/google";
            openWebBrowser(backendUrl);
        });


        findViewById(R.id.button_naver_login).setOnClickListener(v -> {
            String backendUrl = "http://10.0.2.2:8080/oauth2/authorization/naver";
            openWebBrowser(backendUrl);
        });

        handleDeepLink(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }


    private void openWebBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();

        if (data != null && "example-app".equals(data.getScheme()) && "callback".equals(data.getHost())) {

            Log.d("Login", "리다이렉트 URL 감지: " + data.toString());


            String accessToken = data.getQueryParameter("accessToken");
            String refreshToken = data.getQueryParameter("refreshToken");
            String userIdStr = data.getQueryParameter("userId");
            String isNewUserStr = data.getQueryParameter("isNewUser");

            if (accessToken != null && refreshToken != null) {


                int userId = (userIdStr != null) ? Integer.parseInt(userIdStr) : -1;
                boolean isNewUser = "true".equals(isNewUserStr);


                tokenManager.saveTokens(accessToken, refreshToken, userId);

                if (isNewUser) {
                    Log.d("Login", "신규 유저 -> 닉네임 설정 이동");
                    Intent nicknameIntent = new Intent(LoginActivity.this, CreateNicknameActivity.class);
                    nicknameIntent.putExtra("USER_ID", userId);
                    nicknameIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(nicknameIntent);
                } else {
                    Log.d("Login", "기존 유저 -> 홈화면 이동");
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                }
                finish();
            } else {
                Log.e("Login", "토큰이 URL에 없습니다.");
                Toast.makeText(this, "로그인 정보 수신 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }
}