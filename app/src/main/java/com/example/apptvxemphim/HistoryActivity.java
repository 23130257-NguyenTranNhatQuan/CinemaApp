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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvHistoryTickets;
    private TextView tvEmptyState;

    private TicketAdapter adapter;
    private List<Booking> ticketList;

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

        loadHistoryData();
    }

    private void loadHistoryData() {
        if (mAuth.getCurrentUser() == null) return;

        String currentUserId = mAuth.getCurrentUser().getUid();

        // Lấy dữ liệu và sắp xếp vé mới nhất lên đầu
        db.collection("Booking")
                .whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        ticketList.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            //Firebase tự động biến dữ liệu JSON thành object Booking
                            Booking booking = doc.toObject(Booking.class);
                            ticketList.add(booking);
                        }

                        // Cập nhật giao diện Trống/Có vé
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