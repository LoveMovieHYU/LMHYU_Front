package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lmhymvandroid.DTO.ProfileCheckResponseDTO;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.ToastUtil;
import com.example.lmhymvandroid.TokenManager;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);

        // 구글/네이버 로그인 버튼 설정
        findViewById(R.id.sign_in_button).setOnClickListener(v ->
                openWebBrowser("http://10.0.2.2.nip.io:8080/oauth2/authorization/google"));

        findViewById(R.id.button_naver_login).setOnClickListener(v ->
                openWebBrowser("http://10.0.2.2:8080/oauth2/authorization/naver"));

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
            ToastUtil.show(this, "브라우저를 열 수 없습니다.");
        }
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();

        if (data != null && "example-app".equals(data.getScheme()) && "callback".equals(data.getHost())) {
            Log.d("Login", "리다이렉트 URL 감지: " + data.toString());

            String accessToken = data.getQueryParameter("accessToken");
            String refreshToken = data.getQueryParameter("refreshToken");
            String userIdStr = data.getQueryParameter("userId");

            if (accessToken != null && refreshToken != null) {
                int userId = (userIdStr != null) ? Integer.parseInt(userIdStr) : -1;

                // 1. 토큰 즉시 저장
                tokenManager.saveTokens(accessToken, refreshToken, userId);

                // 2. 서비스 인스턴스 초기화 (저장된 토큰 반영)
                authService = RetrofitClient.getClient(this).create(AuthService.class);

                // 3. 서버에 프로필 상태 확인 요청
                checkUserStatusAndNavigate();
            }
        }
    }

    private void checkUserStatusAndNavigate() {
        authService.checkProfileStatus().enqueue(new Callback<ProfileCheckResponseDTO>() {
            @Override
            public void onResponse(Call<ProfileCheckResponseDTO> call, Response<ProfileCheckResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String jsonResponse = new Gson().toJson(response.body());
                    Log.d("Login", "서버 응답 JSON: " + jsonResponse);

                    ProfileCheckResponseDTO status = response.body();


                    if (status.isChecked()) {
                        Log.d("Login", "기존 유저 확인 -> 메인 화면 이동");
                        navigateTo(MainActivity.class, null);
                    } else {
                        String missing = status.getMissingField();
                        Log.d("Login", "정보 누락 유저 -> 설정 이동 (누락 필드: " + missing + ")");
                        navigateTo(CreateNicknameActivity.class, missing);
                    }
                } else {
                    Log.e("Login", "API 호출 실패 코드: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProfileCheckResponseDTO> call, Throwable t) {
                Log.e("Login", "네트워크 에러: " + t.getMessage());
            }
        });
    }

    private void navigateTo(Class<?> targetClass, String missingField) {
        Intent intent = new Intent(LoginActivity.this, targetClass);
        if (missingField != null) {
            intent.putExtra("MISSING_FIELD", missingField);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}