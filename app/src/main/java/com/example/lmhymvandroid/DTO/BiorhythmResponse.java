package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class BiorhythmResponse {

    @SerializedName("physicalIndex")
    private int physicalIndex;

    @SerializedName("emotionalIndex")
    private int emotionalIndex;

    @SerializedName("intellectualIndex")
    private int intellectualIndex;

    @SerializedName("statusMessage")
    private String statusMessage;

    // 생성자 (필요 시 사용)
    public BiorhythmResponse(int physicalIndex, int emotionalIndex, int intellectualIndex, String statusMessage) {
        this.physicalIndex = physicalIndex;
        this.emotionalIndex = emotionalIndex;
        this.intellectualIndex = intellectualIndex;
        this.statusMessage = statusMessage;
    }

    // Getters
    public int getPhysicalIndex() {
        return physicalIndex;
    }

    public int getEmotionalIndex() {
        return emotionalIndex;
    }

    public int getIntellectualIndex() {
        return intellectualIndex;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

}