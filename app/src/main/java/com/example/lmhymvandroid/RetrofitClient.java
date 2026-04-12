package com.example.lmhymvandroid;

import android.content.Context;

import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.Service.TokenAuthenticator;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://moov.ai.kr";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient basicClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            Retrofit basicRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(basicClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            AuthService authService = basicRetrofit.create(AuthService.class);

            TokenAuthenticator authenticator = new TokenAuthenticator(context, authService);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .authenticator(authenticator)
                    .addInterceptor(logging)
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