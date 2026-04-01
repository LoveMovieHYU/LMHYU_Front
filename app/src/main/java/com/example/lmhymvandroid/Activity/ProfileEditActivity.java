package com.example.lmhymvandroid.Activity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.lmhymvandroid.DTO.NicknameUpdateRequest;
import com.example.lmhymvandroid.DTO.UserResponseDTO;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.ToastUtil;
import com.example.lmhymvandroid.TokenManager;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditActivity extends AppCompatActivity {

    private AuthService authService;
    private TokenManager tokenManager;

    private EditText etNickname;
    private TextView tvBirthdate;
    private CardView calendarCardView;
    private MaterialCalendarView calendarView;

    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        tokenManager = new TokenManager(this);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        etNickname = findViewById(R.id.et_nickname);
        tvBirthdate = findViewById(R.id.tv_birthdate);
        calendarCardView = findViewById(R.id.calendarCardView);
        calendarView = findViewById(R.id.calendarView);

        setupCalendarLogic();

        View.OnClickListener toggleCalendar = v -> {
            if (calendarCardView.getVisibility() == View.VISIBLE) {
                calendarCardView.setVisibility(View.GONE);
            } else {
                calendarCardView.setVisibility(View.VISIBLE);
                if (!selectedDate.isEmpty()) {
                    try {
                        String[] parts = selectedDate.split("-");
                        calendarView.setCurrentDate(CalendarDay.from(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2])));
                        calendarView.setSelectedDate(CalendarDay.from(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2])));
                    } catch (Exception ignored) {}
                }
            }
        };

        tvBirthdate.setOnClickListener(toggleCalendar);
        findViewById(R.id.iv_calendar_icon).setOnClickListener(toggleCalendar);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());


        findViewById(R.id.btn_save).setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();
            String birthdate = tvBirthdate.getText().toString().trim();

            if (birthdate.equals("YYYY-MM-DD")) {
                birthdate = "";
            }


            if (validateInputs(nickname, birthdate)) {
                requestUpdateProfile(nickname, birthdate);
            }
        });

        loadCurrentUserInfo();
    }

    private void setupCalendarLogic() {
        calendarView.setTitleFormatter(day -> String.format(Locale.KOREA, "%d년 %02d월", day.getYear(), day.getMonth() + 1));
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

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(calendarView.getCurrentDate().getYear());

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(calendarView.getCurrentDate().getMonth() + 1);

        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            calendarView.setCurrentDate(CalendarDay.from(yearPicker.getValue(), monthPicker.getValue() - 1, 1));
            dialog.dismiss();
        });
        dialog.show();
    }

    private void updateDateDisplay(int year, int month, int day) {
        selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
        tvBirthdate.setText(selectedDate);
        tvBirthdate.setTextColor(Color.BLACK);
    }

    private void loadCurrentUserInfo() {
        String token = tokenManager.getAccessToken();
        if (token == null) return;

        authService.getUserInfo("Bearer " + token).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDTO user = response.body();
                    if (user.getNickname() != null) etNickname.setText(user.getNickname());

                    String existingBirthday = user.getBirthdate();
                    if (existingBirthday != null && !existingBirthday.isEmpty()) {
                        selectedDate = existingBirthday;
                        tvBirthdate.setText(selectedDate);
                        tvBirthdate.setTextColor(Color.BLACK);
                    }
                }
            }
            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                Log.e("PROFILE_EDIT", "정보 로드 실패: " + t.getMessage());
            }
        });
    }


    private boolean validateInputs(String nickname, String birthdate) {
        if (nickname.isEmpty() && birthdate.isEmpty()) {
            ToastUtil.show(this, "수정할 정보를 입력하거나 선택해주세요.");
            return false;
        }
        return true;
    }


    private void requestUpdateProfile(String nickname, String birthday) {


        String patchNickname = nickname.isEmpty() ? null : nickname;
        String patchBirthday = birthday.isEmpty() ? null : birthday;
        NicknameUpdateRequest request = new NicknameUpdateRequest(patchNickname, patchBirthday);

        authService.updateUserInfo(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    ToastUtil.show(ProfileEditActivity.this, "회원 정보가 성공적으로 수정되었습니다.");
                    finish();
                } else {
                    ToastUtil.show(ProfileEditActivity.this, "수정 실패 (코드: " + response.code() + ")");
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                ToastUtil.show(ProfileEditActivity.this, "네트워크 오류가 발생했습니다.");
            }
        });
    }
}