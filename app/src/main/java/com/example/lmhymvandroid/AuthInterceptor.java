package com.example.lmhymvandroid;

import android.content.Context;
import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    // ◀ TokenManager를 사용하기 위해 멤버 변수 선언
    private TokenManager tokenManager;

    // ◀ 생성자에서 Context를 받아 TokenManager를 초기화
    public AuthInterceptor(Context context) {
        this.tokenManager = new TokenManager(context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // ◀ 2단계에서 만든 getAccessToken() 호출
        String accessToken = tokenManager.getAccessToken();

        // 1. Access Token이 있다면, 헤더에 추가
        if (accessToken != null) {
            Request.Builder builder = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + accessToken);
            Request newRequest = builder.build();
            return chain.proceed(newRequest);
        }

        // 2. 토큰이 없다면 (로그인 API 호출 등) 원본 요청 전송
        return chain.proceed(originalRequest);
    }
}