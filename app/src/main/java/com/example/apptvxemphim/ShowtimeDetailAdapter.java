package com.example.apptvxemphim;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ShowtimeDetailAdapter extends RecyclerView.Adapter<ShowtimeDetailAdapter.ShowtimeViewHolder> {
    private List<Showtime> showtimeList;

    public ShowtimeDetailAdapter(List<Showtime> showtimeList) {
        this.showtimeList = showtimeList;
    }

    @NonNull
    @Override
    public ShowtimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_showtime_detail, parent, false);
        return new ShowtimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowtimeViewHolder holder, int position) {
        Showtime showtime = showtimeList.get(position);
        if (showtime == null) return;

        holder.tvCinemaName.setText(showtime.getCinema_name());
        holder.tvTimeDate.setText(showtime.getTime() + " | " + showtime.getDate() + " | " + showtime.getPrice() + "đ");

        // Khi bấm Chọn ghế, chuyển thẳng sang màn hình Đặt ghế (kèm theo ID của suất chiếu)
        holder.btnBookTicket.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SeatSelectionActivity.class);
            intent.putExtra("SHOWTIME_ID", showtime.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return showtimeList != null ? showtimeList.size() : 0;
    }

    public static class ShowtimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvCinemaName, tvTimeDate;
        Button btnBookTicket;

        public ShowtimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCinemaName = itemView.findViewById(R.id.tv_item_cinema_name);
            tvTimeDate = itemView.findViewById(R.id.tv_item_time_date);
            btnBookTicket = itemView.findViewById(R.id.btn_book_ticket);
        }
    }
}
