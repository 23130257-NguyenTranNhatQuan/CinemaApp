package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageCinemasActivity extends AppCompatActivity implements AdminCinemaAdapter.OnCinemaActionListener {

    private RecyclerView rcvCinemas;
    private AdminCinemaAdapter adapter;
    private List<Cinema> cinemaList;
    private FirebaseFirestore db;
    private FloatingActionButton fabAddCinema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_cinemas);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btn_back_admin_cinema).setOnClickListener(v -> finish());

        fabAddCinema = findViewById(R.id.fab_add_cinema);
        fabAddCinema.setOnClickListener(v -> {
            Intent intent = new Intent(ManageCinemasActivity.this, AddEditCinemaActivity.class);
            startActivity(intent);
        });

        rcvCinemas = findViewById(R.id.rcv_admin_cinemas);
        cinemaList = new ArrayList<>();
        adapter = new AdminCinemaAdapter(cinemaList, this);
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
                    cinema.setCinemaId(doc.getId());
                    cinemaList.add(cinema);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onEditClick(Cinema cinema) {
        Intent intent = new Intent(ManageCinemasActivity.this, AddEditCinemaActivity.class);
        intent.putExtra("CINEMA_ID", cinema.getCinemaId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Cinema cinema) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa rạp \"" + cinema.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCinema(cinema))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCinema(Cinema cinema) {
        db.collection("Cinema").document(cinema.getCinemaId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Xóa rạp thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi xóa rạp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}