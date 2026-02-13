package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lmhymvandroid.DTO.UserResponseDTO;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageFragment extends Fragment {

    private TextView tvUserName;
    private TextView tvUserStatus;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 초기화
        tokenManager = new TokenManager(requireContext());
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserStatus = view.findViewById(R.id.tv_user_status);

        // 2. 버튼 리스너 설정
        setupButtons(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 화면이 다시 보일 때마다 최신 정보(닉네임 등) 갱신
        fetchUserInfo();
    }

    // --- [기능 1] 사용자 정보 조회 (GET /api/user/) ---
    private void fetchUserInfo() {
        String token = tokenManager.getAccessToken();

        if (token == null) {
            tvUserName.setText("로그인 필요");
            return;
        }

        AuthService authService = RetrofitClient.getClient(requireContext()).create(AuthService.class);

        // 헤더에 토큰 포함하여 요청
        authService.getUserInfo("Bearer " + token).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDTO userInfo = response.body();

                    // ★ [핵심] 서버에서 받은 닉네임으로 설정
                    String nickname = userInfo.getNickname();

                    if (nickname != null && !nickname.isEmpty()) {
                        tvUserName.setText(nickname);
                    } else {
                        tvUserName.setText("닉네임 없음");
                    }

                } else {
                    Log.e("MyPage", "정보 조회 실패 Code: " + response.code());
                    // 토큰 만료 시 처리 등이 필요할 수 있음
                }
            }

            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                Log.e("MyPage", "네트워크 오류: " + t.getMessage());
                // tvUserName.setText("연결 실패");
            }
        });
    }

    // --- [기능 2] 버튼 클릭 이벤트 ---
    private void setupButtons(View view) {

        // 1. 내가 좋아하는 영화
        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite_movies);
        btnFavorite.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FavoriteMoviesActivity.class);
            startActivity(intent);
        });

        // 2. 개인정보 수정
        LinearLayout btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
            startActivity(intent);
        });

        // 3. 탈퇴하기 (방금 만든 DeleteAccountActivity로 연결)
        LinearLayout btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        btnDeleteAccount.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DeleteAccountActivity.class);
            startActivity(intent);
        });

        // 4. 로그아웃
        LinearLayout btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).logout();
            } else {
                Toast.makeText(getActivity(), "로그아웃을 수행할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}