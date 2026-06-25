package com.example.apptvxemphim;

import android.content.Context;
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

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private List<News> newsList;
    private Context context;

    public NewsAdapter(List<News> newsList, Context context) {
        this.newsList = newsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // LƯU Ý: Bạn cần tạo file layout 'item_news.xml' để hiển thị đẹp như web
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        News news = newsList.get(position);

        holder.tvTitle.setText(news.title);

        // Sử dụng Glide để load ảnh từ Firebase URL
        Glide.with(context)
                .load(news.imageUrl)
                .placeholder(R.drawable.ic_launcher_background) // Ảnh mặc định khi đang tải
                .error(R.drawable.ic_launcher_foreground)       // Ảnh khi lỗi
                .into(holder.imgPoster);

        // Sự kiện click để xem chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NewsDetailActivity.class);
            intent.putExtra("title", news.title);
            intent.putExtra("imageUrl", news.imageUrl);
            intent.putExtra("content", news.content);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster); // Cần có ID này trong item_news.xml
            tvTitle = itemView.findViewById(R.id.tvTitle);     // Cần có ID này trong item_news.xml
        }
    }
}