package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class ProfileCheckResponseDTO {
    @SerializedName("checked")
    private boolean checked;

    @SerializedName("missingField")
    private String missingField;

    public boolean isChecked() {
        return checked;
    }

    public String getMissingField() {
        return missingField;
    }
}