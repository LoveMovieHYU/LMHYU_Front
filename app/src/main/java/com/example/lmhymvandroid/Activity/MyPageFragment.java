package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.Adapter.HorizontalMovieAdapter;
import com.example.lmhymvandroid.DTO.MovieSummaryResponseDTO;
import com.example.lmhymvandroid.DTO.UserResponseDTO;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageFragment extends Fragment {

    private TextView tvUserName;
    private TokenManager tokenManager;
    private AuthService authService;
    private RecyclerView rvLikedMovies;
    private HorizontalMovieAdapter movieAdapter;
    private List<MovieSummaryResponseDTO> likedMovieList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        authService = RetrofitClient.getClient(requireContext()).create(AuthService.class);

        tvUserName = view.findViewById(R.id.tv_user_name);

        // 1. 가로 스크롤 리사이클러뷰 설정
        rvLikedMovies = view.findViewById(R.id.rv_liked_movies_horizontal);
        rvLikedMovies.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        movieAdapter = new HorizontalMovieAdapter(getContext(), likedMovieList);
        rvLikedMovies.setAdapter(movieAdapter);

        // 2. 버튼 리스너 연결
        setupButtons(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUserInfo();
        fetchLikedMovies();
    }

    private void fetchUserInfo() {
        String token = tokenManager.getAccessToken();
        if (token == null) return;

        authService.getUserInfo("Bearer " + token).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String nickname = response.body().getNickname();
                    tvUserName.setText(nickname != null && !nickname.isEmpty() ? nickname : "Nickname");
                }
            }
            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                Log.e("MyPage", "네트워크 오류: " + t.getMessage());
            }
        });
    }

    // 가로 스크롤에 띄울 영화 데이터 가져오기
    private void fetchLikedMovies() {
        String token = tokenManager.getAccessToken();
        if (token == null) return;

        authService.getFavoriteMovies("Bearer " + token).enqueue(new Callback<List<MovieSummaryResponseDTO>>() {
            @Override
            public void onResponse(Call<List<MovieSummaryResponseDTO>> call, Response<List<MovieSummaryResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    likedMovieList.clear();
                    likedMovieList.addAll(response.body());
                    movieAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<MovieSummaryResponseDTO>> call, Throwable t) {
                Log.e("MyPage", "네트워크 에러: " + t.getMessage());
            }
        });
    }

    private void setupButtons(View view) {
        // X 버튼: 드로어 닫기
        ImageView btnClose = view.findViewById(R.id.btn_close_mypage);
        btnClose.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).closeMyPageDrawer();
            }
        });

        // View All > 버튼: 기존 FavoriteMoviesActivity(전체 목록)로 이동
        TextView btnViewAll = view.findViewById(R.id.btn_view_all_movies);
        btnViewAll.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), FavoriteMoviesActivity.class));
        });

        // Edit Personal Information 버튼
        View btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileEditActivity.class));
        });

        // Log Out 버튼
        View btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout();
            }
        });

        // Delete Account 버튼
        View btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        btnDeleteAccount.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), DeleteAccountActivity.class));
        });
    }
}