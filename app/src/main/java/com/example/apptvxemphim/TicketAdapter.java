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

        holder.tvMovieTitle.setText(parts.length > 0 ? parts[0].trim() : "Không có thông tin");

        holder.tvCinemaAndTime.setText(parts.length > 1 ? parts[1].trim() : "Thời gian: ---");

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
                    .setMessage(ticketData)
                    .setPositiveButton("Đóng", (dialog, which) -> {
                        dialog.dismiss();
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