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

public class ComingSoonMovieAdapter extends RecyclerView.Adapter<ComingSoonMovieAdapter.ComingSoonViewHolder> {
    private List<Movie> movieList;

    public ComingSoonMovieAdapter(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public ComingSoonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coming_soon, parent, false);
        return new ComingSoonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComingSoonViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        if (movie == null) return;

        holder.tvTitle.setText(movie.getTitle());

        // Get formatted release date from Timestamp
        String releaseDateStr = movie.getFormattedReleaseDate();
        if (!releaseDateStr.isEmpty()) {
            holder.tvReleaseDate.setText("Khởi chiếu: " + releaseDateStr);
        } else {
            holder.tvReleaseDate.setText("Sắp chiếu");
        }

        Glide.with(holder.itemView.getContext())
                .load(movie.getPoster())
                .placeholder(android.R.drawable.ic_menu_report_image)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imgPoster);

        // Click to open detail page
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ComingSoonDetailActivity.class);
            intent.putExtra("MOVIE_ID", movie.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    public static class ComingSoonViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle;
        TextView tvReleaseDate;

        public ComingSoonViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_poster);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvReleaseDate = itemView.findViewById(R.id.tv_release_date);
        }
    }
}
