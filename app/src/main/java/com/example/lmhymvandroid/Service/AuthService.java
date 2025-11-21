package com.example.lmhymvandroid.Service;

import com.example.lmhymvandroid.DTO.GoogleLoginRequest;
import com.example.lmhymvandroid.DTO.LoginResponseDTO;
import com.example.lmhymvandroid.DTO.NaverLoginRequest;
import com.example.lmhymvandroid.DTO.NicknameUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AuthService {
    // 구글 ID 토큰으로 서버에 로그인 요청
    @POST("/api/v1/auth/google")
    Call<LoginResponseDTO> requestGoogleLogin(@Body GoogleLoginRequest request);

    // 액세스 토큰 재발급 요청
    @POST("/jwt/refresh")
    Call<LoginResponseDTO> requestTokenRefresh(@Header("Authorization-Refresh") String refreshToken);

    // 네이버 인증 코드로 서버에 로그인 요청
    @POST("/api/v1/auth/naver")
    Call<LoginResponseDTO> requestNaverLogin(@Body NaverLoginRequest request);

    // 로그아웃 요청
    @POST("/logout") // 백엔드의 SecurityConfig에 설정된 로그아웃 URL
    Call<Void> requestLogout(@Header("Authorization-Refresh") String refreshToken);

    @PUT("/api/user/{userId}/nickname")
    Call<Void> updateNickname(
            @Path("userId") int userId,
            @Body NicknameUpdateRequest request
    );
}