package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class NicknameUpdateRequest {


    @SerializedName("nickName")
    private String nickName;
    @SerializedName("birthday")
    private String birthday;

    public NicknameUpdateRequest(String nickName, String birthday) {
        this.nickName = nickName;
        this.birthday = birthday;
    }


    public String getNickName() { return nickName; }
    public String getBirthday() { return birthday; }
}