package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreFragment extends Fragment {

    // View 변수 선언
    private Slider sliderEmotion, sliderIntellect, sliderPhysical;
    private Button btnSettingComplete;
    private ImageView btnSearchIcon;
    private RecyclerView rvExploreMovieList;
    private TextView btnRandom;
    private TextView tvEmotionVal, tvIntellectVal, tvPhysicalVal;

    // 어댑터 및 통신 서비스
    private MovieClickAdapter adapter;
    private MovieService movieService;
    private TextView tvDescBox;
    private ProgressBar pbExploreLoading;

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

        tvEmotionVal = view.findViewById(R.id.tv_emotion_val);
        tvIntellectVal = view.findViewById(R.id.tv_intellect_val);
        tvPhysicalVal = view.findViewById(R.id.tv_physical_val);

        // 버튼 연결
        btnSettingComplete = view.findViewById(R.id.btn_setting_complete);
        btnRandom = view.findViewById(R.id.btn_random);
        btnSearchIcon = view.findViewById(R.id.btn_search_icon);

        tvDescBox = view.findViewById(R.id.tv_desc_box);
        pbExploreLoading = view.findViewById(R.id.pb_explore_loading);

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

        sliderEmotion.addOnChangeListener((slider, value, fromUser) -> tvEmotionVal.setText(String.valueOf((int) value)));
        sliderIntellect.addOnChangeListener((slider, value, fromUser) -> tvIntellectVal.setText(String.valueOf((int) value)));
        sliderPhysical.addOnChangeListener((slider, value, fromUser) -> tvPhysicalVal.setText(String.valueOf((int) value)));

        btnRandom.setOnClickListener(v -> {
            Random random = new Random();

            float randEmotion = random.nextInt(201) - 100;
            float randIntellect = random.nextInt(201) - 100;
            float randPhysical = random.nextInt(201) - 100;

            // setValue()를 호출하면 UI 슬라이더가 움직이며 자동으로 addOnChangeListener를 트리거하여 텍스트도 변경됨
            sliderEmotion.setValue(randEmotion);
            sliderIntellect.setValue(randIntellect);
            sliderPhysical.setValue(randPhysical);
        });

        btnSettingComplete.setOnClickListener(v -> {
            float emotionVal = sliderEmotion.getValue();
            float intellectVal = sliderIntellect.getValue();
            float physicalVal = sliderPhysical.getValue();

            Log.d("EXPLORE_LOG", "요청 값 -> 감정:" + emotionVal + ", 지성:" + intellectVal + ", 신체:" + physicalVal);

            loadCustomMovies(physicalVal, emotionVal, intellectVal);
        });

        btnSearchIcon.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_frame_layout, new SearchFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    // API 호출 함수
    private void loadCustomMovies(float p, float e, float i) {

        pbExploreLoading.setVisibility(View.VISIBLE);
        rvExploreMovieList.setVisibility(View.GONE);
        tvDescBox.setVisibility(View.GONE);

        movieService.getCustomRecommendations(p, e, i).enqueue(new Callback<List<MovieItem>>() {
            @Override
            public void onResponse(Call<List<MovieItem>> call, Response<List<MovieItem>> response) {
                // detach 후 콜백이 도착하면 getContext()가 null 이 되어 NPE 발생하므로 방어
                if (!isAdded() || getContext() == null) return;

                pbExploreLoading.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<MovieItem> movies = response.body();

                    if (!movies.isEmpty()) {

                        adapter.setMovieList(movies);
                        rvExploreMovieList.setVisibility(View.VISIBLE);
                    } else {
                        ToastUtil.showSnackbar(getView(), "해당 조건의 추천 영화가 없습니다.");
                        tvDescBox.setText("해당 조건의 영화가 없습니다. 슬라이더를 다시 조절해 보세요.");
                        tvDescBox.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e("API_ERROR", "탐색 실패: " + response.code());
                    ToastUtil.showSnackbar(getView(), "데이터를 불러오지 못했습니다.");
                }
            }

            @Override
            public void onFailure(Call<List<MovieItem>> call, Throwable t) {
                // detach 후 콜백이 도착하면 getContext()가 null 이 되어 NPE 발생하므로 방어
                if (!isAdded() || getContext() == null) return;

                pbExploreLoading.setVisibility(View.GONE);
                tvDescBox.setVisibility(View.VISIBLE);

                Log.e("API_FAIL", "통신/파싱 실패 원인: " + t.getMessage());
                t.printStackTrace();

                ToastUtil.showSnackbar(getView(), "서버 연결을 확인해주세요.");
            }
        });
    }
}