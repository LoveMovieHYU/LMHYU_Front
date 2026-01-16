package com.example.lmhymvandroid.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.lmhymvandroid.DTO.ReactionRequest;
import com.example.lmhymvandroid.DTO.RecommendationResponse;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.MovieApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private ImageView posterImageView;
    private TextView titleTextView; // 필요하면 사용
    private Button btnLike, btnDislike;

    // 현재 보고 있는 영화 정보
    private Long currentMovieId = -1L;

    private List<RecommendationResponse> movieBuffer; // 영화 10개를 담아둘 임시 창고
    private int currentIndex = 0; // 현재 보여주고 있는 영화가 몇 번째인지

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 뷰 연결
        posterImageView = view.findViewById(R.id.img_poster);
        btnLike = view.findViewById(R.id.btn_like);
        btnDislike = view.findViewById(R.id.btn_dislike);

        // 버튼 리스너 설정
        btnLike.setOnClickListener(v -> sendReaction("LIKE"));
        btnDislike.setOnClickListener(v -> sendReaction("DISLIKE"));

        // 초기 데이터 로딩
        fetchRecommendation();

        return view;
    }

    // 1. 추천 영화 가져오기
    // 1. 추천 영화 가져오기 (10개씩 요청)
    private void fetchRecommendation() {

        MovieApiService apiService = RetrofitClient.getClient(requireContext()).create(MovieApiService.class);

        SharedPreferences prefs = requireContext().getSharedPreferences("UserEmotionPref", Context.MODE_PRIVATE);
        String savedEmotion = prefs.getString("today_emotion", "HAPPY"); // 값이 없으면 기본값 HAPPY (혹은 예외처리)
        // limit을 10으로 변경!
        apiService.getRecommendations(savedEmotion,1, 10).enqueue(new Callback<List<RecommendationResponse>>() {
            @Override
            public void onResponse(Call<List<RecommendationResponse>> call, Response<List<RecommendationResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // 받아온 10개를 버퍼에 저장
                    movieBuffer = response.body();
                    currentIndex = 0; // 인덱스 초기화

                    // 첫 번째 영화 보여주기
                    updateUI(movieBuffer.get(0));
                } else {
                    Toast.makeText(getContext(), "추천할 영화가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<RecommendationResponse>> call, Throwable t) {
                Log.e("HomeFragment", "추천 API 실패", t);
            }
        });
    }

    // 2. 화면 갱신
    private void updateUI(RecommendationResponse movie) {
        currentMovieId = movie.getMovieId();

        Glide.with(this)
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.ic_launcher_background) // 로딩 중 이미지
                .into(posterImageView);
    }

    // 3. 다음 영화 보여주기 (서버 통신 X, 메모리에서 꺼냄 O)
    private void showNextMovie() {
        currentIndex++; // 다음 번호로 이동

        if (movieBuffer != null && currentIndex < movieBuffer.size()) {
            // 아직 버퍼에 영화가 남아있으면 -> 바로 보여줌 (0.01초 소요)
            updateUI(movieBuffer.get(currentIndex));
        } else {
            // 버퍼가 바닥났으면 -> 다시 서버에서 10개 가져옴
            fetchRecommendation();
        }
    }

    // 4. 버튼 클릭 로직 수정
    private void sendReaction(String type) {
        if (currentMovieId == -1L) return;

        MovieApiService apiService = RetrofitClient.getClient(requireContext()).create(MovieApiService.class);
        apiService.sendReaction(currentMovieId, new ReactionRequest(type)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (type.equals("LIKE")) {
                        // [좋아요] -> 상세 페이지로 이동
                        Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
                        intent.putExtra("MOVIE_ID", currentMovieId);
                        startActivity(intent);
                    } else {
                        // [싫어요] -> 다음 영화 보여주기 (수정된 함수 호출)
                        // fetchRecommendation(); // <--- 이거 지우고
                        showNextMovie();          // <--- 이걸로 변경!
                    }
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 실패해도 일단 다음 영화 보여주는 게 UX상 좋을 수 있음 (선택 사항)
                Toast.makeText(getContext(), "반응 전송 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}