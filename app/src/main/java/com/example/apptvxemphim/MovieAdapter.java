package com.example.apptvxemphim;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private List<Movie> movieList;

    public MovieAdapter(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        if (movie == null) return;

        holder.tvTitle.setText(movie.getTitle());
        
        // Format genre and duration
        String genreDuration = movie.getGenre() + " • " + formatDuration(movie.getDuration());
        holder.tvGenreDuration.setText(genreDuration);
        
        // Set age rating
        holder.tvAgeRating.setText(movie.getAgeRating());
        
        // Set format (2D/3D) - you can add this field to Movie class if needed
        holder.tvFormat.setText("2D");

        Glide.with(holder.itemView.getContext())
                .load(movie.getPoster())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imgPoster);

        // Set click listener for book button
        holder.btnBook.setOnClickListener(v -> {
            // Handle book ticket click
            // You can start SeatSelectionActivity here
        });
    }

    private String formatDuration(long duration) {
        // Assuming duration is in minutes
        long hours = duration / 60;
        long minutes = duration % 60;
        if (hours > 0) {
            return hours + " giờ " + minutes + " phút";
        }
        return minutes + " phút";
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle;
        TextView tvGenreDuration;
        TextView tvAgeRating;
        TextView tvFormat;
        Button btnBook;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_poster);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvGenreDuration = itemView.findViewById(R.id.tv_genre_duration);
            tvAgeRating = itemView.findViewById(R.id.tv_age_rating);
            tvFormat = itemView.findViewById(R.id.tv_format);
            btnBook = itemView.findViewById(R.id.btn_book);
        }
    }
}