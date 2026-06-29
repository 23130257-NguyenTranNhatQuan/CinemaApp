package com.example.apptvxemphim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.List;

public class ShowtimeOverviewAdapter extends RecyclerView.Adapter<ShowtimeOverviewAdapter.ViewHolder> {

    private List<Showtime> showtimeList;
    private FirebaseFirestore db;

    public ShowtimeOverviewAdapter(List<Showtime> showtimeList) {
        this.showtimeList = showtimeList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime_overview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Showtime showtime = showtimeList.get(position);
        
        holder.tvTime.setText(showtime.getTime());
        holder.tvHall.setText("Phòng " + showtime.getHallId());
        
        // Fetch movie name from Movie collection
        loadMovieName(showtime.getMovieId(), holder.tvMovie);
    }

    private void loadMovieName(String movieId, TextView tvMovie) {
        db.collection("Movie")
                .document(movieId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String movieTitle = task.getResult().getString("title");
                        if (movieTitle != null && !movieTitle.isEmpty()) {
                            tvMovie.setText(movieTitle);
                        } else {
                            tvMovie.setText("Phim: " + movieId);
                        }
                    } else {
                        tvMovie.setText("Phim: " + movieId);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return showtimeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvHall, tvMovie;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_showtime_time);
            tvHall = itemView.findViewById(R.id.tv_showtime_hall);
            tvMovie = itemView.findViewById(R.id.tv_showtime_movie);
        }
    }
}
