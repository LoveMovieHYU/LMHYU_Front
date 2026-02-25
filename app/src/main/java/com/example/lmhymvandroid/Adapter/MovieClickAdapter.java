package com.example.lmhymvandroid.Adapter;

import android.content.Context;
import android.text.TextUtils;
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
import java.util.Locale;

public class MovieClickAdapter extends RecyclerView.Adapter<MovieClickAdapter.MovieViewHolder> {

    private Context context;
    private List<MovieItem> movieList = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(MovieItem movie);
    }

    private OnItemClickListener listener;

    public MovieClickAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMovieList(List<MovieItem> list) {
        this.movieList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_recommend, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieItem movie = movieList.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle, tvDesc;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.iv_movie_poster);
            tvTitle = itemView.findViewById(R.id.tv_movie_title);
            tvDesc = itemView.findViewById(R.id.tv_movie_desc);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(movieList.get(pos));
                }
            });
        }

        public void bind(MovieItem movie) {
            tvTitle.setText(movie.getTitle());

            String genreText = "";
            if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                List<String> genreNames = new ArrayList<>();
                for (int i = 0; i < movie.getGenres().size(); i++) {
                    genreNames.add(movie.getGenres().get(i).getGenre());
                }
                genreText = TextUtils.join(", ", genreNames);
            } else {
                genreText = "장르 정보 없음";
            }

            String year = movie.getReleaseDate() != null ? movie.getReleaseDate() : "-";

            String infoText = String.format(Locale.getDefault(), "%s - %s - ★%.1f",
                    genreText, year, movie.getRating());
            tvDesc.setText(infoText);

            String imageUrl = movie.getPosterUrl();
            if (imageUrl != null && !imageUrl.startsWith("http")) {
                imageUrl = "https://image.tmdb.org/t/p/w500" + imageUrl;
            }

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(ivPoster);
        }
    }
}