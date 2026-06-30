package com.example.apptvxemphim;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {

    private List<Booking> bookingList;
    private Context context;

    public AdminBookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.tvOrderId.setText("Mã: " + booking.getOrderId());
        holder.tvStatus.setText(booking.getStatus());
        holder.tvMovieShowtime.setText("Phim: " + booking.getMovieTitle() + "\nSuất: " + booking.getShowTime());
        holder.tvSeatsCombos.setText("Ghế: " + booking.getSeats() + "\nCombo: " + booking.getCombos());

        if ("Đã sử dụng".equals(booking.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.btnCheckIn.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
            holder.btnCheckIn.setVisibility(View.VISIBLE);
        }

        holder.btnCheckIn.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("Booking").document(booking.getOrderId())
                    .update("status", "Đã sử dụng")
                    .addOnSuccessListener(aVoid -> {
                        booking.setStatus("Đã sử dụng");
                        notifyItemChanged(position);
                        Toast.makeText(context, "Đã check-in vé thành công!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() { return bookingList.size(); }

    public void updateList(List<Booking> newList) {
        bookingList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvMovieShowtime, tvSeatsCombos;
        Button btnCheckIn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvMovieShowtime = itemView.findViewById(R.id.tvMovieShowtime);
            tvSeatsCombos = itemView.findViewById(R.id.tvSeatsCombos);
            btnCheckIn = itemView.findViewById(R.id.btnCheckIn);
        }
    }
}