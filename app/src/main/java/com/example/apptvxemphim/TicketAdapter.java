package com.example.apptvxemphim;

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
        // Trỏ vào file item_ticket.xml mà bạn vừa tạo
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        String ticketData = ticketList.get(position);

        // Cắt dữ liệu theo định dạng "\n" đã tạo ở ProfileActivity
        String[] parts = ticketData.split("\n");

        if (parts.length > 0) {
            holder.tvMovieTitle.setText(parts[0]); // Tên phim
        }
        if (parts.length > 1) {
            holder.tvCinemaAndTime.setText(parts[1]); // Rạp và Thời gian
        }
        if (parts.length > 2) {
            // Chuỗi ví dụ: "Ghế: G11 • Tổng: 100,000đ"
            String[] seatAndPrice = parts[2].split("•");
            if(seatAndPrice.length > 0) holder.tvSeatNumber.setText(seatAndPrice[0].trim());
            if(seatAndPrice.length > 1) holder.tvTicketId.setText(seatAndPrice[1].trim());
        }
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvCinemaAndTime, tvSeatNumber, tvTicketId;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ĐÚNG các ID mới từ item_ticket.xml
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvCinemaAndTime = itemView.findViewById(R.id.tvCinemaAndTime);
            tvSeatNumber = itemView.findViewById(R.id.tvSeatNumber);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
        }
    }
}