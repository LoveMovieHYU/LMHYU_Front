package com.example.lmhymvandroid.Activity;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.Adapter.MovieClickAdapter;
import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.DTO.MovieRecommendationResponse;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.MovieService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private EditText etSearchInput;
    private ImageView btnBack;
    private LinearLayout layoutEmptyState;
    private RecyclerView rvSearchResult;

    private MovieClickAdapter adapter;
    private MovieService movieService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 뷰 초기화
        etSearchInput = view.findViewById(R.id.et_search_input);
        btnBack = view.findViewById(R.id.btn_back);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        rvSearchResult = view.findViewById(R.id.rv_search_result);

        // 2. 서비스 연결
        if (getContext() != null) {
            movieService = RetrofitClient.getClient(getContext()).create(MovieService.class);
        }

        // 3. 리사이클러뷰 설정 (기존 Adapter 재사용)
        rvSearchResult.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MovieClickAdapter(getContext(), new MovieClickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieItem movie) {
                // TODO: 영화 상세 페이지로 이동
                Toast.makeText(getContext(), movie.getTitle() + " 상세 정보로 이동", Toast.LENGTH_SHORT).show();
            }
        });
        rvSearchResult.setAdapter(adapter);

        // 4. 이벤트 리스너 설정
        setupListeners();

        // 화면 진입 시 키보드 자동으로 올리기 (선택 사항)
        etSearchInput.requestFocus();
        showKeyboard();
    }

    private void setupListeners() {
        // 뒤로가기 버튼
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
            hideKeyboard();
        });

        // 키보드에서 '검색' 버튼 눌렀을 때 실행
        etSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });
    }

    private void performSearch() {
        String keyword = etSearchInput.getText().toString().trim();

        if (keyword.isEmpty()) {
            Toast.makeText(getContext(), "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 키보드 숨기기
        hideKeyboard();

        // API 호출 (페이지 1로 고정, 필요 시 페이징 구현 가능)
        movieService.searchMovies(keyword, 1).enqueue(new Callback<MovieRecommendationResponse>() {
            @Override
            public void onResponse(Call<MovieRecommendationResponse> call, Response<MovieRecommendationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieItem> movies = response.body().getMovieList();

                    if (movies != null && !movies.isEmpty()) {
                        // 결과가 있을 때: 리스트 보여주고 빈 화면 숨김
                        adapter.setMovieList(movies);
                        rvSearchResult.setVisibility(View.VISIBLE);
                        layoutEmptyState.setVisibility(View.GONE);
                    } else {
                        // 결과가 0개일 때
                        showEmptyState();
                        Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showEmptyState();
                    Toast.makeText(getContext(), "검색 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieRecommendationResponse> call, Throwable t) {
                showEmptyState();
                Toast.makeText(getContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
        rvSearchResult.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void showKeyboard() {
        if (getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etSearchInput, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        if (getContext() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}