package com.example.lmhymvandroid.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lmhymvandroid.DTO.Diary;
import com.example.lmhymvandroid.R;

import java.util.ArrayList;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {

    private ArrayList<Diary> diaryList;

    public DiaryAdapter(ArrayList<Diary> diaryList) {
        this.diaryList = diaryList;
    }

    public void setDiaryList(ArrayList<Diary> diaryList) {
        this.diaryList = diaryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Diary item = diaryList.get(position);

        // 1. 날짜 포맷 변경 ("2026-01-03" -> "1월 3일")
        try {
            String[] parts = item.date.split("-");
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            holder.tvDate.setText(month + "월 " + day + "일");
        } catch (Exception e) {
            holder.tvDate.setText(item.date);
        }

        // 2. 제목 설정
        holder.tvTitle.setText(item.title);

        // 3. 감정 설정 (영어 -> 한글+이모지 변환)
        String emotionDisplay = "😊 행복함";
        if (item.emotion != null) {
            switch (item.emotion) {
                case "Happy": emotionDisplay = "😊 행복함"; break;
                case "Sad": emotionDisplay = "😢 슬픔"; break;
                case "Angry": emotionDisplay = "😠 화남"; break;
                case "Scared": emotionDisplay = "😨 무서움"; break;
                case "Sleepy": emotionDisplay = "😴 지루함"; break;
                default: emotionDisplay = "😐 보통"; break;
            }
        }
        holder.tvEmotion.setText(emotionDisplay);
    }

    @Override
    public int getItemCount() {
        return diaryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvTitle;
        TextView tvEmotion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvEmotion = itemView.findViewById(R.id.tvItemEmotion);
        }
    }
}