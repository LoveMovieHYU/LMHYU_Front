package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class LoginResponseDTO {
    // 2. ◀◀◀ [추가] 서버가 "userId"로 보내면 "userId", "id"로 보내면 "id"
    @SerializedName("userId") // 👈 서버가 보내는 JSON 키 이름과 동일하게 맞추세요
    private int userId;

    // ◀ SerializedName은 isNewUser, accessToken, refreshToken에도 동일하게 적용하는 것이 좋습니다.
    @SerializedName("isNewUser")
    private boolean isNewUser;

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("refreshToken")
    private String refreshToken;

    // Getters
    public int getUserId() { return userId; }
    public boolean isNewUser() { return isNewUser; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}
