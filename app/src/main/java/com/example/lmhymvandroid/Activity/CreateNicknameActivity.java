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
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.lmhymvandroid.DTO.NicknameRequest;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService; // ◀◀ AuthService 임포트
import com.example.lmhymvandroid.ToastUtil;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.io.IOException;
import java.util.Calendar;
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
    private RadioGroup radioGroupGender;

    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_nickname);

        // ◀◀ AuthService 초기화
        authService = RetrofitClient.getClient(this).create(AuthService.class);

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
        radioGroupGender = findViewById(R.id.radioGroupGender);

        findViewById(R.id.buttonCalendar).setOnClickListener(v -> {
            calendarCardView.setVisibility(calendarCardView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        findViewById(R.id.buttonSubmit).setOnClickListener(v -> {
            String nickname = nicknameEditText.getText().toString().trim();
            String selectedGender = "";
            int checkedId = radioGroupGender.getCheckedRadioButtonId();
            if (checkedId == R.id.radioMale) {
                selectedGender = "M";
            } else if (checkedId == R.id.radioFemale) {
                selectedGender = "W";
            }
            if (validateInput(nickname, selectedGender)) {
                requestUpdateUserInfo(nickname, selectedDate, selectedGender);
            }
        });
    }

    private void setupCalendarLogic() {
        calendarView.setTitleFormatter(day ->
                String.format(Locale.KOREA, "%d년 %02d월", day.getYear(), day.getMonth() + 1));
        calendarView.setOnTitleClickListener(view -> showYearMonthPicker());
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            updateDateDisplay(date.getYear(), date.getMonth(), date.getDay());
            calendarCardView.setVisibility(View.GONE);
        });
    }

    private void showYearMonthPicker() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_year_picker);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        NumberPicker yearPicker = dialog.findViewById(R.id.picker_year);
        NumberPicker monthPicker = dialog.findViewById(R.id.picker_month);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(1950);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(calendarView.getCurrentDate().getYear());

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(calendarView.getCurrentDate().getMonth() + 1);

        btnConfirm.setOnClickListener(v -> {
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

    private boolean validateInput(String nick, String gender) {
        if (nick.isEmpty()) {
            ToastUtil.show(this, "닉네임을 입력해주세요.");
            return false;
        }
        if (gender.isEmpty()) {
            ToastUtil.show(this, "성별을 선택해주세요.");
            return false;
        }
        if (selectedDate.isEmpty()) {
            ToastUtil.show(this, "생년월일을 입력해주세요.");
            return false;
        }
        return true;
    }

    private void requestUpdateUserInfo(String nickname, String birthdate, String gender) {
        Log.d("LMHYU_LOG", "초기 프로필 설정 요청: " + nickname + ", " + birthdate + ", " + gender);
        NicknameRequest request = new NicknameRequest(nickname, birthdate, gender);

        authService.createInitialProfile(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    ToastUtil.show(CreateNicknameActivity.this, "프로필이 설정되었습니다! 환영합니다.");

                    // 메인 화면으로 이동
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