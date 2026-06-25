package com.example.apptvxemphim;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MovieCarouselAdapter extends RecyclerView.Adapter<MovieCarouselAdapter.MovieCarouselViewHolder> {
    private List<Movie> movieList;

    public MovieCarouselAdapter(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MovieCarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie_carousel, parent, false);
        return new MovieCarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieCarouselViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        if (movie == null) return;

        holder.tvTitle.setText(movie.getTitle());

        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            String genresStr = android.text.TextUtils.join(", ", movie.getGenres());
            holder.tvGenre.setText(genresStr);
        } else {
            holder.tvGenre.setText("Đang cập nhật");
        }

        if (movie.getFormat() != null && !movie.getFormat().isEmpty()) {
            holder.tvFormat.setText(movie.getFormat());
        } else {
            holder.tvFormat.setText("2D");
        }
        if (movie.getAge() != null && !movie.getAge().isEmpty()) {
            holder.tvAgeRating.setText(movie.getAge());
        } else {
            holder.tvAgeRating.setText("K");
        }

        Glide.with(holder.itemView.getContext())
                .load(movie.getPoster())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imgPoster);

        // Click on the whole card -> go to detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MovieDetailActivity.class);
            intent.putExtra("MOVIE_ID", movie.getId());
            v.getContext().startActivity(intent);
        });

        // Click on "ĐẶT VÉ" button -> go to booking page
        holder.btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ShowtimeSelectionActivity.class);
            intent.putExtra("MOVIE_ID", movie.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    public static class MovieCarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle;
        TextView tvGenre;
        TextView tvAgeRating;
        TextView tvFormat;
        androidx.appcompat.widget.AppCompatButton btnBook;

        public MovieCarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_poster);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvGenre = itemView.findViewById(R.id.tv_genre);
            tvAgeRating = itemView.findViewById(R.id.tv_age_rating);
            tvFormat = itemView.findViewById(R.id.tv_format);
            btnBook = itemView.findViewById(R.id.btn_book);
        }
    }
}