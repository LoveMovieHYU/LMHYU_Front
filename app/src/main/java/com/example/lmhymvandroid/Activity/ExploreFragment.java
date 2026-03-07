package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.Adapter.MovieClickAdapter;
import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.MovieService;
import com.example.lmhymvandroid.ToastUtil;
import com.google.android.material.slider.Slider;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreFragment extends Fragment {

    // View 변수 선언
    private Slider sliderEmotion, sliderIntellect, sliderPhysical;
    private Button btnSettingComplete;
    private ImageView btnSearchIcon;
    private RecyclerView rvExploreMovieList;

    // 어댑터 및 통신 서비스
    private MovieClickAdapter adapter;
    private MovieService movieService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 뷰 초기화
        initViews(view);

        // 2. Retrofit 서비스 생성
        if (getContext() != null) {
            movieService = RetrofitClient.getClient(getContext()).create(MovieService.class);
        }

        // 3. 버튼 클릭 리스너 등록
        setupListeners();
    }

    private void initViews(View view) {
        // 슬라이더 연결
        sliderEmotion = view.findViewById(R.id.slider_emotion);     // 감정 (Pink)
        sliderIntellect = view.findViewById(R.id.slider_intellect); // 지성 (Blue)
        sliderPhysical = view.findViewById(R.id.slider_physical);   // 신체 (Green)

        // 버튼 연결
        btnSettingComplete = view.findViewById(R.id.btn_setting_complete);
        btnSearchIcon = view.findViewById(R.id.btn_search_icon);

        // 리사이클러뷰 설정
        rvExploreMovieList = view.findViewById(R.id.rv_explore_movie_list);
        rvExploreMovieList.setLayoutManager(new LinearLayoutManager(getContext()));

        // 어댑터 생성 (클릭 시 이벤트 처리)
        adapter = new MovieClickAdapter(getContext(), new MovieClickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieItem movie) {
                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra("movie_data", movie); // 영화 데이터를 통째로 넘겨줌
                startActivity(intent);
            }
        });
        rvExploreMovieList.setAdapter(adapter);
    }

    private void setupListeners() {
        // [1] 설정 완료 버튼 클릭 -> API 호출
        btnSettingComplete.setOnClickListener(v -> {
            // 슬라이더 값 가져오기 (float 타입)
            float emotionVal = sliderEmotion.getValue();
            float intellectVal = sliderIntellect.getValue();
            float physicalVal = sliderPhysical.getValue();

            // 로그 확인용
            Log.d("EXPLORE_LOG", "요청 값 -> 감정:" + emotionVal + ", 지성:" + intellectVal + ", 신체:" + physicalVal);

            // 데이터 요청
            loadCustomMovies(physicalVal, emotionVal, intellectVal);
        });

        // [2] 돋보기 아이콘 클릭 -> 검색 페이지로 이동
        btnSearchIcon.setOnClickListener(v -> {
            // SearchFragment로 화면 교체
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_frame_layout, new SearchFragment()) // SearchFragment가 있어야 함
                    .addToBackStack(null) // 뒤로가기 버튼 누르면 다시 돌아오게 함
                    .commit();
        });
    }

    // API 호출 함수
    private void loadCustomMovies(float p, float e, float i) {

        movieService.getCustomRecommendations(p, e, i).enqueue(new Callback<List<MovieItem>>() {
            @Override
            public void onResponse(Call<List<MovieItem>> call, Response<List<MovieItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieItem> movies = response.body();

                    if (!movies.isEmpty()) {
                        Collections.sort(movies, new Comparator<MovieItem>() {
                            @Override
                            public int compare(MovieItem m1, MovieItem m2) {
                                return Double.compare(m2.getRating(), m1.getRating());
                            }
                        });

                        adapter.setMovieList(movies);
                    } else {
                        ToastUtil.show(getContext(), "해당 조건의 추천 영화가 없습니다.");
                    }
                } else {
                    Log.e("API_ERROR", "탐색 실패: " + response.code());
                    ToastUtil.show(getContext(), "데이터를 불러오지 못했습니다.");
                }
            }

            @Override
            public void onFailure(Call<List<MovieItem>> call, Throwable t) {
                Log.e("API_FAIL", "통신/파싱 실패 원인: " + t.getMessage());
                t.printStackTrace();

                ToastUtil.show(getContext(), "서버 연결을 확인해주세요.");
            }
        });
    }
}