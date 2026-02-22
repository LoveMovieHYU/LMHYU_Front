package com.example.lmhymvandroid.Service;

import com.example.lmhymvandroid.DTO.LoginResponseDTO;
import com.example.lmhymvandroid.DTO.MovieSummaryResponseDTO;
import com.example.lmhymvandroid.DTO.NicknameUpdateRequest;
import com.example.lmhymvandroid.DTO.ProfileCheckResponseDTO;
import com.example.lmhymvandroid.DTO.UserResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthService {
    @PUT("/api/user/update")
    Call<Void> updateUserInfo(@Body NicknameUpdateRequest request);

    @GET("/api/user/check-profile")
    Call<ProfileCheckResponseDTO> checkProfileStatus();

    @POST("/api/user/logout")
    Call<Void> logout();

    // 액세스 토큰 재발급 요청
    @POST("/jwt/refresh")
    Call<LoginResponseDTO> requestTokenRefresh(@Header("Authorization-Refresh") String refreshToken);

    @GET("/api/user/")
    Call<UserResponseDTO> getUserInfo(@Header("Authorization") String accessToken);

    @DELETE("/api/user/")
    Call<Void> deleteAccount(@Header("Authorization") String accessToken);

    @GET("/api/likes/")
    Call<List<MovieSummaryResponseDTO>> getFavoriteMovies(@Header("Authorization") String accessToken);
}