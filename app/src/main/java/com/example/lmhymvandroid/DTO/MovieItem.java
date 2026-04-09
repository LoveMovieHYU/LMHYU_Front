package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class MovieItem implements Serializable {

    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    // 서버는 posterPath로 보내지만, 안드로이드에서는 posterUrl로 사용하겠다고 선언
    @SerializedName("posterPath")
    private String posterUrl;

    @SerializedName("releaseDate")
    private String releaseDate;

    @SerializedName("voteAverage")
    private double rating;

    @SerializedName("popularity")
    private double popularity;

    @SerializedName("genres")
    private List<GenreItem> genres;

    // ================= Getters =================
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getPosterUrl() { return posterUrl; }
    public String getReleaseDate() { return releaseDate; }
    public double getRating() { return rating; }
    public List<GenreItem> getGenres() { return genres; }

    public MovieItem(int id, String title, String posterUrl, double rating, String releaseDate, List<GenreItem> genres) {
        this.id = id;
        this.title = title;
        this.posterUrl = posterUrl;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.genres = genres;
    }

    // ================= 내부 클래스 (장르) =================
    public static class GenreItem implements Serializable {
        @SerializedName("genre")
        private String genre;

        public String getGenre() { return genre; }
    }
}