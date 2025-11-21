package com.example.lmhymvandroid.DTO;

// ◀ 400 Bad Request (유효성 검사 실패) 시 백엔드가 보낼 에러 메시지 DTO
public class ErrorResponse {
    private String newNickname;

    public String getNewNickname() { return newNickname; }
}
