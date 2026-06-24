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
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class AdminMovieAdapter extends RecyclerView.Adapter<AdminMovieAdapter.ViewHolder> {

    private List<Movie> movieList;

    public AdminMovieAdapter(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.tvTitle.setText(movie.getTitle());
        holder.tvDirector.setText("Đạo diễn: " + movie.getDirector());

        Glide.with(holder.itemView.getContext())
                .load(movie.getPoster())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgPoster);

        // Nút Sửa: Chuyển sang màn hình AddEdit và gửi ID phim qua
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddEditMovieActivity.class);
            intent.putExtra("MOVIE_ID", movie.getId());
            v.getContext().startActivity(intent);
        });

        // Nút Xóa: Hiển thị cảnh báo rồi xóa
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Xóa phim")
                    .setMessage("Bạn có chắc muốn xóa phim " + movie.getTitle() + " không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        FirebaseFirestore.getInstance().collection("Movie").document(movie.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    movieList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(v.getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster, btnEdit, btnDelete;
        TextView tvTitle, tvDirector;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_admin_movie_poster);
            tvTitle = itemView.findViewById(R.id.tv_admin_movie_title);
            tvDirector = itemView.findViewById(R.id.tv_admin_movie_director);
            btnEdit = itemView.findViewById(R.id.btn_admin_edit_movie);
            btnDelete = itemView.findViewById(R.id.btn_admin_delete_movie);
        }
    }
}