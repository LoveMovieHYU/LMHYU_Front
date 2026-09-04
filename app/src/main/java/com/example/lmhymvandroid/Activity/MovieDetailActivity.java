package com.example.lmhymvandroid.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.lmhymvandroid.DTO.LikeRequest;
import com.example.lmhymvandroid.DTO.MovieDetailResponse;
import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.MovieService;
import com.example.lmhymvandroid.ToastUtil;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailActivity extends AppCompatActivity {

    private MaterialButton btnLike;
    private MovieService movieService;
    private long currentMovieId;

    private boolean isLiked = false;

    // UI 변수
    private ImageView ivPoster, btnBack;
    private TextView tvTitle, tvInfo, tvSummary;
    // private AppCompatButton btnLike;
    private LinearLayout layoutCast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        movieService = RetrofitClient.getClient(this).create(MovieService.class);
        initViews();

        // 1. Intent에서 MovieItem 받기
        MovieItem movieItem = (MovieItem) getIntent().getSerializableExtra("movie_data");

        if (movieItem == null) {
            ToastUtil.show(this, "영화 정보를 불러올 수 없습니다.");
            finish();
            return;
        }

        currentMovieId = movieItem.getId();
        Log.d("MovieDetail", "전달받은 Movie ID: " + currentMovieId);

        // 2. 화면 초기화 (로딩 상태 표시)
        setLoadingState();

        // 3. 실제 서버에 상세 정보 요청 (이 응답값으로만 화면을 채움)
        loadMovieDetail(currentMovieId);

        // 4. 버튼 리스너
        btnBack.setOnClickListener(v -> finish());
        btnLike.setOnClickListener(v -> {
            if (isLiked) {
                requestUnlikeMovie(); // 이미 좋아요 상태면 취소 요청
            } else {
                requestLikeMovie();   // 좋아요 상태가 아니면 좋아요 요청
            }
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        ivPoster = findViewById(R.id.iv_detail_poster);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvInfo = findViewById(R.id.tv_detail_genre_year_rating);
        tvSummary = findViewById(R.id.tv_summary_content);
        btnLike = findViewById(R.id.btn_like);
        layoutCast = findViewById(R.id.layout_cast_container);
    }

    // API 응답 전까지 보여줄 상태
    private void setLoadingState() {
        tvTitle.setText("로딩 중...");
        tvInfo.setText("");
        tvSummary.setText("상세 정보를 불러오는 중입니다...");
        // 포스터는 비워두거나 로딩 이미지를 둡니다.
        ivPoster.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    // API 호출
    private void loadMovieDetail(long tmdbId) {
        movieService.getMovieDetail(tmdbId).enqueue(new Callback<MovieDetailResponse>() {
            @Override
            public void onResponse(Call<MovieDetailResponse> call, Response<MovieDetailResponse> response) {
                // 액티비티가 이미 종료/파괴된 뒤 콜백이 도착하면 Glide 등 UI 접근 시 크래시하므로 방어
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    // 성공 시: 백엔드 데이터로만 UI 업데이트
                    Log.d("MovieDetail", "API 성공: " + response.body().getMovieTitle());
                    updateUI(response.body());
                } else {
                    // 실패 시: 에러 메시지 표시 (절대 MovieItem 데이터 보여주지 않음)
                    Log.e("API_ERROR", "상세 조회 실패: " + response.code());
                    tvTitle.setText("조회 실패");
                    tvSummary.setText("서버에서 정보를 불러오지 못했습니다. (코드: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<MovieDetailResponse> call, Throwable t) {
                Log.e("API_FAIL", "통신 실패: " + t.getMessage());
                tvTitle.setText("네트워크 오류");
                tvSummary.setText("서버와 연결할 수 없습니다.");
            }
        });
    }

    // 백엔드 데이터(MovieDetailResponse)로만 화면 그리기
    private void updateUI(MovieDetailResponse detail) {
        // 1. 제목
        tvTitle.setText(detail.getMovieTitle());

        // 2. 정보 조합 (개봉년도 - 러닝타임 - 별점)
        // 백엔드 필드: releaseYear(String), formattedRuntime(String), rating(double)
        String runtime = detail.getFormattedRuntime() != null ? detail.getFormattedRuntime() : "";
        String year = detail.getReleaseYear() != null ? detail.getReleaseYear() : "";

        // 예: 2024 - 1h 55m - ★3.5
        String info = String.format("%s - %s - ★%.1f", year, runtime, detail.getRating());
        tvInfo.setText(info);

        // 3. 줄거리
        if (detail.getOverview() != null && !detail.getOverview().isEmpty()) {
            tvSummary.setText(detail.getOverview());
        } else {
            tvSummary.setText("줄거리 정보가 없습니다.");
        }

        // 4. 포스터 (백엔드에서 온 URL 사용)
        String posterPath = detail.getPosterPath();
        String fullPosterUrl = (posterPath != null && posterPath.startsWith("http"))
                ? posterPath
                : "https://image.tmdb.org/t/p/w500" + posterPath;

        Glide.with(this)
                .load(fullPosterUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(ivPoster);

        // 5. 배우 및 감독 리스트
        if (layoutCast != null) {
            layoutCast.removeAllViews();

            // 감독
            if (detail.getDirectors() != null) {
                for (MovieDetailResponse.PersonDTO person : detail.getDirectors()) {
                    addPersonView(person, "Director");
                }
            }
            // 배우
            if (detail.getActors() != null) {
                for (MovieDetailResponse.PersonDTO person : detail.getActors()) {
                    addPersonView(person, "Actor");
                }
            }
        }

        this.isLiked = detail.isLiked();
        updateLikeButtonUI();
    }

    private void updateLikeButtonUI() {
        if (isLiked) {
            btnLike.setText("좋아요");
            //btnLike.setIconResource(R.drawable.ic_heart_red);
            btnLike.setIconTintResource(R.color.red);
        } else {
            btnLike.setText("좋아요");

            btnLike.setIconTintResource(R.color.black);
        }
    }

    // 배우/감독 뷰 생성
    private void addPersonView(MovieDetailResponse.PersonDTO person, String role) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_cast, layoutCast, false);

        ImageView ivProfile = view.findViewById(R.id.iv_cast_profile);
        TextView tvName = view.findViewById(R.id.tv_cast_name);
        TextView tvRole = view.findViewById(R.id.tv_cast_role);

        tvName.setText(person.getName());
        tvRole.setText(role);

        String profilePath = person.getProfileImagePath();
        String fullProfileUrl = (profilePath != null && profilePath.startsWith("http"))
                ? profilePath
                : "https://image.tmdb.org/t/p/w500" + profilePath;

        Glide.with(this)
                .load(fullProfileUrl)
                .transform(new CircleCrop())
                .placeholder(R.drawable.bg_circle_gray)
                .error(R.drawable.bg_circle_gray)
                .into(ivProfile);

        layoutCast.addView(view);
    }

    private void requestLikeMovie() {
        LikeRequest request = new LikeRequest("LIKE");

        movieService.postLike(currentMovieId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    ToastUtil.show(MovieDetailActivity.this, "좋아요 반영 완료! ❤️");

                    isLiked = true;
                    updateLikeButtonUI();

                } else {
                    ToastUtil.show(MovieDetailActivity.this, "좋아요 실패");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                ToastUtil.show(MovieDetailActivity.this, "네트워크 오류");
            }
        });
    }

    private void requestUnlikeMovie() {
        movieService.deleteLike(currentMovieId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    ToastUtil.show(MovieDetailActivity.this, "좋아요 취소 완료! 💔");

                    // 🔥 상태 변경 및 UI 업데이트
                    isLiked = false;
                    updateLikeButtonUI();
                } else {
                    ToastUtil.show(MovieDetailActivity.this, "좋아요 취소 실패");
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                ToastUtil.show(MovieDetailActivity.this, "네트워크 오류");
            }
        });
    }
}