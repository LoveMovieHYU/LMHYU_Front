package com.example.lmhymvandroid;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    /**
     * ◀◀◀ [수정] getClient()가 Context를 받도록 변경
     * (AuthInterceptor가 TokenManager를 생성할 때 Context가 필요하기 때문)
     */
    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            // ◀◀◀ [추가] AuthInterceptor를 포함한 OkHttpClient 생성
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    // ◀ AuthInterceptor를 추가. applicationContext를 넘겨 메모리 누수 방지
                    .addInterceptor(new AuthInterceptor(context.getApplicationContext()))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // ◀◀◀ [수정] 생성한 okHttpClient를 Retrofit에 적용
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}