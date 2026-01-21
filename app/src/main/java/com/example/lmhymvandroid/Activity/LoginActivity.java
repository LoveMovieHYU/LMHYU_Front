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

        // 구글 로그인 버튼
        findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            String backendUrl = "http://10.0.2.2.nip.io:8080/oauth2/authorization/google";
            openWebBrowser(backendUrl);
        });

        // 네이버 로그인 버튼
        findViewById(R.id.button_naver_login).setOnClickListener(v -> {
            String backendUrl = "http://10.0.2.2:8080/oauth2/authorization/naver";
            openWebBrowser(backendUrl);
        });

        // 3. (앱 처음 실행 시) 리다이렉트로 들어왔는지 확인
        handleDeepLink(getIntent());
    }

    // 4. (이미 켜진 앱으로 돌아올 때) 리다이렉트 확인
    // AndroidManifest의 launchMode="singleTask" 덕분에 이 함수가 호출됨
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // 새 인텐트로 교체
        handleDeepLink(intent);
    }

    // 웹 브라우저 실행 함수
    private void openWebBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "브라우저를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // ★ 핵심: URL에서 토큰 뜯어내기
    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();

        // 데이터가 있고, 우리가 설정한 example-app://callback 이 맞는지 확인
        if (data != null && "example-app".equals(data.getScheme()) && "callback".equals(data.getHost())) {

            Log.d("Login", "리다이렉트 URL 감지: " + data.toString());

            // 백엔드가 보내준 쿼리 파라미터 이름("accessToken", "refreshToken")으로 값 추출
            String accessToken = data.getQueryParameter("accessToken");
            String refreshToken = data.getQueryParameter("refreshToken");

            if (accessToken != null && refreshToken != null) {
                Log.d("MY_TOKEN_CHECK", "✅ Access Token: " + accessToken);
                Log.d("MY_TOKEN_CHECK", "✅ Refresh Token: " + refreshToken);
                Log.d("Login", "로그인 성공! 토큰 획득 완료");

                // 1. 토큰 저장 (TokenManager 사용)
                // (userId가 필요한 경우 백엔드에서 param으로 같이 넘겨달라고 하거나, 0으로 임시 저장 후 /me API 호출)
                tokenManager.saveTokens(accessToken, refreshToken, 0);

                // 2. 메인 화면으로 이동
                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                // 뒤로가기 눌렀을 때 로그인 화면 다시 안 나오게 플래그 설정
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
            } else {
                Log.e("Login", "토큰이 URL에 없습니다.");
                Toast.makeText(this, "로그인 정보 수신 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }
}