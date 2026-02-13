package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lmhymvandroid.R;

public class DeleteCompleteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_complete);

        Button btnGoHome = findViewById(R.id.btn_go_home);
        btnGoHome.setOnClickListener(v -> {
            // 로그인 화면으로 이동
            Intent intent = new Intent(DeleteCompleteActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}