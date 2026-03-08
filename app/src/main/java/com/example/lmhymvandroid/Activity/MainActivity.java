package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
    private DrawerLayout drawerLayout;

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

            //  드로어 레이아웃 초기화 (XML의 id와 일치해야 함)
            drawerLayout = findViewById(R.id.drawer_layout);

            // 네비게이션 뷰 설정
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setOnItemSelectedListener(this);

            // 초기 프래그먼트 설정
            if (savedInstanceState == null) {
                replaceFragment(new RecommendFragment());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.drawer_my_page_container, new MyPageFragment())
                        .commit();
            }

            // 모든 설정이 완료되었으므로 스플래시 화면을 걷어냅니다.
            isReady = true;
        }
    }

    // 햄버거 버튼을 누르면 호출할 메서드 (우측 드로어 열기)
    public void openMyPageDrawer() {
        if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    // X 버튼을 누르면 호출할 메서드 (우측 드로어 닫기)
    public void closeMyPageDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
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
        }
        return false;
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