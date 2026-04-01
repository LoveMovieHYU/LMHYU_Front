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

    public TokenAuthenticator(Context context, AuthService authService) {
        this.context = context;
        this.tokenManager = new TokenManager(context);
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

            tokenManager.updateTokens(newTokens.getAccessToken(), newTokens.getRefreshToken());
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