package com.example.lmhymvandroid.DTO;

public class ReactionRequest {
    private String reactionType; // "LIKE" or "DISLIKE"

    public ReactionRequest(String reactionType) {
        this.reactionType = reactionType;
    }
}