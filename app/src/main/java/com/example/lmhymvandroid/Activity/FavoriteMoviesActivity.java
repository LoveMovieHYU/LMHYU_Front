package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.Adapter.FavoriteMovieAdapter;
import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.DTO.MovieSummaryResponseDTO;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.ToastUtil;
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

        tokenManager = new TokenManager(this);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.rv_favorite_movies);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new FavoriteMovieAdapter(this, movieList, new FavoriteMovieAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieSummaryResponseDTO movieSummary) {
                int movieId = movieSummary.getId();
                String title = movieSummary.getTitle();
                String posterUrl = movieSummary.getPosterPath();

                MovieItem tempMovieItem = new MovieItem(
                        movieId,
                        title,
                        posterUrl,
                        0.0,
                        "-",
                        new ArrayList<>()
                );

                Intent intent = new Intent(FavoriteMoviesActivity.this, MovieDetailActivity.class);
                intent.putExtra("movie_data", tempMovieItem);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
        fetchFavoriteMovies();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchFavoriteMovies(); // 화면에 돌아올 때마다 최신 좋아요 목록으로 새로고침
    }

    private void fetchFavoriteMovies() {
        String token = tokenManager.getAccessToken();
        if (token == null) {
            ToastUtil.show(this, "로그인이 필요합니다.");
            return;
        }

        // 인증 헤더는 AuthInterceptor 가 자동 부착한다
        authService.getFavoriteMovies().enqueue(new Callback<List<MovieSummaryResponseDTO>>() {
            @Override
            public void onResponse(Call<List<MovieSummaryResponseDTO>> call, Response<List<MovieSummaryResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    movieList.clear();
                    movieList.addAll(response.body());
                    adapter.notifyDataSetChanged(); // 화면 갱신
                } else {
                    Log.e("FavoriteMovies", "응답 실패: " + response.code());
                    ToastUtil.show(FavoriteMoviesActivity.this, "목록을 불러오지 못했습니다.");
                }
            }

            @Override
            public void onFailure(Call<List<MovieSummaryResponseDTO>> call, Throwable t) {
                Log.e("FavoriteMovies", "네트워크 에러: " + t.getMessage());
                ToastUtil.show(FavoriteMoviesActivity.this, "네트워크 오류가 발생했습니다.");
            }
        });
    }
}