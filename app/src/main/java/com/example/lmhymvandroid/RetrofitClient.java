package com.example.lmhymvandroid;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor; // 임포트 확인
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://161.33.10.6:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            // 1. 로그를 찍어주는 인터셉터 생성
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // 레벨을 BODY로 설정해야 주고받는 데이터 내용이 다 보입니다.
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 2. OkHttpClient에 로그 인터셉터 추가
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    // 기존에 쓰시던 토큰 인터셉터 (그대로 유지)
                    .addInterceptor(new AuthInterceptor(context))
                    // ★ 방금 만든 로그 인터셉터 추가
                    .addInterceptor(logging)
                    .build();

            // 3. Retrofit에 client 연결
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // ★ 여기서 연결
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}