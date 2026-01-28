package com.example.lmhymvandroid.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.lmhymvandroid.DTO.NicknameUpdateRequest;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateNicknameActivity extends AppCompatActivity {

    private AuthService authService;
    private EditText nicknameEditText;
    private TextView textViewBirthdate;
    private Button completeButton;
    private ImageView buttonCalendar;
    private CardView calendarCardView;
    private MaterialCalendarView calendarView;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_nickname);

        authService = RetrofitClient.getClient(this).create(AuthService.class);

        nicknameEditText = findViewById(R.id.editTextNickname);
        textViewBirthdate = findViewById(R.id.textViewBirthdate);
        buttonCalendar = findViewById(R.id.buttonCalendar);
        completeButton = findViewById(R.id.buttonSubmit);
        calendarCardView = findViewById(R.id.calendarCardView);
        calendarView = findViewById(R.id.calendarView);

        setupCalendar();

        buttonCalendar.setOnClickListener(v -> {
            calendarCardView.setVisibility(calendarCardView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        completeButton.setOnClickListener(v -> {
            String nickname = nicknameEditText.getText().toString().trim();

            if (!validateNicknameFormat(nickname)) {
                return;
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "생년월일을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            requestUpdateUserInfo(nickname, selectedDate);
        });
    }

    // 닉네임 형식 검사
    private boolean validateNicknameFormat(String nickname) {
        if (nickname.isEmpty()) {
            nicknameEditText.setError("닉네임을 입력해주세요.");
            return false;
        }
        if (nickname.length() < 1 || nickname.length() > 15) {
            nicknameEditText.setError("닉네임은 1~5자 사이여야 합니다.");
            return false;
        }
        if (!Pattern.matches("^[a-zA-Z0-9가-힣]*$", nickname)) {
            nicknameEditText.setError("특수문자는 사용할 수 없습니다.");
            return false;
        }
        return true;
    }

    private void setupCalendar() {
        calendarView.setTitleFormatter(day ->
                String.format(Locale.KOREA, "%d년 %02d월", day.getYear(), day.getMonth() + 1));

        calendarView.setOnTitleClickListener(view -> {
            Calendar c = Calendar.getInstance();
            CalendarDay current = calendarView.getCurrentDate();
            DatePickerDialog dialog = new DatePickerDialog(this, (datePicker, year, month, day) -> {
                calendarView.setCurrentDate(CalendarDay.from(year, month, 1));
            }, current.getYear(), current.getMonth(), 1);
            dialog.show();
        });

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            updateDateDisplay(date.getYear(), date.getMonth(), date.getDay());
            calendarCardView.setVisibility(View.GONE);
        });

        findViewById(R.id.btnCalendarToday).setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();
            calendarView.setCurrentDate(today);
            calendarView.setSelectedDate(today);
            updateDateDisplay(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        });

        findViewById(R.id.btnCalendarDelete).setOnClickListener(v -> {
            selectedDate = "";
            textViewBirthdate.setText("YYYY-MM-DD");
            textViewBirthdate.setTextColor(Color.parseColor("#BBBBBB"));
            calendarCardView.setVisibility(View.GONE);
        });
    }

    private void updateDateDisplay(int year, int month, int day) {
        selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
        textViewBirthdate.setText(selectedDate);
        textViewBirthdate.setTextColor(Color.BLACK);
        completeButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4A4A8A")));
    }

    private void requestUpdateUserInfo(String nickname, String birthdate) {
        Log.d("LMHYU_LOG", "서버 요청 시작: " + nickname + ", " + birthdate);

        NicknameUpdateRequest request = new NicknameUpdateRequest(nickname, birthdate);
        authService.updateNickname(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("LMHYU_LOG", "응답 코드: " + response.code());

                if (response.isSuccessful()) {
                    Log.d("LMHYU_LOG", "성공! 메인 화면 이동");
                    Intent intent = new Intent(CreateNicknameActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("LMHYU_LOG", "네트워크 에러: " + t.getMessage());
                Toast.makeText(CreateNicknameActivity.this, "네트워크 연결 상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleApiError(Response<?> response) {
        try {
            String errorJson = response.errorBody() != null ? response.errorBody().string() : "";
            Log.e("LMHYU_LOG", "에러 상세: " + errorJson);

            if (response.code() == 409) {
                nicknameEditText.setError("이미 사용 중인 닉네임입니다.");
                Toast.makeText(this, "다른 닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            } else if (response.code() == 400) {
                nicknameEditText.setError("올바르지 않은 형식입니다.");
            } else {
                Toast.makeText(this, "정보 업데이트 실패 (코드: " + response.code() + ")", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("LMHYU_LOG", "에러 바디 읽기 실패", e);
        }
    }
}