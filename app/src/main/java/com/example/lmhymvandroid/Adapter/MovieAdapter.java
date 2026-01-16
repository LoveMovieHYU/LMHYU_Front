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

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<MovieSummaryResponseDTO> movieList;
    private Context context;

    public MovieAdapter(Context context, List<MovieSummaryResponseDTO> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieSummaryResponseDTO movie = movieList.get(position);
        holder.title.setText(movie.getTitle());
        holder.rating.setText("★ " + movie.getRating());

        // TMDB 이미지 기본 URL (필요 시 수정)
        String imageUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background) // 로딩 중 이미지
                .error(R.drawable.ic_launcher_background)       // 에러 시 이미지
                .into(holder.poster);
    }

    @Override
    public int getItemCount() { return movieList.size(); }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, rating;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.item_movie_poster);
            title = itemView.findViewById(R.id.item_movie_title);
            rating = itemView.findViewById(R.id.item_movie_rating);
        }
    }
}