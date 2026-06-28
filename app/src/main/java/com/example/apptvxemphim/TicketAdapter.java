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
        // Nạp file giao diện item_ticket.xml của bạn
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        String ticketData = ticketList.get(position);

        if (ticketData == null || ticketData.isEmpty()) {
            return;
        }

        // Tách dữ liệu theo ký tự xuống dòng "\n"
        String[] parts = ticketData.split("\n");

        // Dòng 1: Tên Phim hoặc Mã suất chiếu
        if (parts.length > 0) {
            holder.tvMovieTitle.setText(parts[0].trim());
        } else {
            holder.tvMovieTitle.setText("Không có thông tin");
        }

        // Dòng 2: Rạp & Giờ chiếu hoặc Ngày đặt vé
        if (parts.length > 1) {
            holder.tvCinemaAndTime.setText(parts[1].trim());
        } else {
            holder.tvCinemaAndTime.setText("Thời gian: ---");
        }

        // Dòng 3: Chứa thông tin Ghế và Giá tiền (Được phân tách bởi dấu chấm tròn •)
        if (parts.length > 2) {
            String seatAndPriceSection = parts[2];

            if (seatAndPriceSection.contains("•")) {
                // Tách đôi chuỗi tại vị trí dấu •
                String[] seatAndPrice = seatAndPriceSection.split("•");

                // Hiển thị danh sách ghế số ở phần bên trái dấu •
                if (seatAndPrice.length > 0) {
                    holder.tvSeatNumber.setText(seatAndPrice[0].trim());
                } else {
                    holder.tvSeatNumber.setText("Ghế: ---");
                }

                // Hiển thị số tiền/mã vé ở phần bên phải dấu •
                if (seatAndPrice.length > 1) {
                    holder.tvTicketId.setText(seatAndPrice[1].trim());
                } else {
                    holder.tvTicketId.setText("Tổng: 0đ");
                }
            } else {
                // Phòng trường hợp chuỗi dòng 3 không chứa dấu • thì hiển thị toàn bộ vào ô Số ghế
                holder.tvSeatNumber.setText(seatAndPriceSection.trim());
                holder.tvTicketId.setText("");
            }
        } else {
            holder.tvSeatNumber.setText("Ghế: ---");
            holder.tvTicketId.setText("Tổng: 0đ");
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
            // Ánh xạ chính xác các ID thành phần từ file xml item_ticket
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvCinemaAndTime = itemView.findViewById(R.id.tvCinemaAndTime);
            tvSeatNumber = itemView.findViewById(R.id.tvSeatNumber);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
        }
    }
}