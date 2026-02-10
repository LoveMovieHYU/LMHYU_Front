package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class BiorhythmResponse {

    @SerializedName("physicalIndex")
    private double physicalIndex;

    @SerializedName("emotionalIndex")
    private double emotionalIndex;

    @SerializedName("intellectualIndex")
    private double intellectualIndex;

    @SerializedName("statusMessage")
    private String statusMessage;

    public BiorhythmResponse() {
    }

    public BiorhythmResponse(double physicalIndex, double emotionalIndex, double intellectualIndex, String statusMessage) {
        this.physicalIndex = physicalIndex;
        this.emotionalIndex = emotionalIndex;
        this.intellectualIndex = intellectualIndex;
        this.statusMessage = statusMessage;
    }

    // Getters
    public double getPhysicalIndex() {
        return physicalIndex;
    }

    public double getEmotionalIndex() {
        return emotionalIndex;
    }

    public double getIntellectualIndex() {
        return intellectualIndex;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

}