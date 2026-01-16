package com.example.lmhymvandroid.DTO;

import java.io.Serializable;

public class Diary implements Serializable {
    public String date;      // "2026-01-03" (ID 역할)
    public String title;
    public String content;
    public String emotion;   // "Happy", "Sad" 등

    public Diary(String date, String title, String content, String emotion) {
        this.date = date;
        this.title = title;
        this.content = content;
        this.emotion = emotion;
    }

}