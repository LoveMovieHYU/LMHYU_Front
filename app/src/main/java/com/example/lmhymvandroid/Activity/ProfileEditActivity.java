package com.example.lmhymvandroid.Activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lmhymvandroid.DTO.NicknameUpdateRequest;
import com.example.lmhymvandroid.DTO.UserResponseDTO; // 기존에 만든 UserResponseDTO 사용
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.TokenManager;

import java.util.Calendar;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditActivity extends AppCompatActivity {

    private AuthService authService;
    private TokenManager tokenManager;

    private EditText etNickname;
    private TextView etBirthdate; // DatePicker를 띄우기 위해 TextView로 사용
    private Button btnSave;
    private ImageView btnBack;

    // 정규식 (영문, 숫자, 한글)
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9가-힣]*$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // 1. 초기화
        tokenManager = new TokenManager(this);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        etNickname = findViewById(R.id.et_nickname);
        etBirthdate = findViewById(R.id.et_birthdate);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);

        // 2. 현재 사용자 정보 불러오기 (화면에 미리 보여주기 위함)
        loadCurrentUserInfo();

        // 3. 생년월일 클릭 시 달력 띄우기
        etBirthdate.setOnClickListener(v -> showDatePicker());

        // 4. 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish());

        // 5. 저장하기 버튼
        btnSave.setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();
            String birthdate = etBirthdate.getText().toString().trim();

            if (validateInputs(nickname, birthdate)) {
                requestUpdateProfile(nickname, birthdate);
            }
        });
    }

    // --- [기능] 현재 정보 로드 ---
    private void loadCurrentUserInfo() {
        String token = tokenManager.getAccessToken();
        if (token == null) return;

        Call<UserResponseDTO> call = authService.getUserInfo("Bearer " + token);
        call.enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDTO user = response.body();

                    // 닉네임 세팅
                    if (user.getNickname() != null) {
                        etNickname.setText(user.getNickname());
                    }
                    // 생년월일 세팅 (서버에서 YYYY-MM-DD 형식으로 온다고 가정)
                    if (user.getBirthdate() != null) {
                        etBirthdate.setText(user.getBirthdate());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                Log.e("ProfileEdit", "정보 로드 실패: " + t.getMessage());
            }
        });
    }

    // --- [기능] 달력 다이얼로그 ---
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // 월은 0부터 시작하므로 +1 필요. 포맷: YYYY-MM-DD
                    String selectedDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    etBirthdate.setText(selectedDate);
                }, year, month, day);

        // 오늘 이후 날짜 선택 방지 (선택 사항)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    // --- [기능] 유효성 검사 ---
    private boolean validateInputs(String nickname, String birthdate) {
        if (nickname.isEmpty()) {
            Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (nickname.length() < 2 || nickname.length() > 10) { // 길이 제한 수정
            Toast.makeText(this, "닉네임은 2~10자로 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            Toast.makeText(this, "닉네임에 특수문자는 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (birthdate.isEmpty()) {
            Toast.makeText(this, "생년월일을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // --- [기능] 정보 업데이트 요청 (PUT) ---
    private void requestUpdateProfile(String nickname, String birthdate) {
        // DTO에 닉네임과 생년월일을 모두 담아서 보냄
        NicknameUpdateRequest request = new NicknameUpdateRequest(nickname, birthdate);

        Call<Void> call = authService.updateUserInfo(request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileEditActivity.this, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    finish(); // 수정 완료 후 화면 닫기
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProfileEditActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- [기능] 에러 처리 ---
    private void handleApiError(Response<?> response) {
        if (response.code() == 409) {
            Toast.makeText(this, "이미 사용 중인 닉네임입니다.", Toast.LENGTH_SHORT).show();
        } else if (response.code() == 400) {
            Toast.makeText(this, "입력 형식을 확인해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "서버 오류: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }
}