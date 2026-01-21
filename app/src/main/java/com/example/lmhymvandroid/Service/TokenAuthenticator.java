package com.example.lmhymvandroid.Service;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.lmhymvandroid.DTO.LoginResponseDTO;
import com.example.lmhymvandroid.TokenManager;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {

    private TokenManager tokenManager;
    private AuthService authService; // 토큰 갱신 API를 호출하기 위해 필요

    public TokenAuthenticator(Context context, AuthService authService) {
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
        }

        return null;
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}