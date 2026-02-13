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

        btnMore = view.findViewById(R.id.btn_more); // fragment_recommend.xml에 있는 버튼 ID
        btnMore.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.nav_explore);
            }
        });

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
    // 수정된 loadBiorhythmData: 분석 요청 -> 성공 시 데이터 표시
    private void loadBiorhythmData() {
        // 1단계: 분석 요청 (Redis에 데이터 생성/갱신)
        movieService.getBiorhythmAnalyze().enqueue(new Callback<BiorhythmResponse>() {
            @Override
            public void onResponse(Call<BiorhythmResponse> call, Response<BiorhythmResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 분석 성공! 바로 화면에 표시
                    BiorhythmResponse data = response.body();
                    updateBiorhythmUI(data);
                } else {
                    // 분석 실패 시
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

    // UI 업데이트용 헬퍼 메서드 (중복 코드 제거)
    private void updateBiorhythmUI(BiorhythmResponse data) {
        if (data == null) return;

        // 소수점 제거하고 정수로 표시 (깔끔하게)
        tvPhysical.setText(String.valueOf(Math.round(data.getPhysicalIndex())));
        tvEmotional.setText(String.valueOf(Math.round(data.getEmotionalIndex())));
        tvIntellectual.setText(String.valueOf(Math.round(data.getIntellectualIndex())));
        tvStatusMsg.setText(data.getStatusMessage());
    }

    // 2. 영화 추천 리스트 요청
    // 2. 영화 추천 리스트 요청
    private void loadRecommendedMovies() {
        // Call<List<MovieItem>> 으로 변경됨
        movieService.getRecommendedMovies().enqueue(new Callback<List<MovieItem>>() {
            @Override
            public void onResponse(Call<List<MovieItem>> call, Response<List<MovieItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // response.body() 자체가 이미 리스트입니다! (.getMovieList() 필요 없음)
                    List<MovieItem> movies = response.body();
                    adapter.setMovieList(movies);
                } else {
                    Log.e("API_ERROR", "영화 리스트 로드 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<MovieItem>> call, Throwable t) {
                // 여기에 "Expected BEGIN_OBJECT but was BEGIN_ARRAY" 에러가 찍히고 있었을 겁니다.
                Log.e("API_FAIL", t.getMessage());
            }
        });
    }
}