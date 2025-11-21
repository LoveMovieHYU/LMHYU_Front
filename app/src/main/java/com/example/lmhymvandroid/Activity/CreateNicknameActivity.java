// com/example/lmhymvandroid/Activity/CreateNicknameActivity.java
package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lmhymvandroid.DTO.ErrorResponse;
import com.example.lmhymvandroid.DTO.NicknameUpdateRequest;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateNicknameActivity extends AppCompatActivity {

    private AuthService authService;
    private int currentUserId;
    private EditText nicknameEditText;
    private Button completeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_nickname);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        currentUserId = getIntent().getIntExtra("USER_ID", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "오류: 사용자 정보 없음", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nicknameEditText = findViewById(R.id.editTextNickname);
        completeButton = findViewById(R.id.buttonSubmitNickname);

        completeButton.setOnClickListener(v -> {
            String nickname = nicknameEditText.getText().toString().trim();
            if (nickname.isEmpty()) {
                nicknameEditText.setError("닉네임을 입력하세요.");
                return;
            }
            requestUpdateNickname(nickname);
        });
    }

    private void requestUpdateNickname(String nickname) {
        NicknameUpdateRequest request = new NicknameUpdateRequest(nickname);

        Call<Void> call = authService.updateNickname(currentUserId, request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateNicknameActivity.this, "환영합니다!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateNicknameActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CreateNicknameActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleApiError(Response<?> response) {
        try {
            String errorBodyString = response.errorBody().string();
            Gson gson = new GsonBuilder().create();


            if (response.code() == 400) {

                // 1. "동일 닉네임" 오류인지 먼저 확인
                if (errorBodyString != null && errorBodyString.contains("same")) {
                    nicknameEditText.setError("현재 닉네임과 동일합니다."); //

                } else {
                    ErrorResponse errorResponse = gson.fromJson(errorBodyString, ErrorResponse.class);
                    if (errorResponse != null && errorResponse.getNewNickname() != null) {
                        nicknameEditText.setError(errorResponse.getNewNickname());
                    } else {
                        nicknameEditText.setError("닉네임 형식이 올바르지 않습니다.");
                    }
                }

            } else if (response.code() == 409) { // 닉네임 중복
                nicknameEditText.setError("이미 사용 중인 닉네임입니다.");

            } else {
                Log.e("ApiError", "Error Code: " + response.code() + ", Message: " + errorBodyString);
                Toast.makeText(this, "오류: " + response.message(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "오류 응답을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}