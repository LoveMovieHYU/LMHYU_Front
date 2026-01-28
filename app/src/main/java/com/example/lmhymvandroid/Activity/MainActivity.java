package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    private TokenManager tokenManager;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenManager = new TokenManager(this);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }
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
        if (itemId == R.id.nav_home) {
            replaceFragment(new HomeFragment());
            return true;
        } else if (itemId == R.id.nav_search) {
            replaceFragment(new SearchFragment());
            return true;
        } else if (itemId == R.id.nav_mypage) {
            replaceFragment(new com.example.lmhymvandroid.Activity.MyPageFragment());
            return true;
        }
        return false;
    }

    /**
     * 로그아웃 요청 메서드
     *
     */
    public void logout() {
        Log.d("Logout", "로그아웃 프로세스 시작");

        // 1. 서버에 로그아웃 알림
        authService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Logout", "서버 로그아웃 성공");
                } else {
                    Log.e("Logout", "서버 응답 에러: " + response.code());
                }
                performClientSideLogout();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Logout", "네트워크 에러로 API 호출 실패", t);
                performClientSideLogout();
            }
        });
    }

    /**
     * 클라이언트 데이터 정리 및 화면 전환
     */
    private void performClientSideLogout() {
        tokenManager.clearTokens();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        try {
            startActivity(intent);
            Toast.makeText(this, "성공적으로 로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
            finish(); // ◀ 현재 MainActivity 종료
        } catch (Exception e) {
            Log.e("Logout", "LoginActivity 시작 실패. 메니페스트를 확인하세요.", e);
        }
    }
}