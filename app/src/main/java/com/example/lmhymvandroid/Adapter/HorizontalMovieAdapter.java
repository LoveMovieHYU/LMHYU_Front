package com.example.lmhymvandroid.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lmhymvandroid.DTO.MovieSummaryResponseDTO;
import com.example.lmhymvandroid.R;

import java.util.List;

public class HorizontalMovieAdapter extends RecyclerView.Adapter<HorizontalMovieAdapter.ViewHolder> {

    private Context context;
    private List<MovieSummaryResponseDTO> movies;
    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(MovieSummaryResponseDTO movie);
    }

    public HorizontalMovieAdapter(Context context, List<MovieSummaryResponseDTO> movies, OnItemClickListener listener) {
        this.context = context;
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieSummaryResponseDTO movie = movies.get(position);

        String imageUrl = movie.getPosterPath();

        if (imageUrl != null && !imageUrl.startsWith("http")) {
            imageUrl = "https://image.tmdb.org/t/p/w500" + imageUrl;
        }

        Glide.with(context)
                .load(imageUrl)
                .into(holder.ivPoster);

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_horizontal_poster);
        }
    }
}