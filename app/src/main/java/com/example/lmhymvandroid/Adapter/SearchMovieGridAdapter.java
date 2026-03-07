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
import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.R;

import java.util.ArrayList;
import java.util.List;

public class SearchMovieGridAdapter extends RecyclerView.Adapter<SearchMovieGridAdapter.ViewHolder> {

    private Context context;
    private List<MovieItem> movies = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(MovieItem movie);
    }
    private OnItemClickListener listener;

    public SearchMovieGridAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMovieList(List<MovieItem> list) {
        this.movies = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieItem movie = movies.get(position);

        holder.tvTitle.setText(movie.getTitle());

        String posterUrl = movie.getPosterUrl();
        if (posterUrl != null && !posterUrl.startsWith("http")) {
            posterUrl = "https://image.tmdb.org/t/p/w500" + posterUrl;
        }

        Glide.with(context)
                .load(posterUrl)
                .placeholder(R.color.loading_gray)
                .error(R.color.loading_gray)
                .into(holder.ivPoster);

        // 클릭 시 상세 페이지로 이동하도록 리스너 연결
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(movie);
            }
        });
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