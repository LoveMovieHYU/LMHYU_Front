package com.example.lmhymvandroid.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lmhymvandroid.DTO.GoogleLoginRequest;
import com.example.lmhymvandroid.DTO.LoginResponseDTO;
import com.example.lmhymvandroid.DTO.NaverLoginRequest;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.AuthService;
import com.example.lmhymvandroid.TokenManager;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.OAuthLoginCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> mGoogleSignInLauncher;
    private TokenManager tokenManager;
    private AuthService authService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);
        authService = RetrofitClient.getClient(this).create(AuthService.class);

        // ================== 구글 로그인 설정 ==================
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .requestScopes(
                        new Scope("https://www.googleapis.com/auth/profile.agerange.read"),
                        new Scope("https://www.googleapis.com/auth/user.gender.read")
                )
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleSignInResult(task);
                    }
                });
        SignInButton googleSignInButton = findViewById(R.id.sign_in_button);
        googleSignInButton.setOnClickListener(v -> signIn());


        // ================== 네이버 로그인 설정 ==================
        NaverIdLoginSDK.INSTANCE.initialize(this,
                getString(R.string.naver_client_id),
                getString(R.string.naver_client_secret),
                getString(R.string.naver_client_name));

        View naverLoginButton = findViewById(R.id.button_naver_login);
        naverLoginButton.setOnClickListener(v -> startNaverLogin());
    }

    private void startNaverLogin() {
        OAuthLoginCallback oauthLoginCallback = new OAuthLoginCallback() {
            @Override
            public void onSuccess() {
                String accessToken = NaverIdLoginSDK.INSTANCE.getAccessToken();
                sendNaverTokenToBackend(accessToken);
            }

            @Override
            public void onFailure(int httpStatus, String message) {
                Log.e("NaverLogin", "로그인 실패: " + message);
            }

            @Override
            public void onError(int errorCode, String message) {
                onFailure(errorCode, message);
            }
        };
        NaverIdLoginSDK.INSTANCE.authenticate(this, oauthLoginCallback);
    }

    private void sendNaverTokenToBackend(String accessToken) {
        Call<LoginResponseDTO> call = authService.requestNaverLogin(new NaverLoginRequest(accessToken));
        call.enqueue(new Callback<LoginResponseDTO>() {
            @Override
            public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulLogin(response.body());
                } else {
                    Log.e("Backend", "네이버 로그인 실패: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
                Log.e("Backend", "네이버 API 호출 실패", t);
            }
        });
    }


    // --- 구글 로그인 관련 메소드 ---
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        mGoogleSignInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            if (idToken == null) {
                Log.e("GoogleSignIn", "ID Token is null. 로그인 실패.");
                return;
            }

            getAccessTokenInBackground(account, idToken);

        } catch (ApiException e) {
            Log.w("GoogleSignIn", "signInResult:failed code=" + e.getStatusCode());
        }
    }


    private void getAccessTokenInBackground(GoogleSignInAccount account, String idToken) {

        String scope = "oauth2:" + "https://www.googleapis.com/auth/profile.agerange.read" + " " + "https://www.googleapis.com/auth/user.gender.read";

        executor.execute(() -> {
            try {
                String accessToken = GoogleAuthUtil.getToken(
                        LoginActivity.this,
                        account.getAccount(),
                        scope
                );

                runOnUiThread(() -> {
                    sendTokensToBackend(idToken, accessToken);
                });

            } catch (Exception e) {
                Log.e("GoogleSignIn", "getAccessTokenInBackground: accessToken 획득 실패", e);
            }
        });
    }

    private void sendTokensToBackend(String idToken, String accessToken) {
        Call<LoginResponseDTO> call = authService.requestGoogleLogin(new GoogleLoginRequest(idToken, accessToken));
        call.enqueue(new Callback<LoginResponseDTO>() {
            @Override
            public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulLogin(response.body());
                } else {
                    Log.e("Backend", "구글 로그인 실패: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
                Log.e("Backend", "구글 API 호출 실패", t);
            }
        });
    }

    /**
     * ## 로그인 성공 시 공통 처리 로직  ##
     */
    private void handleSuccessfulLogin(LoginResponseDTO loginResponse) {
        tokenManager.saveTokens(
                loginResponse.getAccessToken(),
                loginResponse.getRefreshToken(),
                loginResponse.getUserId()
        );

        if (loginResponse.isNewUser()) {
            Intent intent = new Intent(LoginActivity.this, CreateNicknameActivity.class);
            intent.putExtra("USER_ID", loginResponse.getUserId());
            startActivity(intent);
        } else {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

        finishAffinity();
    }
}