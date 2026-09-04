package com.example.lmhymvandroid.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.ToastUtil;
import com.example.lmhymvandroid.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    private TokenManager tokenManager;
    private AuthService authService;
    private boolean isReady = false;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. 스플래시 화면 설치 및 대기 설정
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        // 데이터(토큰) 확인이 끝날 때까지 스플래시 로고를 화면에 고정합니다.
        splashScreen.setKeepOnScreenCondition(() -> !isReady);

        super.onCreate(savedInstanceState);

        // 2. 관리 도구 초기화
        tokenManager = new TokenManager(this);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        // 3. 로그인 상태 체크 및 분기 처리
        checkLoginAndSetup(savedInstanceState);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 검색 등 백스택에 쌓인 화면이 있으면 먼저 되돌린다
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                } else if (bottomNav != null && bottomNav.getSelectedItemId() != R.id.nav_recommend) {
                    // 추천 탭이 아니면 추천 탭으로 우선 이동
                    bottomNav.setSelectedItemId(R.id.nav_recommend);
                } else {
                    showExitDialog();
                }
            }
        });
    }

    /**
     * 로그인 여부를 확인하고 화면을 구성하거나 로그인 액티비티로 이동시킵니다.
     */
    private void checkLoginAndSetup(Bundle savedInstanceState) {
        String refreshToken = tokenManager.getRefreshToken();

        if (refreshToken == null) {
            // [로그인 안 된 경우] 로그인 액티비티로 즉시 이동
            Log.d("LoginCheck", "토큰 없음: LoginActivity로 이동");
            navigateToLogin();
        } else {
            // [로그인 된 경우] 메인 화면 레이아웃 구성
            Log.d("LoginCheck", "토큰 확인됨: 메인 화면 구성 시작");
            setContentView(R.layout.activity_main);

            // 엣지 투 엣지: 시스템 바 뒤까지 그리고 하단 내비에 인셋을 반영한다
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

            // 네비게이션 뷰 설정 (추천/탐색/검색/마이페이지)
            bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setOnItemSelectedListener(this);

            // 하단 시스템 바(제스처/내비게이션 바)만큼 바텀 내비에 패딩을 준다
            ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bars.bottom);
                return insets;
            });

            // 초기 프래그먼트 설정
            if (savedInstanceState == null) {
                replaceFragment(new RecommendFragment());
            }

            // 모든 설정이 완료되었으므로 스플래시 화면을 걷어냅니다.
            isReady = true;
        }
    }

    /**
     * 추천 화면의 메뉴 버튼에서 호출한다. 마이페이지 탭으로 전환한다.
     * (기존 드로어 열기를 대체 — 호출부 시그니처는 유지)
     */
    public void openMyPageDrawer() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_mypage);
        }
    }

    /**
     * 마이페이지의 닫기 버튼/뒤로가기에서 호출한다. 추천 탭으로 되돌린다.
     * (기존 드로어 닫기를 대체 — 호출부 시그니처는 유지)
     */
    public void closeMyPageDrawer() {
        if (bottomNav != null && bottomNav.getSelectedItemId() != R.id.nav_recommend) {
            bottomNav.setSelectedItemId(R.id.nav_recommend);
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_recommend) {
            replaceFragment(new RecommendFragment());
            return true;
        } else if (itemId == R.id.nav_explore) {
            replaceFragment(new ExploreFragment());
            return true;
        } else if (itemId == R.id.nav_search) {
            replaceFragment(new SearchFragment());
            return true;
        } else if (itemId == R.id.nav_mypage) {
            replaceFragment(new MyPageFragment());
            return true;
        }
        return false;
    }

    private void showExitDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_exit_app);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;
        }

        dialog.findViewById(R.id.btn_continue).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_exit).setOnClickListener(v -> {
            dialog.dismiss();
            finishAffinity();
        });

        dialog.show();
    }

    /**
     * 로그아웃 요청 및 클라이언트 데이터 정리
     */
    public void logout() {
        authService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                performClientSideLogout();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                performClientSideLogout();
            }
        });
    }

    private void performClientSideLogout() {
        tokenManager.clearTokens();
        ToastUtil.show(this, "성공적으로 로그아웃되었습니다.");
        navigateToLogin();
    }
}
