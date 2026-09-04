package com.example.lmhymvandroid.Service;

import com.example.lmhymvandroid.DTO.LoginResponseDTO;
import com.example.lmhymvandroid.DTO.MovieSummaryResponseDTO;
import com.example.lmhymvandroid.DTO.NicknameRequest;
import com.example.lmhymvandroid.DTO.NicknameUpdateRequest;
import com.example.lmhymvandroid.DTO.ProfileCheckResponseDTO;
import com.example.lmhymvandroid.DTO.UserResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface AuthService {
    @POST("/api/user/update")
    Call<Void> createInitialProfile(@Body NicknameRequest request);

    @PATCH("/api/user/update")
    Call<Void> updateUserInfo(@Body NicknameUpdateRequest request);

    @GET("/api/user/check-profile")
    Call<ProfileCheckResponseDTO> checkProfileStatus();

    @POST("/api/user/logout")
    Call<Void> logout();

    // 액세스 토큰 재발급 요청
    @POST("/jwt/refresh")
    Call<LoginResponseDTO> requestTokenRefresh(@Header("Authorization-Refresh") String refreshToken);

    // 인증 헤더는 AuthInterceptor 가 자동 부착하므로 수동으로 넘기지 않는다
    @GET("/api/user/")
    Call<UserResponseDTO> getUserInfo();

    @DELETE("/api/user/")
    Call<Void> deleteAccount();

    @GET("/api/likes/")
    Call<List<MovieSummaryResponseDTO>> getFavoriteMovies();
}