package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class LoginResponseDTO {
    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("refreshToken")
    private String refreshToken;

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}
