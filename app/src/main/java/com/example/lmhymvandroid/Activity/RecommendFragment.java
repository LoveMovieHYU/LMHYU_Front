package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.Adapter.MovieClickAdapter;
import com.example.lmhymvandroid.DTO.BiorhythmResponse;
import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.MovieService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
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
    private AppCompatButton btnMore;

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

        btnMore = view.findViewById(R.id.btn_more);
        btnMore.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.nav_explore);
            }
        });

        adapter = new MovieClickAdapter(getContext(), new MovieClickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieItem movie) {
                Toast.makeText(getContext(), movie.getTitle() + " 선택", Toast.LENGTH_SHORT).show();
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

                    // 평점(Rating) 기준으로 내림차순 정렬
                    Collections.sort(movies, new Comparator<MovieItem>() {
                        @Override
                        public int compare(MovieItem m1, MovieItem m2) {
                            return Double.compare(m2.getRating(), m1.getRating());
                        }
                    });

                    // 상위 3개만 뽑아내기 (리스트 크기가 3보다 작을 수 있으므로 예외처리 포함)
                    int limit = Math.min(movies.size(), 3);
                    List<MovieItem> top3Movies = new ArrayList<>(movies.subList(0, limit));

                    // 3개로 추려진 리스트를 어댑터에 전달
                    adapter.setMovieList(top3Movies);

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