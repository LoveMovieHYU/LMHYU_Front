package com.example.lmhymvandroid.Activity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private String originalNickname = "";
    private String originalBirthdate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        applyImeInsets();

        tokenManager = new TokenManager(this);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        etNickname = findViewById(R.id.et_nickname);
        tvBirthdate = findViewById(R.id.tv_birthdate);
        calendarCardView = findViewById(R.id.calendarCardView);
        calendarView = findViewById(R.id.calendarView);

        setupCalendarLogic();
        // (applyImeInsets 는 onCreate 에서 호출)

        etNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etNickname.setTextColor(Color.parseColor("#000000"));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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

    /**
     * 엣지 투 엣지로 그리고, 키보드(IME)/하단 시스템 바 인셋을 스크롤 루트의 하단 패딩으로 반영해
     * 키보드가 입력칸/버튼을 가리지 않게 한다. (상단은 레이아웃의 기존 여백을 유지)
     */
    private void applyImeInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.profile_edit_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                    Math.max(bars.bottom, ime.bottom));
            return insets;
        });
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
        tvBirthdate.setTextColor(Color.parseColor("#000000"));
    }

    private void loadCurrentUserInfo() {
        String token = tokenManager.getAccessToken();
        if (token == null) return;

        // 인증 헤더는 AuthInterceptor 가 자동 부착한다
        authService.getUserInfo().enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDTO user = response.body();

                    if (user.getNickname() != null) {
                        originalNickname = user.getNickname();
                        etNickname.setText(originalNickname);
                        etNickname.setTextColor(Color.parseColor("#999999"));
                    }

                    String existingBirthday = user.getBirthdate();
                    if (existingBirthday != null && !existingBirthday.isEmpty()) {
                        originalBirthdate = existingBirthday;
                        selectedDate = existingBirthday;
                        tvBirthdate.setText(selectedDate);
                        tvBirthdate.setTextColor(Color.parseColor("#999999"));
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
        boolean isNicknameChanged = !nickname.equals(originalNickname) && !nickname.isEmpty();
        boolean isBirthdateChanged = !birthdate.equals(originalBirthdate) && !birthdate.isEmpty();

        if (!isNicknameChanged && !isBirthdateChanged) {
            ToastUtil.show(this, "수정된 정보가 없습니다.");
            return false;
        }
        return true;
    }


    private void requestUpdateProfile(String nickname, String birthday) {
        String patchNickname = nickname.equals(originalNickname) || nickname.isEmpty() ? null : nickname;
        String patchBirthday = birthday.equals(originalBirthdate) || birthday.isEmpty() ? null : birthday;

        NicknameUpdateRequest request = new NicknameUpdateRequest(patchNickname, patchBirthday);

        authService.updateUserInfo(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    ToastUtil.show(ProfileEditActivity.this, "회원 정보가 성공적으로 수정되었습니다.");
                    setResult(RESULT_OK);
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