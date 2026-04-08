package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.Adapter.MovieClickAdapter;
import com.example.lmhymvandroid.DTO.BiorhythmResponse;
import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.MovieService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendFragment extends Fragment {

    private TextView tvPhysical, tvEmotional, tvIntellectual, tvStatusMsg;
    private RecyclerView rvMovieList;
    private MovieClickAdapter adapter;
    private MovieService movieService;
    private ImageView btnMenu;
    private ProgressBar pbMovieLoading;
    private ConstraintLayout layoutBioHeader;
    private LinearLayout layoutBiorhythmContent;
    private ImageView btnBioToggle;
    private View layoutBioDataContainer;
    private View layoutBioInfoContainer;
    private ImageView btnBioInfo;
    private boolean isInfoShowing = false;
    private boolean isBioExpanded = true;
    private View bgDim;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initRetrofit();

        loadBiorhythmData();
        loadRecommendedMovies();
    }

    private void initViews(View view) {
        tvPhysical = view.findViewById(R.id.tv_physical_index);
        tvEmotional = view.findViewById(R.id.tv_emotional_index);
        tvIntellectual = view.findViewById(R.id.tv_intellectual_index);
        tvStatusMsg = view.findViewById(R.id.tv_status_message);

        rvMovieList = view.findViewById(R.id.rv_movie_list);
        rvMovieList.setLayoutManager(new LinearLayoutManager(getContext()));

        pbMovieLoading = view.findViewById(R.id.pb_movie_loading);
        btnMenu = view.findViewById(R.id.btn_menu);

        layoutBioHeader = view.findViewById(R.id.layout_bio_header);
        layoutBiorhythmContent = view.findViewById(R.id.layout_biorhythm_content);
        btnBioToggle = view.findViewById(R.id.btn_bio_toggle);

        bgDim = view.findViewById(R.id.bg_dim);

        layoutBioDataContainer = view.findViewById(R.id.layout_bio_data_container);
        layoutBioInfoContainer = view.findViewById(R.id.layout_bio_info_container);
        btnBioInfo = view.findViewById(R.id.btn_bio_info);

        btnBioInfo.setOnClickListener(v -> toggleBioInfoPanel());
        bgDim.setOnClickListener(v -> toggleBioInfoPanel());
        btnBioToggle.setOnClickListener(v -> toggleBiorhythmPanel());

        btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openMyPageDrawer();
            }
        });

        adapter = new MovieClickAdapter(getContext(), new MovieClickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieItem movie) {
                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra("movie_data", movie);
                startActivity(intent);
            }
        });
        rvMovieList.setAdapter(adapter);
    }

    private void toggleBiorhythmPanel() {
        isBioExpanded = !isBioExpanded;

        if (isBioExpanded) {
            layoutBiorhythmContent.setVisibility(View.VISIBLE);
            btnBioToggle.animate().rotation(0).setDuration(200).start();
        } else {
            layoutBiorhythmContent.setVisibility(View.GONE);
            btnBioToggle.animate().rotation(180).setDuration(200).start();
        }
    }

    private void initRetrofit() {
        if (getContext() != null) {
            movieService = RetrofitClient.getClient(getContext()).create(MovieService.class);
        }
    }

    private void loadBiorhythmData() {
        movieService.getBiorhythmAnalyze().enqueue(new Callback<BiorhythmResponse>() {
            @Override
            public void onResponse(Call<BiorhythmResponse> call, Response<BiorhythmResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BiorhythmResponse data = response.body();
                    updateBiorhythmUI(data);
                } else {
                    tvStatusMsg.setText("데이터 분석에 실패했습니다.");
                    Log.e("API_ERROR", "분석 요청 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<BiorhythmResponse> call, Throwable t) {
                tvStatusMsg.setText("서버 연결 상태를 확인해주세요.");
                Log.e("API_FAIL", t.getMessage());
            }
        });
    }

    private void updateBiorhythmUI(BiorhythmResponse data) {
        if (data == null) return;
        tvPhysical.setText(String.valueOf(Math.round(data.getPhysicalIndex())));
        tvEmotional.setText(String.valueOf(Math.round(data.getEmotionalIndex())));
        tvIntellectual.setText(String.valueOf(Math.round(data.getIntellectualIndex())));
        tvStatusMsg.setText(data.getStatusMessage());
    }

    private void loadRecommendedMovies() {
        pbMovieLoading.setVisibility(View.VISIBLE);
        rvMovieList.setVisibility(View.GONE);

        movieService.getRecommendedMovies().enqueue(new Callback<List<MovieItem>>() {
            @Override
            public void onResponse(Call<List<MovieItem>> call, Response<List<MovieItem>> response) {
                pbMovieLoading.setVisibility(View.GONE);
                rvMovieList.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null) {
                    List<MovieItem> movies = response.body();

                    adapter.setMovieList(movies);
                } else {
                    Log.e("API_ERROR", "영화 리스트 로드 실패: " + response.code());
                    tvStatusMsg.setText("추천 영화를 불러오지 못했습니다.");
                }
            }

            @Override
            public void onFailure(Call<List<MovieItem>> call, Throwable t) {
                pbMovieLoading.setVisibility(View.GONE);
                Log.e("API_FAIL", t.getMessage());
            }
        });
    }
    private void toggleBioInfoPanel() {
        isInfoShowing = !isInfoShowing;

        if (isInfoShowing) {

            bgDim.setVisibility(View.VISIBLE);
            bgDim.setAlpha(0f);
            bgDim.animate().alpha(1f).setDuration(250).start();

            layoutBioInfoContainer.setVisibility(View.VISIBLE);
            layoutBioInfoContainer.setTranslationY(1000f);
            layoutBioInfoContainer.animate()
                    .translationY(0f)
                    .setDuration(250)
                    .start();
        } else {
            // 배경 서서히 사라짐
            bgDim.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> bgDim.setVisibility(View.GONE))
                    .start();

            // 팝업이 위에서 밑으로 슬라이드 다운
            layoutBioInfoContainer.animate()
                    .translationY(layoutBioInfoContainer.getHeight()) // 뷰의 높이만큼 밑으로 내려감
                    .setDuration(200)
                    .withEndAction(() -> layoutBioInfoContainer.setVisibility(View.GONE))
                    .start();
        }
    }
}