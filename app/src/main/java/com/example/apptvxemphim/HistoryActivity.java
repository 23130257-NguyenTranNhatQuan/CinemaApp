package com.example.apptvxemphim;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvHistoryTickets;
    private TextView tvEmptyState;

    private HistoryTicketAdapter adapter;
    private List<String> ticketList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        rvHistoryTickets = findViewById(R.id.rvHistoryTickets);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvHistoryTickets.setLayoutManager(new LinearLayoutManager(this));
        ticketList = new ArrayList<>();
        adapter = new HistoryTicketAdapter(ticketList);
        rvHistoryTickets.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadBookingHistory();
    }

    private void loadBookingHistory() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        // 1. Truy vấn đúng Collection "Booking" và lọc theo "user_id" như trong ảnh
        db.collection("Booking")
                .whereEqualTo("user_id", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ticketList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 2. Lấy ĐÚNG các trường dữ liệu có trong ảnh Firestore của bạn

                            // Lấy showtime_id (Vì không có Tên phim)
                            String showtimeId = document.getString("showtime_id");
                            if (showtimeId == null) showtimeId = "Không rõ mã suất chiếu";

                            // Lấy booking_time (Định dạng lại ngày giờ)
                            com.google.firebase.Timestamp timestamp = document.getTimestamp("booking_time");
                            String timeString = "Không rõ thời gian";
                            if (timestamp != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                timeString = sdf.format(timestamp.toDate());
                            }

                            // Lấy mảng seats và nối lại thành chuỗi
                            List<String> seatsArray = (List<String>) document.get("seats");
                            String seatsString = "Chưa chọn";
                            if (seatsArray != null && !seatsArray.isEmpty()) {
                                seatsString = android.text.TextUtils.join(", ", seatsArray);
                            }

                            // Lấy total_price
                            Long totalPrice = document.getLong("total_price");
                            String priceString = (totalPrice != null) ? String.format(Locale.getDefault(), "%,d đ", totalPrice) : "0 đ";

                            // 3. Đóng gói lại thành chuỗi 4 dòng để đẩy vào Adapter
                            String formattedTicket = "Mã suất chiếu: " + showtimeId + "\n"
                                    + "Ngày đặt: " + timeString + "\n"
                                    + "Ghế: " + seatsString + "\n"
                                    + "Tổng tiền: " + priceString;

                            ticketList.add(formattedTicket);
                        }

                        if (ticketList.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            rvHistoryTickets.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            rvHistoryTickets.setVisibility(View.VISIBLE);
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(HistoryActivity.this, "Lỗi tải lịch sử!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    class HistoryTicketAdapter extends RecyclerView.Adapter<HistoryTicketAdapter.ViewHolder> {
        private final List<String> data;

        public HistoryTicketAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String item = data.get(position);
            String[] parts = item.split("\n");

            if (parts.length > 0) holder.tvMovieTitle.setText(parts[0]);     // Hiện: Mã suất chiếu...
            if (parts.length > 1) holder.tvCinemaAndTime.setText(parts[1]);  // Hiện: Ngày đặt...
            if (parts.length > 2) holder.tvSeatNumber.setText(parts[2]);     // Hiện: Ghế...
            if (parts.length > 3) holder.tvTicketId.setText(parts[3]);       // Hiện: Tổng tiền...
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMovieTitle, tvCinemaAndTime, tvSeatNumber, tvTicketId;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
                tvCinemaAndTime = itemView.findViewById(R.id.tvCinemaAndTime);
                tvSeatNumber = itemView.findViewById(R.id.tvSeatNumber);
                tvTicketId = itemView.findViewById(R.id.tvTicketId);
            }
        }
    }
}