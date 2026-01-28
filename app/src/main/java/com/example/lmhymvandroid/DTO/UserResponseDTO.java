package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class UserResponseDTO {

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("birthdate")
    private String birthdate;

    @SerializedName("id")
    private int id;

    // Getter & Setter
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}