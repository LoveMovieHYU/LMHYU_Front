package com.example.lmhymvandroid.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.lmhymvandroid.DTO.HomeResponse;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.MovieApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private ImageView mainPosterImageView;
    private final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. 레이아웃 인플레이트 (화면 그리기)
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 2. XML의 ImageView 연결 (imgRecommended는 xml에 정의한 ID)
        mainPosterImageView = view.findViewById(R.id.imgRecommended);

        // 3. 백엔드 데이터 요청
        fetchHomeData();

        return view;
    }

    private void fetchHomeData() {
        // [중요] Fragment는 requireContext()를 통해 권한을 넘겨줘야 합니다.
        MovieApiService apiService = RetrofitClient.getClient(requireContext()).create(MovieApiService.class);

        apiService.getHomeData().enqueue(new Callback<HomeResponse>() {
            @Override
            public void onResponse(Call<HomeResponse> call, Response<HomeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HomeResponse homeData = response.body();

                    // DTO 필드 구조에 따라 추천 영화 데이터 추출
                    if (homeData.recommendedMovie != null) {
                        String posterPath = homeData.recommendedMovie.posterPath;
                        String fullPath = TMDB_IMAGE_BASE_URL + posterPath;

                        // 4. Glide로 이미지 출력
                        // Fragment에서는 Glide.with(this) 또는 Glide.with(getContext())를 사용합니다.
                        Glide.with(HomeFragment.this)
                                .load(fullPath)
                                .into(mainPosterImageView);
                    }
                }
            }

            @Override
            public void onFailure(Call<HomeResponse> call, Throwable t) {
                Log.e("API_ERROR", "백엔드 연결 실패: " + t.getMessage());
            }
        });
    }
}