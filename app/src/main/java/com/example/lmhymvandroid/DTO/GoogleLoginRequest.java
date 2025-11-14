package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class GoogleLoginRequest {
    @SerializedName("idToken")
    String idToken;

    @SerializedName("accessToken")
    String accessToken; // 1. accessToken 필드 추가


    public GoogleLoginRequest(String idToken, String accessToken) {
        this.idToken = idToken;
        this.accessToken = accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}