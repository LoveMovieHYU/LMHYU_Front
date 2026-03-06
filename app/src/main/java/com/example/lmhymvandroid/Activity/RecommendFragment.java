package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // 🌟 추가됨
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.Adapter.MovieClickAdapter;
import com.example.lmhymvandroid.DTO.BiorhythmResponse;
import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.MovieService;

import java.util.Collections;
import java.util.Comparator;
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

        btnMenu = view.findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(v -> {
            Toast.makeText(getContext(), "메뉴 클릭됨 (추후 프로필 창 연동)", Toast.LENGTH_SHORT).show();
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
        movieService.getRecommendedMovies().enqueue(new Callback<List<MovieItem>>() {
            @Override
            public void onResponse(Call<List<MovieItem>> call, Response<List<MovieItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieItem> movies = response.body();

                    Collections.sort(movies, new Comparator<MovieItem>() {
                        @Override
                        public int compare(MovieItem m1, MovieItem m2) {
                            return Double.compare(m2.getRating(), m1.getRating());
                        }
                    });

                    adapter.setMovieList(movies);

                } else {
                    Log.e("API_ERROR", "영화 리스트 로드 실패: " + response.code());
                    tvStatusMsg.setText("추천 영화를 불러오지 못했습니다.");
                }
            }

            @Override
            public void onFailure(Call<List<MovieItem>> call, Throwable t) {
                Log.e("API_FAIL", t.getMessage());
            }
        });
    }
}