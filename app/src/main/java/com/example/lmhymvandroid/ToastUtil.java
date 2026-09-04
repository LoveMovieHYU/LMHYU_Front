package com.example.lmhymvandroid;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class ToastUtil {
    // minSdk 33(API 31+)에서는 커스텀 뷰 토스트가 무시되므로 표준 Toast 를 사용한다
    public static void show(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 루트 뷰 앵커가 자명한 화면에서 일시적 피드백을 스낵바로 표시한다.
     * 앵커(anchorView)가 불명확한 곳에서는 기존 show(Context, String) 을 그대로 사용한다.
     */
    public static void showSnackbar(View anchorView, String message) {
        if (anchorView == null) return;
        Snackbar.make(anchorView, message, Snackbar.LENGTH_SHORT).show();
    }
}
