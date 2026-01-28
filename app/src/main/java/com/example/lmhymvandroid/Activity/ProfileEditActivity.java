package com.example.lmhymvandroid.Activity;

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
import com.example.lmhymvandroid.TokenManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditActivity extends AppCompatActivity {

    private AuthService authService;
    private TokenManager tokenManager;
    private long currentUserId;
    private EditText nicknameEditText;
    private Button saveButton; //

    private static final Pattern NICKNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9가-힣]*$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        authService = RetrofitClient.getClient(this).create(AuthService.class);
        tokenManager = new TokenManager(this);

        currentUserId = tokenManager.getUserId();
        if (currentUserId == -1) {
            Toast.makeText(this, "오류: 로그인 정보 없음", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nicknameEditText = findViewById(R.id.editTextNickname); // ◀ xml에 필요
        saveButton = findViewById(R.id.buttonSaveChanges); // ◀ xml에 필요


        saveButton.setOnClickListener(v -> {
            String nickname = nicknameEditText.getText().toString().trim();

            if (!validateNickname(nickname)) {
                return;
            }

            requestUpdateNickname(nickname);
        });
    }


    private boolean validateNickname(String nickname) {

        if (nickname.isEmpty()) {
            nicknameEditText.setError("형식에 맞지 않습니다.");
            return false;
        }


        if (nickname.length() < 3 || nickname.length() > 13) {
            nicknameEditText.setError("닉네임은 최소3자리부터 최대 13자리짜기만 설정가능합니다.");
            return false;
        }


        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            nicknameEditText.setError("형식에 맞지 않습니다.");
            return false;
        }


        return true;
    }

    private void requestUpdateNickname(String nickname) {
        NicknameUpdateRequest request = new NicknameUpdateRequest(nickname, null);
        Call<Void> call = authService.updateNickname(request);;
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileEditActivity.this, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProfileEditActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleApiError(Response<?> response) {
        try {
            String errorBodyString = response.errorBody().string();
            Gson gson = new GsonBuilder().create();


            if (response.code() == 400) { // 유효성 검사 또는 동일 닉네임
                if (errorBodyString != null && errorBodyString.contains("same")) {
                    nicknameEditText.setError("현재 닉네임과 동일합니다.");
                } else {
                    ErrorResponse errorResponse = gson.fromJson(errorBodyString, ErrorResponse.class);
                    if (errorResponse != null && errorResponse.getNewNickname() != null) {
                        nicknameEditText.setError(errorResponse.getNewNickname());
                    } else {
                        nicknameEditText.setError("닉네임 형식이 올바르지 않습니다.");
                    }
                }
            } else if (response.code() == 409) {
                nicknameEditText.setError("이미 사용 중인 닉네임입니다.");
            } else if (response.code() == 403) { // ◀◀◀ 403 (권한 없음)
                Log.e("ApiError", "Error Code: 403. ID 불일치. URL ID: " + currentUserId);
                Toast.makeText(this, "오류: 사용자 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("ApiError", "Error Code: " + response.code() + ", Message: " + errorBodyString);
                Toast.makeText(this, "오류: " + response.message(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "오류 응답을 처리할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}