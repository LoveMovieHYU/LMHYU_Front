package com.example.lmhymvandroid;

import android.content.Context;

import com.example.lmhymvandroid.Service.AuthInterceptor;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.Service.TokenAuthenticator;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://moov.ai.kr/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            TokenManager tokenManager = new TokenManager(context.getApplicationContext());

            Retrofit authRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            AuthService authService = authRetrofit.create(AuthService.class);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(new AuthInterceptor(tokenManager))
                    .authenticator(new TokenAuthenticator(context.getApplicationContext(), authService, tokenManager))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}