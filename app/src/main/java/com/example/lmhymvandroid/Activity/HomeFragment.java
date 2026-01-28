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
import com.example.lmhymvandroid.Service.MovieService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private ImageView mainPosterImageView;
    private final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. 레이아웃 인플레이트
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 2. XML의 ImageView 연결
        mainPosterImageView = view.findViewById(R.id.imgRecommended);

        // 3. 백엔드 데이터 요청 시작
        fetchHomeData();

        return view;
    }

    private void fetchHomeData() {
        // Fragment가 Context에 안전하게 붙어있는지 확인
        if (getContext() == null) return;

        MovieService apiService = RetrofitClient.getClient(getContext()).create(MovieService.class);

        apiService.getHomeData().enqueue(new Callback<HomeResponse>() {
            @Override
            public void onResponse(Call<HomeResponse> call, Response<HomeResponse> response) {
                // ◀◀ [해결] 로그아웃 등으로 프래그먼트가 소멸되었을 때 Glide 호출 차단
                // 이 체크가 없으면 "getActivity() returns null" 크래시가 발생합니다.
                if (!isAdded() || getActivity() == null || getActivity().isFinishing()) {
                    Log.d("HomeFragment", "프래그먼트가 유효하지 않아 응답 처리를 중단합니다.");
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    HomeResponse homeData = response.body();

                    // 추천 영화 데이터 추출 및 출력
                    if (homeData.recommendedMovie != null) {
                        String posterPath = homeData.recommendedMovie.posterPath;
                        String fullPath = TMDB_IMAGE_BASE_URL + posterPath;

                        // 이제 안전하게 Glide로 이미지를 로드할 수 있습니다.
                        Glide.with(HomeFragment.this)
                                .load(fullPath)
                                .into(mainPosterImageView);

                        Log.d("HomeFragment", "이미지 로드 성공: " + fullPath);
                    }
                }
            }

            @Override
            public void onFailure(Call<HomeResponse> call, Throwable t) {
                // 실패 시에도 UI 조작 전에 상태 체크
                if (isAdded()) {
                    Log.e("API_ERROR", "백엔드 연결 실패: " + t.getMessage());
                }
            }
        });
    }
}