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

import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.DTO.NicknameUpdateRequest;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.Service.MovieService;
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
    private Button completeButton;
    private ImageView buttonCalendar;
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
//        authService = RetrofitClient.getClient(this).create(AuthService.class);
//        initViews();
        setupCalendarLogic();
        String missingField = getIntent().getStringExtra("MISSING_FIELD");
        handleMissingField(missingField);
    }

    private void handleMissingField(String field) {
        if (field == null) return;

        if (field.contains("NICKNAME")) {
            nicknameEditText.requestFocus();
            Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else if (field.contains("BIRTHDATE")) {
            Toast.makeText(this, "생년월일을 선택해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        nicknameEditText = findViewById(R.id.editTextNickname);
        textViewBirthdate = findViewById(R.id.textViewBirthdate);
        buttonCalendar = findViewById(R.id.buttonCalendar);
        completeButton = findViewById(R.id.buttonSubmit);
        calendarCardView = findViewById(R.id.calendarCardView);
        calendarView = findViewById(R.id.calendarView);

        buttonCalendar.setOnClickListener(v -> {
            int visibility = (calendarCardView.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE;
            calendarCardView.setVisibility(visibility);
        });

        completeButton.setOnClickListener(v -> {
            String nickname = nicknameEditText.getText().toString().trim();
            if (!validateInput(nickname)) return;
            requestUpdateUserInfo(nickname, selectedDate);
        });
    }

    private void setupCalendarLogic() {
        calendarView.setTitleFormatter(day ->
                String.format(Locale.KOREA, "%d년 %02d월", day.getYear(), day.getMonth() + 1));

        calendarView.setOnTitleClickListener(view -> {
            Calendar c = Calendar.getInstance();
            CalendarDay current = calendarView.getCurrentDate();
            DatePickerDialog dialog = new DatePickerDialog(this, (datePicker, year, month, day) -> {
                calendarView.setCurrentDate(CalendarDay.from(year, month, 1));
            }, current.getYear(), current.getMonth(), 1);
            dialog.setTitle("이동할 연도와 월을 선택하세요");
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
        completeButton.setTextColor(Color.WHITE);
    }

    private boolean validateInput(String nickname) {
        if (nickname.isEmpty()) {
            nicknameEditText.setError("닉네임을 입력해주세요.");
            return false;
        }
        if (nickname.length() < 2) {
            nicknameEditText.setError("닉네임은 최소 2자 이상이어야 합니다.");
            return false;
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "생년월일을 선택해주세요.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CreateNicknameActivity.this, "환영합니다! 추천을 시작합니다.", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(CreateNicknameActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleApiError(Response<?> response) {
        try {
            String errorJson = response.errorBody() != null ? response.errorBody().string() : "";
            Log.e("LMHYU_LOG", "에러 상세: " + errorJson);

            if (response.code() == 409) {
                nicknameEditText.setError("이미 사용 중인 닉네임입니다.");
            } else if (response.code() == 400) {
                Toast.makeText(this, "입력 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "오류가 발생했습니다 (코드: " + response.code() + ")", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}