package com.example.lmhymvandroid.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.Adapter.FavoriteMovieAdapter;
import com.example.lmhymvandroid.DTO.MovieSummaryResponseDTO;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteMoviesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoriteMovieAdapter adapter;
    private List<MovieSummaryResponseDTO> movieList = new ArrayList<>();
    private TokenManager tokenManager;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_movies);

        // 1. 초기화
        tokenManager = new TokenManager(this);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        // 2. 뷰 연결 및 뒤로가기 설정
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_favorite_movies);

        // 3. RecyclerView 설정 (한 줄에 3개씩 나오도록 Grid Layout 사용)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        // 어댑터 연결
        adapter = new FavoriteMovieAdapter(this, movieList);
        recyclerView.setAdapter(adapter);

        // 4. API 호출하여 데이터 가져오기
        fetchFavoriteMovies();
    }

    private void fetchFavoriteMovies() {
        String token = tokenManager.getAccessToken();
        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        authService.getFavoriteMovies("Bearer " + token).enqueue(new Callback<List<MovieSummaryResponseDTO>>() {
            @Override
            public void onResponse(Call<List<MovieSummaryResponseDTO>> call, Response<List<MovieSummaryResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieList.clear();
                    movieList.addAll(response.body());
                    adapter.notifyDataSetChanged(); // 화면 갱신
                } else {
                    Log.e("FavoriteMovies", "응답 실패: " + response.code());
                    Toast.makeText(FavoriteMoviesActivity.this, "목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MovieSummaryResponseDTO>> call, Throwable t) {
                Log.e("FavoriteMovies", "네트워크 에러: " + t.getMessage());
                Toast.makeText(FavoriteMoviesActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}