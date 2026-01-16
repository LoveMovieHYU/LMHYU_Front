package com.example.lmhymvandroid.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lmhymvandroid.DTO.GenreSectionResponseDTO;
import com.example.lmhymvandroid.R;
import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {
    private List<GenreSectionResponseDTO> sectionList;
    private Context context;

    public GenreAdapter(Context context, List<GenreSectionResponseDTO> sectionList) {
        this.context = context;
        this.sectionList = sectionList;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_genre_section, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        GenreSectionResponseDTO section = sectionList.get(position);
        holder.genreTitle.setText(section.getGenreName());

        // 내부 리사이클러뷰 설정 (가로 스크롤)
        MovieAdapter movieAdapter = new MovieAdapter(context, section.getMovies());
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(movieAdapter);
    }

    @Override
    public int getItemCount() { return sectionList.size(); }

    public static class GenreViewHolder extends RecyclerView.ViewHolder {
        TextView genreTitle;
        RecyclerView recyclerView;

        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            genreTitle = itemView.findViewById(R.id.item_genre_title);
            recyclerView = itemView.findViewById(R.id.item_genre_recycler_view);
        }
    }
}