package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lmhymvandroid.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WriteActivity extends AppCompatActivity {

    private String selectedEmotion = "Happy";
    private TextView[] emotionViews;

    private TextView tvDate;
    private EditText etTitle;
    private EditText etContent;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        tvDate = findViewById(R.id.tvDate);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        ratingBar = findViewById(R.id.ratingBar);
        Button btnSave = findViewById(R.id.btnSave);
        ImageButton btnBack = findViewById(R.id.btnBack);

        emotionViews = new TextView[]{
                findViewById(R.id.emoji1), findViewById(R.id.emoji2),
                findViewById(R.id.emoji3), findViewById(R.id.emoji4),
                findViewById(R.id.emoji5)
        };

        btnBack.setOnClickListener(v -> finish());

        // 기본 날짜 설정
        String today = new SimpleDateFormat("yyyy년 M월 d일", Locale.getDefault()).format(new Date());
        String intentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvDate.setText(today);

        // 감정 클릭 이벤트
        View.OnClickListener emotionListener = v -> {
            for (TextView tv : emotionViews) {
                tv.setBackgroundResource(R.drawable.border_box);
            }
            v.setBackgroundColor(Color.LTGRAY);

            TextView clicked = (TextView) v;
            if(clicked.getId() == R.id.emoji1) selectedEmotion = "Happy";
            else if(clicked.getId() == R.id.emoji2) selectedEmotion = "Sad";
            else if(clicked.getId() == R.id.emoji3) selectedEmotion = "Angry";
            else if(clicked.getId() == R.id.emoji4) selectedEmotion = "Scared";
            else selectedEmotion = "Sleepy";
        };

        for (TextView tv : emotionViews) {
            tv.setOnClickListener(emotionListener);
        }

// 일기 데이터가 전달되었다면 화면에 채워넣기 (보기 모드 & 수정 불가 처리)
        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.hasExtra("title")) {

            // 1. 저장 버튼 숨기기 (보기 모드니까)
            btnSave.setVisibility(View.GONE);

            // 2. 내용 채우기
            String dateStr = receivedIntent.getStringExtra("date");
            tvDate.setText(dateStr);
            etTitle.setText(receivedIntent.getStringExtra("title"));
            etContent.setText(receivedIntent.getStringExtra("content"));

            // 3. 입력창 수정 못하게 막기 (읽기 전용)
            etTitle.setFocusable(false);
            etTitle.setClickable(false);

            etContent.setFocusable(false);
            etContent.setClickable(false);

            // 4. 별점 수정 못하게 막기 (별이 보여지기만 함)
            ratingBar.setIsIndicator(true);

            // 5. 감정 이모티콘 선택 못하게 막기
            for (TextView tv : emotionViews) {
                tv.setClickable(false); // 클릭 방지
            }

            // 6. 감정 복구 및 하이라이트 표시
            String receivedEmotion = receivedIntent.getStringExtra("emotion");
            if(receivedEmotion != null) {
                // 초기화
                for (TextView tv : emotionViews) tv.setBackgroundResource(R.drawable.border_box);

                int index = 0;
                switch (receivedEmotion) {
                    case "Happy": index = 0; break;
                    case "Sad": index = 1; break;
                    case "Angry": index = 2; break;
                    case "Scared": index = 3; break;
                    case "Sleepy": index = 4; break;
                }
                // 선택된 감정만 회색 배경 + 불투명하게 잘 보이도록 유지
                emotionViews[index].setBackgroundColor(Color.LTGRAY);
            }
        }
// =========================================================
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String content = etContent.getText().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "제목을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("date", intentDate);
            resultIntent.putExtra("title", title);
            resultIntent.putExtra("content", content);
            resultIntent.putExtra("emotion", selectedEmotion);

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}