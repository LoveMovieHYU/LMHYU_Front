package com.example.lmhymvandroid.Activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide; // 이미지 로딩 라이브러리
import com.example.lmhymvandroid.DTO.MovieDetailResponse;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.MovieApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {

    private TextView title, genre, date, rating, plot;
    private ImageView poster;
    private ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // 뷰 연결
        title = findViewById(R.id.detail_title);
        genre = findViewById(R.id.detail_genre);
        date = findViewById(R.id.detail_date);
        rating = findViewById(R.id.detail_rating);
        plot = findViewById(R.id.detail_plot);
        poster = findViewById(R.id.detail_poster);
        backBtn = findViewById(R.id.btn_back);

        // 뒤로가기 버튼 기능
        backBtn.setOnClickListener(v -> finish());

        // HomeFragment에서 넘겨준 영화 ID 받기
        long movieId = getIntent().getLongExtra("MOVIE_ID", -1);

        if (movieId != -1) {
            loadMovieDetail(movieId);
        } else {
            Toast.makeText(this, "영화 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadMovieDetail(long movieId) {
        MovieApiService service = RetrofitClient.getClient(this).create(MovieApiService.class);

        service.getMovieDetail(movieId).enqueue(new Callback<MovieDetailResponse>() {
            @Override
            public void onResponse(Call<MovieDetailResponse> call, Response<MovieDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetailResponse movie = response.body();

                    // 텍스트 설정
                    title.setText(movie.getTitle());
                    genre.setText("장르: " + movie.getGenre());
                    date.setText("개봉: " + movie.getReleaseDate());
                    rating.setText("평점: ★ " + movie.getAvgRating());
                    plot.setText(movie.getPlot());

                    // 이미지 로딩 (Glide 사용 권장)
                    Glide.with(MovieDetailActivity.this)
                            .load(movie.getPosterUrl())
                            .into(poster);
                }
            }

            @Override
            public void onFailure(Call<MovieDetailResponse> call, Throwable t) {
                Toast.makeText(MovieDetailActivity.this, "통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}