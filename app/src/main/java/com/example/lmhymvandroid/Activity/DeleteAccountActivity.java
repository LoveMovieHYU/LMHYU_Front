package com.example.lmhymvandroid.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.ToastUtil;
import com.example.lmhymvandroid.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteAccountActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private EditText etEtcReason;
    private Button btnNext;
    private AuthService authService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        // 1. 초기화
        tokenManager = new TokenManager(this);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        radioGroup = findViewById(R.id.radio_group_reasons);
        etEtcReason = findViewById(R.id.et_etc_reason);
        btnNext = findViewById(R.id.btn_next);

        // 뒤로가기 버튼
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 2. 라디오 버튼 리스너
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // 버튼 보이게 하기
            btnNext.setVisibility(View.VISIBLE);

            // '기타' 선택 시 입력창 보이기
            if (checkedId == R.id.rb_reason_etc) {
                etEtcReason.setVisibility(View.VISIBLE);
            } else {
                etEtcReason.setVisibility(View.GONE);
            }
        });

        // 3. '탈퇴 계속 진행하기' 버튼 클릭
        btnNext.setOnClickListener(v -> showConfirmDialog());
    }

    // --- 탈퇴 ---
    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirm, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // 다이얼로그 내부 버튼 연결
        Button btnCancel = view.findViewById(R.id.btn_dialog_cancel);
        Button btnConfirm = view.findViewById(R.id.btn_dialog_confirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            requestDeleteAccount(); // 실제 삭제 요청
        });

        dialog.show();
    }

    // --- 회원 탈퇴 요청 ---
    private void requestDeleteAccount() {
        String token = tokenManager.getAccessToken();
        if (token == null) {
            ToastUtil.show(this, "로그인 정보가 유효하지 않습니다.");
            return;
        }

        // 인증 헤더는 AuthInterceptor 가 자동 부착한다
        authService.deleteAccount().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // 성공: 로컬 데이터 삭제 및 완료 화면 이동
                    handleDeleteSuccess();
                } else {
                    Log.e("DeleteAccount", "탈퇴 실패 Code: " + response.code());
                    ToastUtil.show(DeleteAccountActivity.this, "탈퇴 처리에 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DeleteAccount",  "Network Error: " + t.getMessage());
                ToastUtil.show(DeleteAccountActivity.this, "네트워크 오류가 발생했습니다.");
            }
        });
    }

    // ---데이터 삭제 및 화면 이동 ---
    private void handleDeleteSuccess() {
        // 1. 토큰 및 저장된 정보 삭제
        tokenManager.clearTokens();
        SharedPreferences sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        sp.edit().clear().apply();

        // 2. 완료 화면으로 이동
        Intent intent = new Intent(DeleteAccountActivity.this, DeleteCompleteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}