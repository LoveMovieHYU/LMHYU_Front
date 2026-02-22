package com.example.lmhymvandroid.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.Adapter.MovieClickAdapter;
import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.DTO.MovieSearchResponse;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.RetrofitClient;
import com.example.lmhymvandroid.Service.MovieService;
import com.example.lmhymvandroid.ToastUtil;

import java.util.ArrayList;
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

        // 3. 리사이클러뷰 설정
        rvSearchResult.setLayoutManager(new LinearLayoutManager(getContext()));

        // 클릭 시 상세 페이지로 이동하도록 설정
        adapter = new MovieClickAdapter(getContext(), new MovieClickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieItem movie) {
                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra("movie_data", movie); // 상세 페이지로 데이터 전달
                startActivity(intent);
            }
        });
        rvSearchResult.setAdapter(adapter);

        // 4. 리스너 설정
        setupListeners();

        // 키보드 올리기
        etSearchInput.requestFocus();
        showKeyboard();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            hideKeyboard();
            getParentFragmentManager().popBackStack();
        });

        etSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String keyword = etSearchInput.getText().toString().trim();

        if (keyword.isEmpty()) {
            ToastUtil.show(getContext(), "검색어를 입력해주세요.");
            return;
        }

        hideKeyboard();

        movieService.searchMovies(keyword, 1).enqueue(new Callback<List<MovieSearchResponse>>() {
            @Override
            public void onResponse(Call<List<MovieSearchResponse>> call, Response<List<MovieSearchResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieSearchResponse> resultList = response.body();

                    if (!resultList.isEmpty()) {
                        // [데이터 변환] SearchResponse -> MovieItem
                        List<MovieItem> movieItems = convertToMovieItems(resultList);

                        adapter.setMovieList(movieItems);
                        rvSearchResult.setVisibility(View.VISIBLE);
                        layoutEmptyState.setVisibility(View.GONE);
                    } else {
                        showEmptyState();
                        ToastUtil.show(getContext(), "검색 결과가 없습니다.");
                    }
                } else {
                    showEmptyState();
                    // 404 등 에러 처리
                    if(response.code() == 404) {
                        ToastUtil.show(getContext(), "검색 실패: " + response.code());
                    } else {
                        ToastUtil.show(getContext(), "서버 응답 시간이 초과되었습니다.");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<MovieSearchResponse>> call, Throwable t) {
                showEmptyState();

                // 로그에 정확한 에러 원인 출력
                t.printStackTrace();

                if (t instanceof java.net.SocketTimeoutException) {
                    ToastUtil.show(getContext(), "서버 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
                } else {
                    ToastUtil.show(getContext(), "네트워크 오류가 발생했습니다.");
                }
            }
        });
    }

    private List<MovieItem> convertToMovieItems(List<MovieSearchResponse> searchResults) {
        List<MovieItem> items = new ArrayList<>();

        for (MovieSearchResponse res : searchResults) {

            double defaultRating = 0.0;
            List<String> defaultGenres = new ArrayList<>(); // 빈 리스트

            MovieItem item = new MovieItem(
                    res.getMovieId(),
                    res.getTitle(),
                    res.getPosterUrl(),
                    defaultRating,
                    res.getReleaseDate(),
                    defaultGenres
            );
            items.add(item);
        }
        return items;
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