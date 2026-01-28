package com.example.lmhymvandroid.DTO;

public class NicknameUpdateRequest {
    private String newNickname;
    private String birthday;

    public NicknameUpdateRequest(String newNickname, String birthday) {
        this.newNickname = newNickname;
        this.birthday = birthday;
    }



    // Getters...
    public String getNewNickname() { return newNickname; }
    public String getBirthday() { return birthday; }
}