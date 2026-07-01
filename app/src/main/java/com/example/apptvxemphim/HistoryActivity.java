package com.example.apptvxemphim;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvHistoryTickets;
    private TextView tvEmptyState;

    private TicketAdapter adapter;
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

        adapter = new TicketAdapter(ticketList);
        rvHistoryTickets.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        loadBookingHistory();
    }

    private void loadBookingHistory() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử!", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        db.collection("Booking")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ticketList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 1. ĐỌC TẤT CẢ CÁC BIẾN TỪ FIREBASE
                            String movieTitle = document.getString("movieTitle");
                            String orderId = document.getString("orderId");
                            String hallId = document.getString("hallId");
                            String showTime = document.getString("showTime");
                            String combos = document.getString("combos");
                            String seats = document.getString("seats");
                            String status = document.getString("status");
                            String paymentMethod = document.getString("paymentMethod");
                            Long totalPrice = document.getLong("totalPrice");
                            com.google.firebase.Timestamp createdAt = document.getTimestamp("createdAt");

                            // 2. XỬ LÝ PHÒNG NGỪA DỮ LIỆU TRỐNG (NULL CHECKS)
                            if (movieTitle == null) movieTitle = "Không rõ phim";
                            if (orderId == null) orderId = "---";
                            if (hallId == null) hallId = "---";
                            if (showTime == null) showTime = "---";
                            if (combos == null || combos.isEmpty()) combos = "Không có Combo";
                            if (seats == null) seats = "---";
                            if (status == null) status = "Chưa rõ trạng thái";
                            if (paymentMethod == null) paymentMethod = "Thanh toán";

                            String priceString = (totalPrice != null) ? String.format(Locale.getDefault(), "%,d đ", totalPrice) : "0 đ";

                            String createdAtString = "---";
                            if (createdAt != null) {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                createdAtString = sdf.format(createdAt.toDate());
                            }


                            String formattedTicket = movieTitle + " (Mã đơn: " + orderId + ")\n"
                                    + "Rạp: " + hallId + " - Suất: " + showTime + " | " + combos + " | Đặt lúc: " + createdAtString + "\n"
                                    + "Ghế: " + seats + " (" + status + ") • " + paymentMethod + ": " + priceString;

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
                        Toast.makeText(HistoryActivity.this, "Lỗi tải lịch sử đặt vé!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}