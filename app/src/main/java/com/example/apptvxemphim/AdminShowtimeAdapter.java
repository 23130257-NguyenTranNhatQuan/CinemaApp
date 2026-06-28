package com.example.apptvxemphim;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Map;

public class AdminShowtimeAdapter extends RecyclerView.Adapter<AdminShowtimeAdapter.ViewHolder> {

    private List<Showtime> list;
    private Map<String, String> movieNames;
    private Map<String, String> cinemaNames;
    private Map<String, String> hallNames;
    private Map<String, String> movieFormats; // movieId -> format

    public AdminShowtimeAdapter(List<Showtime> list,
                                Map<String, String> movieNames,
                                Map<String, String> cinemaNames,
                                Map<String, String> hallNames,
                                Map<String, String> movieFormats) {
        this.list = list;
        this.movieNames = movieNames;
        this.cinemaNames = cinemaNames;
        this.hallNames = hallNames;
        this.movieFormats = movieFormats;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_showtime, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Showtime st = list.get(position);

        h.tvMovie.setText(movieNames.getOrDefault(st.getMovieId(), "Phim?"));
        h.tvCinemaHall.setText(
                cinemaNames.getOrDefault(st.getCinemaId(), "Rạp?") + " - " +
                        hallNames.getOrDefault(st.getHallId(), "Phòng?")
        );
        h.tvDatetime.setText(st.getDate() + " | " + st.getTime());

        String fmt = movieFormats.getOrDefault(st.getMovieId(), "");
        String prefix = (fmt != null && !fmt.isEmpty()) ? fmt + " " : "";
        h.tvLang.setText(prefix + (st.getLanguage() != null ? st.getLanguage() : ""));

        h.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddEditShowtimeActivity.class);
            intent.putExtra("SHOWTIME_ID", st.getShowtimeId());
            v.getContext().startActivity(intent);
        });

        h.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Xóa suất chiếu")
                    .setMessage("Xóa suất " + st.getTime() + " ngày " + st.getDate() + "?")
                    .setPositiveButton("Xóa", (d, w) -> {
                        int pos = h.getAdapterPosition();
                        FirebaseFirestore.getInstance()
                                .collection("Showtime")
                                .document(st.getShowtimeId())
                                .delete()
                                .addOnSuccessListener(a -> {
                                    list.remove(pos);
                                    notifyItemRemoved(pos);
                                    Toast.makeText(v.getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovie, tvCinemaHall, tvDatetime, tvLang;
        ImageView btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvMovie      = v.findViewById(R.id.tv_showtime_movie);
            tvCinemaHall = v.findViewById(R.id.tv_showtime_cinema_hall);
            tvDatetime   = v.findViewById(R.id.tv_showtime_datetime);
            tvLang       = v.findViewById(R.id.tv_showtime_lang);
            btnEdit      = v.findViewById(R.id.btn_edit_showtime);
            btnDelete    = v.findViewById(R.id.btn_delete_showtime);
        }
    }
}