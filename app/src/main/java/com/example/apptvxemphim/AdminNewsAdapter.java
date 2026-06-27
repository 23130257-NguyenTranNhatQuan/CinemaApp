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

public class AdminNewsAdapter extends RecyclerView.Adapter<AdminNewsAdapter.ViewHolder> {

    private List<News> newsList;

    public AdminNewsAdapter(List<News> newsList) {
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_news, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.tvTitle.setText(news.getTitle());

        Glide.with(holder.itemView.getContext())
                .load(news.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgPoster);

        // Nút Sửa: Chuyển sang màn hình AddEditNews
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddEditNewsActivity.class);
            intent.putExtra("NEWS_ID", news.getId());
            v.getContext().startActivity(intent);
        });

        // Nút Xóa: Hiển thị cảnh báo rồi xóa
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Xóa tin tức")
                    .setMessage("Bạn có chắc muốn xóa tin tức \"" + news.getTitle() + "\" không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        FirebaseFirestore.getInstance().collection("News").document(news.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    newsList.remove(position);
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
        return newsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster, btnEdit, btnDelete;
        TextView tvTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.img_admin_news_poster);
            tvTitle = itemView.findViewById(R.id.tv_admin_news_title);
            btnEdit = itemView.findViewById(R.id.btn_admin_edit_news);
            btnDelete = itemView.findViewById(R.id.btn_admin_delete_news);
        }
    }
}
