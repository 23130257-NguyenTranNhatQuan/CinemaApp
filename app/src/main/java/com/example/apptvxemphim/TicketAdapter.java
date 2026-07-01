package com.example.apptvxemphim;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Booking> ticketList;

    public TicketAdapter(List<Booking> ticketList) {
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
        Booking ticket = ticketList.get(position);

        if (ticket == null) return;

        holder.tvMovieTitle.setText(ticket.getMovieTitle() != null ? ticket.getMovieTitle() : "Không có thông tin");
        holder.tvCinemaAndTime.setText(ticket.getShowTime() != null ? ticket.getShowTime() : "Thời gian: ---");
        holder.tvSeatNumber.setText("Ghế: " + (ticket.getSeats() != null ? ticket.getSeats() : "---"));
        holder.tvTicketId.setText(String.format("Tổng: %,d đ", ticket.getTotalPrice()));

        String orderId = ticket.getOrderId();
        if (orderId != null && !orderId.isEmpty()) {
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(orderId, BarcodeFormat.QR_CODE, 300, 300);
                holder.ivTicketQR.setImageBitmap(bitmap);
                holder.ivTicketQR.setVisibility(View.VISIBLE);

                // Nếu vé đã check-in, làm mờ QR đi 70%
                if ("Đã sử dụng".equals(ticket.getStatus())) {
                    holder.ivTicketQR.setAlpha(0.3f);
                } else {
                    holder.ivTicketQR.setAlpha(1.0f);
                }
            } catch (Exception e) {
                e.printStackTrace();
                holder.ivTicketQR.setVisibility(View.GONE);
            }
        } else {
            holder.ivTicketQR.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();

            String detailMessage = "Mã đơn: " + ticket.getOrderId() +
                    "\nPhim: " + ticket.getMovieTitle() +
                    "\nThời gian: " + ticket.getShowTime() +
                    "\nGhế: " + ticket.getSeats() +
                    "\nCombo: " + ticket.getCombos() +
                    "\nThanh toán: " + ticket.getPaymentMethod() +
                    "\nTổng tiền: " + String.format("%,d đ", ticket.getTotalPrice()) +
                    "\nTrạng thái: " + ticket.getStatus();

            new AlertDialog.Builder(context)
                    .setTitle("🎟️ Chi tiết vé phim")
                    .setMessage(detailMessage)
                    .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return ticketList != null ? ticketList.size() : 0;
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieTitle, tvCinemaAndTime, tvSeatNumber, tvTicketId;
        ImageView ivTicketQR;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvCinemaAndTime = itemView.findViewById(R.id.tvCinemaAndTime);
            tvSeatNumber = itemView.findViewById(R.id.tvSeatNumber);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            ivTicketQR = itemView.findViewById(R.id.ivTicketQR);
        }
    }
}