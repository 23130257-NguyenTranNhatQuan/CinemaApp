package com.example.apptvxemphim;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageCinemasActivity extends AppCompatActivity {

    private RecyclerView rcvCinemas;
    private AdminCinemaAdapter adapter;
    private List<Cinema> cinemaList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cinemas);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btn_back_admin_cinema).setOnClickListener(v -> finish());

        rcvCinemas = findViewById(R.id.rcv_admin_cinemas);
        cinemaList = new ArrayList<>();
        adapter = new AdminCinemaAdapter(cinemaList);
        rcvCinemas.setLayoutManager(new LinearLayoutManager(this));
        rcvCinemas.setAdapter(adapter);

        // Load danh sách rạp từ Firebase
        db.collection("Cinema").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            cinemaList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Cinema cinema = doc.toObject(Cinema.class);
                    cinemaList.add(cinema);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}