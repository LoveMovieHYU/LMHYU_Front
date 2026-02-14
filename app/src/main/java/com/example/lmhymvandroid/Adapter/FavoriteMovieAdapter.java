package com.example.lmhymvandroid.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lmhymvandroid.DTO.MovieSummaryResponseDTO;
import com.example.lmhymvandroid.R;

import java.util.List;

public class FavoriteMovieAdapter extends RecyclerView.Adapter<FavoriteMovieAdapter.ViewHolder> {

    private Context context;
    private List<MovieSummaryResponseDTO> movies;

    public FavoriteMovieAdapter(Context context, List<MovieSummaryResponseDTO> movies) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieSummaryResponseDTO movie = movies.get(position);

        holder.tvTitle.setText(movie.getTitle());

        // 포스터 이미지 로드
        String posterUrl = movie.getPosterPath();

        // 만약 서버에서 '/path/to/image.jpg' 처럼 뒷부분만 준다면 아래 주석을 풀어서 사용하세요.
        if (posterUrl != null && !posterUrl.startsWith("http")) {
            posterUrl = "https://image.tmdb.org/t/p/w500" + posterUrl;
        }

        Glide.with(context)
                .load(posterUrl)
                .placeholder(R.color.loading_gray) // 로딩 중 색상
                .error(R.color.loading_gray)       // 에러 시 색상
                .into(holder.ivPoster);
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_poster);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}