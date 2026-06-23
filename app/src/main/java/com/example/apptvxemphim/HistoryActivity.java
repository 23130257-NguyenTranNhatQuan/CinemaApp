package com.example.apptvxemphim;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private List<String> ticketList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        ticketList = new ArrayList<>();
        adapter = new HistoryAdapter(ticketList);
        rvHistory.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadHistoryFromFirebase();
    }

    private void loadHistoryFromFirebase() {
        if (mAuth.getCurrentUser() == null) return;
        String currentUid = mAuth.getCurrentUser().getUid();

        // 1. Lọc bảng Booking lấy những vé của user_id đang đăng nhập
        db.collection("Booking").whereEqualTo("user_id", currentUid).get().addOnSuccessListener(bookingDocs -> {
            ticketList.clear();
            for (QueryDocumentSnapshot booking : bookingDocs) {
                List<String> seats = (List<String>) booking.get("seats");
                String seatStr = seats != null ? String.join(", ", seats) : "";
                long totalPrice = booking.getLong("total_price") != null ? booking.getLong("total_price") : 0;
                String showtimeId = booking.getString("showtime_id");

                if (showtimeId != null) {
                    // 2. Tra cứu rạp chiếu trong bảng Showtime
                    db.collection("Showtime").document(showtimeId).get().addOnSuccessListener(showtimeDoc -> {
                        if (showtimeDoc.exists()) {
                            String cinemaName = showtimeDoc.getString("cinema_name");
                            String movieId = showtimeDoc.getString("movie_id");

                            if (movieId != null) {
                                // 3. Tra cứu Tên phim trong bảng Movie
                                db.collection("Movie").document(movieId).get().addOnSuccessListener(movieDoc -> {
                                    if (movieDoc.exists()) {
                                        String movieTitle = movieDoc.getString("title");

                                        // Gom toàn bộ data lại thành 1 chuỗi để hiển thị
                                        String finalTicket = movieTitle + "\n"
                                                + cinemaName + " - Ghế: " + seatStr + "\n"
                                                + "Tổng tiền: " + totalPrice + " VNĐ";

                                        ticketList.add(finalTicket);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    // Adapter xử lý RecyclerView
    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<String> data;
        public HistoryAdapter(List<String> data) { this.data = data; }

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
            holder.tvMovieTitle.setText(parts[0]); // Tên phim
            if (parts.length > 2) {
                holder.tvSeatInfo.setText(parts[1] + "\n" + parts[2]); // Thông tin rạp, ghế, giá
            }
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMovieTitle, tvSeatInfo;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
                tvSeatInfo = itemView.findViewById(R.id.tvSeatInfo);
            }
        }
    }
}