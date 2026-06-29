package com.example.apptvxemphim;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<String> ticketList;

    public TicketAdapter(List<String> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        String ticketData = ticketList.get(position);

        if (ticketData == null || ticketData.isEmpty()) {
            return;
        }

        String[] parts = ticketData.split("\n");

        // 1. Ô Tên Phim (Dòng 1)
        holder.tvMovieTitle.setText(parts.length > 0 ? parts[0].trim() : "Không có thông tin");

        // 2. Ô Thời gian & Rạp (Dòng 2)
        holder.tvCinemaAndTime.setText(parts.length > 1 ? parts[1].trim() : "Thời gian: ---");

        // 3. Xử lý Dòng 3 (Ghế và Giá tiền)
        if (parts.length > 2) {
            String seatAndPriceSection = parts[2];

            if (seatAndPriceSection.contains("•")) {
                String[] seatAndPrice = seatAndPriceSection.split("•");

                holder.tvSeatNumber.setText(seatAndPrice.length > 0 ? seatAndPrice[0].trim() : "Ghế: ---");

                holder.tvTicketId.setText(seatAndPrice.length > 1 ? seatAndPrice[1].trim() : "Tổng: 0đ");
            } else {
                holder.tvSeatNumber.setText(seatAndPriceSection.trim());
                holder.tvTicketId.setText("");
            }
        } else {
            holder.tvSeatNumber.setText("Ghế: ---");
            holder.tvTicketId.setText("Tổng: 0đ");
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();

            // Tạo Hộp thoại Popup (AlertDialog)
            new AlertDialog.Builder(context)
                    .setTitle("🎟️ Chi tiết vé phim")
                    // Hiển thị toàn bộ chuỗi ticketData (chứa tất cả thông tin)
                    .setMessage(ticketData)
                    .setPositiveButton("Đóng", (dialog, which) -> {
                        dialog.dismiss(); // Tắt hộp thoại khi ấn Đóng
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvCinemaAndTime, tvSeatNumber, tvTicketId;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvCinemaAndTime = itemView.findViewById(R.id.tvCinemaAndTime);
            tvSeatNumber = itemView.findViewById(R.id.tvSeatNumber);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
        }
    }
}