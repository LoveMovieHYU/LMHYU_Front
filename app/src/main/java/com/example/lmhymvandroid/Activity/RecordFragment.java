package com.example.lmhymvandroid.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.Adapter.DiaryAdapter;
import com.example.lmhymvandroid.Adapter.EventDecorator;
import com.example.lmhymvandroid.Adapter.MySelectorDecorator;
import com.example.lmhymvandroid.DTO.Diary;
import com.example.lmhymvandroid.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;

import java.util.ArrayList;

public class RecordFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private DiaryAdapter adapter;
    private Button btnWriteDiary;
    private Button btnLogout;
    private LinearLayout layoutDiaryDetail;
    private TextView tvDetailPoster;
    private TextView tvDetailTitle;
    private TextView tvDetailInfo;

    private ArrayList<Diary> diaryList = new ArrayList<>();
    private Diary selectedDiary = null;

    private final ActivityResultLauncher<Intent> writeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String date = data.getStringExtra("date");
                    String title = data.getStringExtra("title");
                    String content = data.getStringExtra("content");
                    String emotion = data.getStringExtra("emotion");

                    Diary newDiary = new Diary(date, title, content, emotion);
                    diaryList.add(0, newDiary);
                    adapter.setDiaryList(diaryList);
                    updateCalendarDecorator();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        // 1. 뷰 연결
        calendarView = view.findViewById(R.id.calendarView);
        recyclerView = view.findViewById(R.id.recyclerViewRecent);
        btnWriteDiary = view.findViewById(R.id.btnWriteDiary);
        btnLogout = view.findViewById(R.id.btnLogout);

        layoutDiaryDetail = view.findViewById(R.id.layoutDiaryDetail);
        tvDetailPoster = view.findViewById(R.id.tvDetailPoster);
        tvDetailTitle = view.findViewById(R.id.tvDetailTitle);
        tvDetailInfo = view.findViewById(R.id.tvDetailInfo);

        // 2. 달력 디자인 설정
        calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(
                new CharSequence[]{"일", "월", "화", "수", "목", "금", "토"}
        ));
        calendarView.setTitleFormatter(day -> day.getYear() + "년 " + day.getMonth() + "월");

        // 3. 리사이클러뷰 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DiaryAdapter(diaryList);
        recyclerView.setAdapter(adapter);

        // 4. 작성 버튼 클릭
        btnWriteDiary.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WriteActivity.class);
            writeLauncher.launch(intent);
        });

        // 5. 달력 날짜 클릭 이벤트
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            showDiaryDetail(date);
            new Handler(Looper.getMainLooper()).postDelayed(() -> widget.clearSelection(), 1000);
        });

        // 6. 상세 정보창 클릭 시
        layoutDiaryDetail.setOnClickListener(v -> {
            if (selectedDiary != null) {
                Intent intent = new Intent(requireContext(), WriteActivity.class);
                intent.putExtra("date", selectedDiary.date);
                intent.putExtra("title", selectedDiary.title);
                intent.putExtra("content", selectedDiary.content);
                intent.putExtra("emotion", selectedDiary.emotion);
                startActivity(intent);
            }
        });

        // 로그아웃 버튼
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
        });

        calendarView.addDecorator(new MySelectorDecorator(requireActivity()));

        return view;
    }

    private void updateCalendarDecorator() {
        calendarView.removeDecorators();
        calendarView.addDecorator(new MySelectorDecorator(requireActivity()));

        if (!diaryList.isEmpty()) {
            ArrayList<CalendarDay> dates = new ArrayList<>();
            for (Diary diary : diaryList) {
                try {
                    String[] parts = diary.date.split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    dates.add(CalendarDay.from(year, month, day));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            calendarView.addDecorator(new EventDecorator(requireContext(), dates));
        }
        calendarView.invalidateDecorators();
    }

    private void showDiaryDetail(CalendarDay date) {
        String targetDate = String.format("%d-%02d-%02d", date.getYear(), date.getMonth(), date.getDay());
        Diary foundDiary = null;
        for (Diary d : diaryList) {
            if (d.date.equals(targetDate)) {
                foundDiary = d;
                break;
            }
        }
        this.selectedDiary = foundDiary;
        if (foundDiary != null) {
            layoutDiaryDetail.setVisibility(View.VISIBLE);
            tvDetailTitle.setText(foundDiary.title);
        } else {
            layoutDiaryDetail.setVisibility(View.GONE);
        }
    }
}