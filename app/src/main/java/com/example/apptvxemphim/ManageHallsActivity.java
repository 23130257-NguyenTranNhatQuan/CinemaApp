package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageHallsActivity extends AppCompatActivity {

    private RecyclerView rcvHalls;
    private AdminHallAdapter adapter;
    private List<Hall> hallList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_halls);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btn_back_admin_hall).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_hall).setOnClickListener(v ->
                startActivity(new Intent(this, HallCreateActivity.class)));

        rcvHalls = findViewById(R.id.rcv_admin_halls);
        hallList = new ArrayList<>();
        adapter = new AdminHallAdapter(hallList, hall ->
                startActivity(new Intent(this, HallEditActivity.class)
                        .putExtra("HALL_ID", hall.hallId)));
        rcvHalls.setLayoutManager(new LinearLayoutManager(this));
        rcvHalls.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHalls(); // load lại mỗi khi quay về (vd sau khi Generate hoặc Save xong)
    }

    private void loadHalls() {
        db.collection("Hall").get()
                .addOnSuccessListener(snapshot -> {
                    hallList.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        hallList.add(Hall.fromDocument(doc));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}