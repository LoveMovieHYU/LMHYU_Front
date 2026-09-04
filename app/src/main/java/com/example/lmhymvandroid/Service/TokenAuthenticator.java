package com.example.lmhymvandroid.Service;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.lmhymvandroid.Activity.LoginActivity;
import com.example.lmhymvandroid.DTO.LoginResponseDTO;
import com.example.lmhymvandroid.TokenManager;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {

    private final Context context;
    private final TokenManager tokenManager;
    private final AuthService authService;

    public TokenAuthenticator(Context context, AuthService authService, TokenManager tokenManager) {
        this.context = context;
        // 주입받은 tokenManager 를 그대로 사용한다 (불필요한 재생성 제거)
        this.tokenManager = tokenManager;
        this.authService = authService;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, Response response) throws IOException {

        if (responseCount(response) >= 2) {
            return null;
        }

        String refreshToken = tokenManager.getRefreshToken();
        if (refreshToken == null) {
            return null;
        }

        Call<LoginResponseDTO> refreshCall = authService.requestTokenRefresh(refreshToken);
        retrofit2.Response<LoginResponseDTO> refreshResponse = refreshCall.execute();

        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
            LoginResponseDTO newTokens = refreshResponse.body();

            // 응답에 refresh 토큰이 없으면 기존 저장값을 유지하고, 새 값이 있을 때만 갱신한다
            String newRefreshToken = newTokens.getRefreshToken() != null
                    ? newTokens.getRefreshToken()
                    : refreshToken;
            tokenManager.updateTokens(newTokens.getAccessToken(), newRefreshToken);
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newTokens.getAccessToken())
                    .build();
        } else {
            tokenManager.clearTokens();

            //유저를 로그인 화면으로 쫓아냄 (기존 쌓인 화면 스택을 모두 날림)
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);

            return null;
        }
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}