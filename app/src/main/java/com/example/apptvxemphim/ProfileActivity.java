package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName;
    private Button btnViewHistory;
    private Button btnLogout;
    private BottomNavigationView bottomNavigationView;

    // Khai báo RecyclerView và Adapter cho danh sách vé gần đây
    private RecyclerView rvRecentTickets;
    private List<String> recentTicketList;
    private RecentTicketAdapter recentTicketAdapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 1. Ánh xạ các View từ XML
        tvName = findViewById(R.id.tvName);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        rvRecentTickets = findViewById(R.id.rvRecentTickets);

        // 2. Cấu hình RecyclerView cuộn theo chiều ngang (HORIZONTAL)
        rvRecentTickets.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recentTicketList = new ArrayList<>();
        recentTicketAdapter = new RecentTicketAdapter(recentTicketList);
        rvRecentTickets.setAdapter(recentTicketAdapter);

        // 3. Tải dữ liệu từ Firestore
        loadUserProfile();
        loadRecentTickets();

        // 4. Xử lý nút xem lịch sử đặt vé chi tiết
        btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // 5. Xử lý nút đăng xuất tài khoản
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ProfileActivity.this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 6. Xử lý thanh điều hướng Bottom Navigation ở đáy màn hình
        bottomNavigationView.setSelectedItemId(R.id.nav_account); // Chuyển thành nav_account
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) { // Chuyển thành nav_home
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;

                } else if (id == R.id.nav_news) { // Chuyển thành nav_news
                    startActivity(new Intent(ProfileActivity.this, NewsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;

                } else if (id == R.id.nav_account) { // Chuyển thành nav_account
                    // Đang ở trang Tài khoản (Profile) rồi nên không làm gì cả
                    return true;
                }

                // Lưu ý: Nếu bạn có trang Mua vé (nav_ticket), bạn có thể thêm else if (id == R.id.nav_ticket) ở đây
                return false;
            }
        });
    }

    // Hàm lấy Tên người dùng từ Firestore
    private void loadUserProfile() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            db.collection("User").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("full_name");
                            if (fullName != null) {
                                tvName.setText(fullName);
                            } else {
                                tvName.setText("Khách hàng");
                            }
                        } else {
                            tvName.setText("Khách hàng mới");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Lỗi tải thông tin!", Toast.LENGTH_SHORT).show();
                        tvName.setText("Lỗi kết nối");
                    });
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    // Hàm tải danh sách vé phim gần đây
    private void loadRecentTickets() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            db.collection("Tickets")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        recentTicketList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String movieTitle = doc.getString("movieTitle");
                            String showtime = doc.getString("showtime");
                            String seat = doc.getString("seats"); // Hoặc trường lưu ghế trong DB của bạn

                            if (movieTitle != null) {
                                // Định dạng chuỗi gồm 3 phần phân tách bằng dấu xuống dòng \n giống HistoryActivity
                                String ticketInfo = movieTitle + "\n" + (showtime != null ? showtime : "Đang cập nhật") + "\nGhế: " + (seat != null ? seat : "");
                                recentTicketList.add(ticketInfo);
                            }
                        }

                        // Nếu chưa mua vé nào, tạo dữ liệu ảo để kiểm tra giao diện hiển thị
                        if (recentTicketList.isEmpty()) {
                            recentTicketList.add("Lật Mặt 7\n19:30 - CGV Hùng Vương\nGhế: H5, H6");
                            recentTicketList.add("Doraemon\n14:15 - Galaxy Nguyễn Du\nGhế: C12");
                        }

                        // Cập nhật lại giao diện RecyclerView sau khi có dữ liệu
                        recentTicketAdapter.notifyDataSetChanged();
                    });
        }
    }


    private class RecentTicketAdapter extends RecyclerView.Adapter<RecentTicketAdapter.ViewHolder> {
        private List<String> data;

        public RecentTicketAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Sử dụng chung layout item_ticket
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String item = data.get(position);
            String[] parts = item.split("\n");

            // Đổ dữ liệu vào các TextView mới
            if (parts.length > 0) {
                holder.tvMovieTitle.setText(parts[0]); // Tên phim
            }
            if (parts.length > 1) {
                holder.tvCinemaAndTime.setText(parts[1]); // Cụm rạp và giờ chiếu
            }
            if (parts.length > 2) {
                holder.tvSeatNumber.setText(parts[2]); // Ghế ngồi
            } else {
                holder.tvSeatNumber.setText("Ghế: Đang cập nhật");
            }

            // Vì ở trang Profile (vé xem nhanh) không cần hiện mã vé hay tổng tiền, ta có thể ẩn nó đi
            holder.tvTicketId.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return data != null ? data.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            // Khai báo đúng các ID mới
            TextView tvMovieTitle, tvCinemaAndTime, tvSeatNumber, tvTicketId;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // Ánh xạ chính xác các ID có trong file item_ticket.xml hiện tại
                tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
                tvCinemaAndTime = itemView.findViewById(R.id.tvCinemaAndTime);
                tvSeatNumber = itemView.findViewById(R.id.tvSeatNumber);
                tvTicketId = itemView.findViewById(R.id.tvTicketId);
            }
        }
    }
}