package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.content.SharedPreferences; // 추가됨
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lmhymvandroid.R;

public class EmotionSelectionActivity extends AppCompatActivity {
    private String selectedTag = null;
    private View lastSelectedView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion_selection);

        // 8개 버튼 리스너 등록 (기존과 동일)
        setupButton(R.id.btn_happy, "HAPPY");
        setupButton(R.id.btn_sad, "SAD");
        setupButton(R.id.btn_angry, "ANGRY");
        setupButton(R.id.btn_anxious, "ANXIOUS");
        setupButton(R.id.btn_tired, "TIRED");
        setupButton(R.id.btn_thinking, "THINKING");
        setupButton(R.id.btn_relaxed, "RELAXED");
        setupButton(R.id.btn_depressed, "DEPRESSED");

        findViewById(R.id.btn_submit).setOnClickListener(v -> {
            if (selectedTag == null) {
                Toast.makeText(this, "오늘의 감정을 선택해주세요!", Toast.LENGTH_SHORT).show();
            } else {
                // 서버 전송 대신 로컬 저장 함수 호출
                saveEmotionLocally(selectedTag);
            }
        });
    }

    private void setupButton(int id, String tag) {
        View view = findViewById(id);
        view.setOnClickListener(v -> {
            if (lastSelectedView != null) {
                lastSelectedView.setSelected(false);
            }
            v.setSelected(true);
            selectedTag = tag;
            lastSelectedView = v;
        });
    }

    // [핵심] 기기에 감정 저장하는 함수
    private void saveEmotionLocally(String tag) {
        // 1. 저장소 열기 (파일 이름: "UserEmotionPref")
        SharedPreferences sharedPreferences = getSharedPreferences("UserEmotionPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 2. 데이터 저장 (Key: "today_emotion", Value: 선택한 태그)
        editor.putString("today_emotion", tag);

        // 3. 오늘 날짜도 같이 저장 (나중에 하루가 지났는지 체크하기 위해 필요)
        long currentTime = System.currentTimeMillis();
        editor.putLong("emotion_saved_time", currentTime);

        // 4. 변경 사항 반영 (저장)
        editor.apply();

        Toast.makeText(this, "오늘의 감정이 기기에 저장되었습니다.", Toast.LENGTH_SHORT).show();

        // 5. 메인 화면으로 이동
        Intent intent = new Intent(EmotionSelectionActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}