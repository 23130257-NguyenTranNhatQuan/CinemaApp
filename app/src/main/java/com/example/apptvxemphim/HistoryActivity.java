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

import java.util.ArrayList;
import java.util.List;

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

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ View
        btnBack = findViewById(R.id.btnBack);
        rvHistoryTickets = findViewById(R.id.rvHistoryTickets);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Nút quay lại màn hình Profile
        btnBack.setOnClickListener(v -> finish());

        // Cài đặt RecyclerView (Cuộn dọc)
        rvHistoryTickets.setLayoutManager(new LinearLayoutManager(this));
        ticketList = new ArrayList<>();
        adapter = new HistoryTicketAdapter(ticketList);
        rvHistoryTickets.setAdapter(adapter);

        // Tải dữ liệu lịch sử vé
        loadHistoryTickets();
    }

    private void loadHistoryTickets() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            // Truy vấn lấy TẤT CẢ vé của user này
            db.collection("Tickets")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        ticketList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            // Lấy các trường dữ liệu từ Firebase
                            String movieTitle = doc.getString("movieTitle");
                            String cinemaName = doc.getString("cinemaName");
                            String showtime = doc.getString("showtime");
                            String seats = doc.getString("seats");
                            Long totalPrice = doc.getLong("totalPrice");

                            if (movieTitle != null) {
                                // Gộp chuỗi theo cú pháp riêng biệt bằng ký tự \n để Adapter dễ dàng cắt chuỗi
                                // Định dạng: Tên Phim \n Rạp • Thời gian \n Ghế: X \n Tổng tiền: Y
                                String priceStr = (totalPrice != null) ? String.format("%,d VNĐ", totalPrice) : "Đang cập nhật";
                                String cinemaAndTime = (cinemaName != null ? cinemaName : "Rạp") + " • " + (showtime != null ? showtime : "Thời gian");
                                String seatStr = "Ghế: " + (seats != null ? seats : "Trống");
                                String priceInfo = "Tổng tiền: " + priceStr;

                                String fullTicketData = movieTitle + "\n" + cinemaAndTime + "\n" + seatStr + "\n" + priceInfo;
                                ticketList.add(fullTicketData);
                            }
                        }

                        // Nếu không có vé nào, hiển thị text "Chưa có giao dịch"
                        if (ticketList.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            rvHistoryTickets.setVisibility(View.GONE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                            rvHistoryTickets.setVisibility(View.VISIBLE);
                        }

                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(HistoryActivity.this, "Lỗi tải lịch sử vé!", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // =========================================================================
    // ADAPTER HIỂN THỊ VÉ - Tái sử dụng giao diện item_ticket.xml
    // =========================================================================
    private class HistoryTicketAdapter extends RecyclerView.Adapter<HistoryTicketAdapter.ViewHolder> {
        private List<String> data;

        public HistoryTicketAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Dùng chung item_ticket.xml đã thiết kế
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String item = data.get(position);
            String[] parts = item.split("\n");

            // Kiểm tra và đổ dữ liệu chuẩn xác vào các trường của thẻ vé
            if (parts.length > 0) holder.tvMovieTitle.setText(parts[0]);
            if (parts.length > 1) holder.tvCinemaAndTime.setText(parts[1]);

            // Xử lý ẩn/hiện nếu thiếu thông tin
            if (parts.length > 2) {
                holder.tvSeatNumber.setText(parts[2]);
            } else {
                holder.tvSeatNumber.setText("Ghế: Đang cập nhật");
            }

            if (parts.length > 3) {
                holder.tvTicketId.setText(parts[3]);
            } else {
                holder.tvTicketId.setText("Tổng tiền: ---");
            }
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMovieTitle, tvCinemaAndTime, tvSeatNumber, tvTicketId;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // Các ID này dựa trên thiết kế item_ticket.xml siêu đẹp ở bước trước
                tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
                tvCinemaAndTime = itemView.findViewById(R.id.tvCinemaAndTime);
                tvSeatNumber = itemView.findViewById(R.id.tvSeatNumber);
                tvTicketId = itemView.findViewById(R.id.tvTicketId);
            }
        }
    }
}