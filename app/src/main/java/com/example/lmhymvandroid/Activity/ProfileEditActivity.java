package com.example.lmhymvandroid.Activity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

        // 1. 초기화
        tokenManager = new TokenManager(this);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        // XML ID와 연결
        etNickname = findViewById(R.id.et_nickname);
        tvBirthdate = findViewById(R.id.tv_birthdate);
        calendarCardView = findViewById(R.id.calendarCardView);
        calendarView = findViewById(R.id.calendarView);

        // 2. 달력 로직 설정 (타이틀 클릭, 날짜 선택 등)
        setupCalendarLogic();

        // 3. 생년월일 클릭 시 달력 열기/닫기 (토글)
        View.OnClickListener toggleCalendar = v -> {
            if (calendarCardView.getVisibility() == View.VISIBLE) {
                calendarCardView.setVisibility(View.GONE);
            } else {
                calendarCardView.setVisibility(View.VISIBLE);

                // 이미 입력된 날짜가 있다면 달력을 그 날짜로 이동
                if (!selectedDate.isEmpty()) {
                    try {
                        String[] parts = selectedDate.split("-");
                        int y = Integer.parseInt(parts[0]);
                        int m = Integer.parseInt(parts[1]);
                        int d = Integer.parseInt(parts[2]);

                        // 달력 이동 및 선택 표시
                        calendarView.setCurrentDate(CalendarDay.from(y, m - 1, d));
                        calendarView.setSelectedDate(CalendarDay.from(y, m - 1, d));
                    } catch (Exception e) {
                        // 날짜 포맷 에러 시 무시 (현재 날짜 유지)
                    }
                }
            }
        };

        // 텍스트뷰와 달력 아이콘 모두에 클릭 리스너 연결
        tvBirthdate.setOnClickListener(toggleCalendar);
        findViewById(R.id.iv_calendar_icon).setOnClickListener(toggleCalendar);

        // 4. 상단 뒤로가기 버튼
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 5. 저장하기 버튼
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();
            if (validateInputs(nickname, selectedDate)) {
                requestUpdateProfile(nickname, selectedDate);
            }
        });

        // 6. 기존 정보 불러오기
        loadCurrentUserInfo();
    }

    // --- 달력 동작 설정 ---
    private void setupCalendarLogic() {
        // 1) 타이틀 포맷 설정 (예: 2026년 02월)
        calendarView.setTitleFormatter(day ->
                String.format(Locale.KOREA, "%d년 %02d월", day.getYear(), day.getMonth() + 1));

        // 2) 타이틀 클릭 시 '연도/월 선택 다이얼로그' 띄우기
        calendarView.setOnTitleClickListener(view -> showYearMonthPicker());

        // 3) 날짜 선택 시 텍스트 갱신하고 달력 닫기
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            updateDateDisplay(date.getYear(), date.getMonth(), date.getDay());
            calendarCardView.setVisibility(View.GONE);
        });

        // 4) [오늘] 버튼 기능
        findViewById(R.id.btnCalendarToday).setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();
            // 오늘 날짜로 텍스트 갱신
            updateDateDisplay(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
            // 달력 닫기
            calendarCardView.setVisibility(View.GONE);
        });

        // 5) [삭제] 버튼 기능
        findViewById(R.id.btnCalendarDelete).setOnClickListener(v -> {
            selectedDate = "";
            tvBirthdate.setText("");
            tvBirthdate.setHint("YYYY-MM-DD");
            // 달력 선택 해제 및 닫기
            calendarView.clearSelection();
            calendarCardView.setVisibility(View.GONE);
        });
    }

    // --- 연도/월 선택 커스텀 다이얼로그 ---
    private void showYearMonthPicker() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_year_picker); // dialog_year_picker.xml 사용

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        NumberPicker yearPicker = dialog.findViewById(R.id.picker_year);
        NumberPicker monthPicker = dialog.findViewById(R.id.picker_month);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        // 연도 범위 설정 (1900 ~ 현재)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(calendarView.getCurrentDate().getYear());

        // 월 범위 설정
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(calendarView.getCurrentDate().getMonth() + 1);

        btnConfirm.setOnClickListener(v -> {
            // 선택한 연도/월의 1일로 이동
            calendarView.setCurrentDate(CalendarDay.from(yearPicker.getValue(), monthPicker.getValue() - 1, 1));
            dialog.dismiss();
        });

        dialog.show();
    }

    // 날짜 포맷팅 및 화면 표시 (YYYY-MM-DD)
    private void updateDateDisplay(int year, int month, int day) {
        selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
        tvBirthdate.setText(selectedDate);
        tvBirthdate.setTextColor(Color.BLACK);
    }

    // --- API 통신 ---
    private void loadCurrentUserInfo() {
        String token = tokenManager.getAccessToken();
        if (token == null) return;

        authService.getUserInfo("Bearer " + token).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDTO user = response.body();
                    etNickname.setText(user.getNickname());

                    if (user.getBirthdate() != null && !user.getBirthdate().isEmpty()) {
                        selectedDate = user.getBirthdate();
                        tvBirthdate.setText(selectedDate);
                        tvBirthdate.setTextColor(Color.BLACK);
                    }
                }
            }
            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                ToastUtil.show(ProfileEditActivity.this, "정보를 불러오지 못했습니다.");
            }
        });
    }

    private boolean validateInputs(String nickname, String birthdate) {
        if (nickname.isEmpty()) {
            ToastUtil.show(this, "닉네임을 입력해주세요.");
            return false;
        }
        if (birthdate.isEmpty()) {
            ToastUtil.show(this, "생년월일을 선택해주세요.");
            return false;
        }
        return true;
    }

    private void requestUpdateProfile(String nickname, String birthdate) {
        // 토큰 가져오기
        String token = tokenManager.getAccessToken();
        NicknameUpdateRequest request = new NicknameUpdateRequest(nickname, birthdate);

        // API 호출
        authService.updateUserInfo(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    ToastUtil.show(ProfileEditActivity.this, "회원 정보가 수정되었습니다.");
                    finish(); // 수정 완료 후 액티비티 종료
                } else {
                    ToastUtil.show(ProfileEditActivity.this, "수정 실패");
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                ToastUtil.show(ProfileEditActivity.this, "네트워크 오류");
            }
        });
    }
}