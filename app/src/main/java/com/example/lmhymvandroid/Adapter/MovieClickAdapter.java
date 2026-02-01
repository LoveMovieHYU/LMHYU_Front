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

public class MovieClickAdapter extends RecyclerView.Adapter<MovieClickAdapter.MovieViewHolder> {

    private Context context;
    private List<MovieItem> movieList = new ArrayList<>();

    // 클릭 이벤트를 위한 인터페이스 정의
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

            // 아이템 클릭 시 리스너 호출
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(movieList.get(pos));
                }
            });
        }

        public void bind(MovieItem movie) {
            tvTitle.setText(movie.getTitle());

            // 장르 리스트를 문자열로 변환 (예: "액션, SF")
            if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                tvDesc.setText(TextUtils.join(", ", movie.getGenres()));
            } else {
                tvDesc.setText("장르 정보 없음");
            }

            // Glide로 포스터 이미지 로드
            Glide.with(context)
                    .load(movie.getPosterUrl()) // URL 로드
                    .placeholder(android.R.drawable.ic_menu_gallery) // 로딩 중 이미지
                    .error(android.R.drawable.stat_notify_error) // 에러 시 이미지
                    .into(ivPoster);
        }
    }
}