package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class UserResponseDTO {

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("birthday")
    private String birthday;

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    // Getter & Setter
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBirthdate() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}
}