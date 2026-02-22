package com.example.lmhymvandroid.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.DTO.NicknameUpdateRequest;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.Service.MovieService;
import com.example.lmhymvandroid.ToastUtil;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateNicknameActivity extends AppCompatActivity {

    private AuthService authService;
    private EditText nicknameEditText;
    private TextView textViewBirthdate;
    private CardView calendarCardView;
    private MaterialCalendarView calendarView;
    private String selectedDate = "";
    private MovieService movieService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_nickname);

        movieService = RetrofitClient.getClient(this).create(MovieService.class);
        initViews();
        setupCalendarLogic();
        String missing = getIntent().getStringExtra("MISSING_FIELD");
        if (missing != null) Log.d("HYMV_DEBUG", "누락 필드: " + missing);
    }

    private void initViews() {
        nicknameEditText = findViewById(R.id.editTextNickname);
        textViewBirthdate = findViewById(R.id.textViewBirthdate);
        calendarCardView = findViewById(R.id.calendarCardView);
        calendarView = findViewById(R.id.calendarView);

        // 달력 아이콘 클릭 시 토글
        findViewById(R.id.buttonCalendar).setOnClickListener(v -> {
            calendarCardView.setVisibility(calendarCardView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        // 시작하기 버튼 클릭
        findViewById(R.id.buttonSubmit).setOnClickListener(v -> {
            String nickname = nicknameEditText.getText().toString().trim();
            if (validateInput(nickname)) {
                requestUpdateUserInfo(nickname, selectedDate);
            }
        });
    }

    private void setupCalendarLogic() {
        // 1. 타이틀 포맷 설정
        calendarView.setTitleFormatter(day ->
                String.format(Locale.KOREA, "%d년 %02d월", day.getYear(), day.getMonth() + 1));

        // 2. 타이틀 클릭 시 연도 점프 다이얼로그 표시
        calendarView.setOnTitleClickListener(view -> showYearMonthPicker());

        // 3. 날짜 선택 시 텍스트 업데이트 및 달력 닫기
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            updateDateDisplay(date.getYear(), date.getMonth(), date.getDay());
            calendarCardView.setVisibility(View.GONE);
        });

        // '오늘', '삭제' 버튼 로직
        findViewById(R.id.btnCalendarToday).setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();
            updateDateDisplay(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
        });

        findViewById(R.id.btnCalendarDelete).setOnClickListener(v -> {
            selectedDate = "";
            textViewBirthdate.setText("YYYY-MM-DD");
            textViewBirthdate.setTextColor(Color.parseColor("#BBBBBB"));
            calendarCardView.setVisibility(View.GONE);
        });
    }

    private void showYearMonthPicker() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_year_picker);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        NumberPicker yearPicker = dialog.findViewById(R.id.picker_year);
        NumberPicker monthPicker = dialog.findViewById(R.id.picker_month);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        // 연도 범위 설정 (1950 ~ 2026)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(1950);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(calendarView.getCurrentDate().getYear());

        // 월 범위 설정 (1 ~ 12)
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(calendarView.getCurrentDate().getMonth() + 1);

        btnConfirm.setOnClickListener(v -> {
            // 선택한 연도/월로 달력 이동
            calendarView.setCurrentDate(CalendarDay.from(yearPicker.getValue(), monthPicker.getValue() - 1, 1));
            dialog.dismiss();
        });
        dialog.show();
    }

    private void updateDateDisplay(int year, int month, int day) {
        selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
        textViewBirthdate.setText(selectedDate);
        textViewBirthdate.setTextColor(Color.BLACK);
    }

    private boolean validateInput(String nick) {
        if (nick.isEmpty() || selectedDate.isEmpty()) {
            ToastUtil.show(this, "닉네임과 생일을 모두 입력해주세요.");
            return false;
        }
        return true;
    }

    private void requestUpdateUserInfo(String nickname, String birthdate) {
        Log.d("LMHYU_LOG", "API 요청 시작: " + nickname + ", " + birthdate);
        NicknameUpdateRequest request = new NicknameUpdateRequest(nickname, birthdate);

        movieService.postFirstRecommend(request).enqueue(new Callback<List<MovieItem>>() {
            @Override
            public void onResponse(Call<List<MovieItem>> call, Response<List<MovieItem>> response) {
                if (response.isSuccessful()) {
                    ToastUtil.show(CreateNicknameActivity.this, "환영합니다! 추천을 시작합니다.");

                    Intent intent = new Intent(CreateNicknameActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    handleApiError(response);
                }
            }
            @Override
            public void onFailure(Call<List<MovieItem>> call, Throwable t) {
                Log.e("LMHYU_LOG", "네트워크 에러: " + t.getMessage());
                ToastUtil.show(CreateNicknameActivity.this, "서버 연결 실패");
            }
        });
    }

    private void handleApiError(Response<?> response) {
        try {
            String errorJson = response.errorBody() != null ? response.errorBody().string() : "";
            Log.e("LMHYU_LOG", "에러 상세: " + errorJson);

            if (response.code() == 409) {
                ToastUtil.show(CreateNicknameActivity.this, "이미 사용 중인 닉네임입니다.");
            } else if (response.code() == 400) {
                ToastUtil.show(this, "입력 형식이 올바르지 않습니다.");
            } else {
                ToastUtil.show(this, "오류가 발생했습니다 (코드: " + response.code() + ")");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}