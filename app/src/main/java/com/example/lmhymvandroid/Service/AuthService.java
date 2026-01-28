package com.example.lmhymvandroid.Service;

import com.example.lmhymvandroid.DTO.LoginResponseDTO;
import com.example.lmhymvandroid.DTO.NicknameUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthService {
    // 액세스 토큰 재발급 요청
    @POST("/jwt/refresh")
    Call<LoginResponseDTO> requestTokenRefresh(@Header("Authorization-Refresh") String refreshToken);

    // 로그아웃 요청
    @POST("/logout")
    Call<Void> requestLogout(@Header("Authorization-Refresh") String refreshToken);

    @PUT("/api/user/nickname")
    Call<Void> updateNickname(
            @Body NicknameUpdateRequest request
    );
}