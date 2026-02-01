package com.example.lmhymvandroid.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.lmhymvandroid.DTO.MovieRecommendationResponse;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.MovieService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendFragment extends Fragment {

    // XML ID와 연결할 변수들
    private TextView tvPhysical, tvEmotional, tvIntellectual, tvStatusMsg;
    private RecyclerView rvMovieList;
    private MovieClickAdapter adapter;
    private MovieService movieService;

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

        // 데이터 호출
        loadBiorhythmData();
        loadRecommendedMovies();
    }

    private void initViews(View view) {
        // 바이오리듬 카드 연결
        tvPhysical = view.findViewById(R.id.tv_physical_index);
        tvEmotional = view.findViewById(R.id.tv_emotional_index);
        tvIntellectual = view.findViewById(R.id.tv_intellectual_index);
        tvStatusMsg = view.findViewById(R.id.tv_status_message);

        // 영화 리스트 연결
        rvMovieList = view.findViewById(R.id.rv_movie_list);
        rvMovieList.setLayoutManager(new LinearLayoutManager(getContext()));

        // 어댑터 설정 (클릭 시 이벤트)
        adapter = new MovieClickAdapter(getContext(), new MovieClickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieItem movie) {
                Toast.makeText(getContext(), movie.getTitle() + " 선택", Toast.LENGTH_SHORT).show();
            }
        });
        rvMovieList.setAdapter(adapter);
    }

    private void initRetrofit() {
        if (getContext() != null) {
            // RetrofitClient를 사용하여 토큰이 포함된 요청을 보냄
            movieService = RetrofitClient.getClient(getContext()).create(MovieService.class);
        }
    }

    // 1. 바이오리듬 데이터 요청
    private void loadBiorhythmData() {
        movieService.getBiorhythmAnalyze().enqueue(new Callback<BiorhythmResponse>() {
            @Override
            public void onResponse(Call<BiorhythmResponse> call, Response<BiorhythmResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BiorhythmResponse data = response.body();

                    // 숫자 데이터를 문자열로 변환해 화면에 표시
                    tvPhysical.setText(String.valueOf(data.getPhysicalIndex()));
                    tvEmotional.setText(String.valueOf(data.getEmotionalIndex()));
                    tvIntellectual.setText(String.valueOf(data.getIntellectualIndex()));
                    tvStatusMsg.setText(data.getStatusMessage());
                } else {
                    // 서버 오류 시
                    tvStatusMsg.setText("데이터 분석에 실패했습니다.");
                }
            }

            @Override
            public void onFailure(Call<BiorhythmResponse> call, Throwable t) {
                // 통신 실패 시
                tvStatusMsg.setText("서버 연결 상태를 확인해주세요.");
                Log.e("API_FAIL", t.getMessage());
            }
        });
    }

    // 2. 영화 추천 리스트 요청
    private void loadRecommendedMovies() {
        movieService.getRecommendedMovies().enqueue(new Callback<MovieRecommendationResponse>() {
            @Override
            public void onResponse(Call<MovieRecommendationResponse> call, Response<MovieRecommendationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieItem> movies = response.body().getMovieList();
                    if (movies != null) {
                        adapter.setMovieList(movies); // 어댑터에 데이터 전달
                    }
                } else {
                    Log.e("API_ERROR", "영화 리스트 로드 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MovieRecommendationResponse> call, Throwable t) {
                Log.e("API_FAIL", t.getMessage());
            }
        });
    }
}