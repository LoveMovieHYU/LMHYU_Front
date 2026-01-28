package com.example.lmhymvandroid.DTO;

public class NicknameUpdateRequest {
    private String newNickname;
    private String birthdate;

    public NicknameUpdateRequest(String newNickname, String birthdate) {
        this.newNickname = newNickname;
        this.birthdate = birthdate;
    }



    // Getters...
    public String getNewNickname() { return newNickname; }
    public String getBirthdate() { return birthdate; }
}