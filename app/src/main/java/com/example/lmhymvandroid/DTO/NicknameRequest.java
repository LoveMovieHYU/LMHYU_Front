package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class NicknameRequest {

    @SerializedName("nickName")
    private String nickName;

    @SerializedName("birthday")
    private String birthday;

    @SerializedName("gender")
    private String gender;

    public NicknameRequest(String nickName, String birthday, String gender) {
        this.nickName = nickName;
        this.birthday = birthday;
        this.gender = gender;
    }

    public String getNickName() { return nickName; }
    public String getBirthday() { return birthday; }
    public String getGender() { return gender; }
}