package com.example.lmhymvandroid.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.Adapter.MovieSearchAdapter;
import com.example.lmhymvandroid.DTO.MovieSearchDTO;
import com.example.lmhymvandroid.R;
import com.example.lmhymvandroid.Service.MovieApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {

    private RecyclerView rvMovieList;
    private MovieSearchAdapter adapter;
    private EditText etSearchBar;
    private MovieApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // 1. 뷰 초기화
        etSearchBar = view.findViewById(R.id.et_search_bar);
        rvMovieList = view.findViewById(R.id.rv_movie_list);

        // 2. 리사이클러뷰 설정
        setupRecyclerView();

        // 3. Retrofit 초기화 (Base URL은 실제 서버 주소로 변경 필요)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://your-server-ip:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(MovieApiService.class);

        // 4. 검색 이벤트 리스너 (키보드 돋보기 버튼 클릭 시)
        etSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = etSearchBar.getText().toString().trim();
                    if (!keyword.isEmpty()) {
                        performSearch(keyword);
                    }
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    private void setupRecyclerView() {
        adapter = new MovieSearchAdapter();
        rvMovieList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMovieList.setAdapter(adapter);

        // 상세 페이지 이동 로직
        adapter.setOnItemClickListener(new MovieSearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Long movieId) {
                // MovieDetailActivity로 이동하면서 movieId 전달
                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                intent.putExtra("movieId", movieId);
                startActivity(intent);
            }
        });
    }

    private void performSearch(String keyword) {
        // API 호출 (페이지는 일단 1로 고정)
        apiService.searchMovies(keyword, 1).enqueue(new Callback<List<MovieSearchDTO>>() {
            @Override
            public void onResponse(Call<List<MovieSearchDTO>> call, Response<List<MovieSearchDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieSearchDTO> movies = response.body();
                    // 어댑터에 데이터 업데이트
                    adapter.setMovieList(movies);

                    if(movies.isEmpty()){
                        Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "검색 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<MovieSearchDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}