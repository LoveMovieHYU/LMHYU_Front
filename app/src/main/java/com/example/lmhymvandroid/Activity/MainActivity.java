package com.example.lmhymvandroid.Activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

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
            replaceFragment(new MyPageFragment());
            return true;
        }

        return false;
    }


    public void logout() {
        String refreshToken = tokenManager.getRefreshToken();
        if (refreshToken == null) return;

        Call<Void> call = authService.requestLogout(refreshToken);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                performClientSideLogout();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Logout", "Logout API call failed", t);
                performClientSideLogout();
            }
        });
    }

    private void performClientSideLogout() {
        tokenManager.clearTokens();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}