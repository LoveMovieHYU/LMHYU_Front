package com.example.lmhymvandroid.Activity; // 패키지명 확인

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecommendFragment extends Fragment {

    // ... (변수 선언 부분 기존 유지) ...
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

        // [수정] 실제 API 대신 테스트 데이터를 로드합니다.
        // loadRecommendedMovies();  <-- 기존 API 호출 주석 처리
        loadTestMovies();         // <-- 테스트 데이터 로드 함수 호출
    }

    // ... (initViews, initRetrofit, loadBiorhythmData 등 기존 메서드 유지) ...

    private void initViews(View view) {
        // ... (기존 코드 유지)
        tvPhysical = view.findViewById(R.id.tv_physical_index);
        tvEmotional = view.findViewById(R.id.tv_emotional_index);
        tvIntellectual = view.findViewById(R.id.tv_intellectual_index);
        tvStatusMsg = view.findViewById(R.id.tv_status_message);

        rvMovieList = view.findViewById(R.id.rv_movie_list);
        rvMovieList.setLayoutManager(new LinearLayoutManager(getContext()));

        // ... (버튼 리스너 등 기존 코드 유지) ...

        adapter = new MovieClickAdapter(getContext(), new MovieClickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieItem movie) {
                // 클릭 시 상세 페이지 이동 로직 (이전 질문에서 구현한 내용)
                android.content.Intent intent = new android.content.Intent(getContext(), MovieDetailActivity.class);
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
        // ... (기존 코드 유지) ...
        // 바이오리듬 API는 잘 된다면 그대로 두시고, 만약 이것도 안 되면 주석 처리하세요.
        movieService.getBiorhythmAnalyze().enqueue(new Callback<BiorhythmResponse>() {
            @Override
            public void onResponse(Call<BiorhythmResponse> call, Response<BiorhythmResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateBiorhythmUI(response.body());
                }
            }
            @Override
            public void onFailure(Call<BiorhythmResponse> call, Throwable t) {
                // 에러 처리
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

    // ==========================================
    // [추가] 강제 테스트 데이터 로드 메서드
    // ==========================================
    private void loadTestMovies() {
        List<MovieItem> testList = new ArrayList<>();

        // TMDB 이미지 기본 URL (포스터 URL이 /로 시작하므로 앞에 붙여줘야 함)
        String imageBaseUrl = "https://image.tmdb.org/t/p/w500";

        // 1. 우리의 잘못
        testList.add(new MovieItem(
                1156594,
                "우리의 잘못",
                imageBaseUrl + "/yCbT1nKemh1AuQgdbns5Cf1RmRj.jpg",
                4.0,
                "2025-10-15",
                Arrays.asList("앙앙") // 장르 리스트
        ));

        // 2. 마르코
        testList.add(new MovieItem(
                1186350,
                "마르코",
                imageBaseUrl + "/6Nj8Y1A9lcReqZZvRHOSiO3iTl6.jpg",
                4.0,
                "2025-10-15",
                Arrays.asList("앙앙")
        ));

        // 3. 쥬라기 월드: 새로운 시작
        testList.add(new MovieItem(
                1234821,
                "쥬라기 월드: 새로운 시작",
                imageBaseUrl + "/ygr4hE8Qpagv8sxZbMw1mtYkcQE.jpg",
                4.0,
                "2025-10-15",
                Arrays.asList("앙앙")
        ));

        // 어댑터에 데이터 세팅
        adapter.setMovieList(testList);

        // UI에 "테스트 모드입니다" 표시 (선택사항)
        if (tvStatusMsg != null) {
            tvStatusMsg.setText("현재 테스트 데이터 표시 중입니다.");
        }
    }

    // [기존 메서드 주석 처리 또는 유지]
    /*
    private void loadRecommendedMovies() {
        movieService.getRecommendedMovies().enqueue(...)
    }
    */
}