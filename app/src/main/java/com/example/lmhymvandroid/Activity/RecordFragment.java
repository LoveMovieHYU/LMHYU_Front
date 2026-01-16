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
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;

import java.util.ArrayList;

public class RecordFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private DiaryAdapter adapter;
    private Button btnWriteDiary;

    // 상세 내용을 보여줄 뷰들
    private LinearLayout layoutDiaryDetail;
    private TextView tvDetailPoster;
    private TextView tvDetailTitle;
    private TextView tvDetailInfo;

    // 데이터를 저장할 리스트
    private ArrayList<Diary> diaryList = new ArrayList<>();

    // 현재 선택된(화면에 보여지고 있는) 일기를 저장할 변수
    private Diary selectedDiary = null;

    // WriteActivity에서 데이터 받아오기
    private final ActivityResultLauncher<Intent> writeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    String date = data.getStringExtra("date");
                    String title = data.getStringExtra("title");
                    String content = data.getStringExtra("content");
                    String emotion = data.getStringExtra("emotion");

                    // DTO 생성
                    Diary newDiary = new Diary(date, title, content, emotion);

                    // 리스트 추가 및 갱신
                    diaryList.add(0, newDiary);
                    adapter.setDiaryList(diaryList);

                    // 달력 갱신
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

        layoutDiaryDetail = view.findViewById(R.id.layoutDiaryDetail);
        tvDetailPoster = view.findViewById(R.id.tvDetailPoster);
        tvDetailTitle = view.findViewById(R.id.tvDetailTitle);
        tvDetailInfo = view.findViewById(R.id.tvDetailInfo);

        // 2. 달력 디자인 설정 (한글 요일, 제목 포맷)
        calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(
                new CharSequence[]{"일", "월", "화", "수", "목", "금", "토"}
        ));

        calendarView.setTitleFormatter(new TitleFormatter() {
            @Override
            public CharSequence format(CalendarDay day) {
                return day.getYear() + "년 " + day.getMonth() + "월";
            }
        });

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
            // (1) 상세 내용 보여주기
            showDiaryDetail(date);

            // (2) 1초 뒤에 선택 표시(회색) 사라지게 하기
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                widget.clearSelection();
            }, 1000);
        });

        // 상세 정보창 클릭 시 -> 작성한 내용 보러 가기
        layoutDiaryDetail.setOnClickListener(v -> {
            if (selectedDiary != null) {
                Intent intent = new Intent(requireContext(), WriteActivity.class);
                // 데이터를 담아서 보냅니다.
                intent.putExtra("date", selectedDiary.date);
                intent.putExtra("title", selectedDiary.title);
                intent.putExtra("content", selectedDiary.content);
                intent.putExtra("emotion", selectedDiary.emotion);

                startActivity(intent); // 이동!
            }
        });

        // 앱 시작 시 기본 디자인(네모 박스) 적용
        calendarView.addDecorator(new MySelectorDecorator(requireActivity()));

        return view;
    }

    /**
     * 작성된 일기 날짜들에 검정색 네모 박스를 씌우고, 선택 효과를 유지하는 함수
     */
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