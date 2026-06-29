package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

    private Spinner spinnerCinema;

    private java.util.Map<String, String> cinemaNameMap = new java.util.HashMap<>();
    private List<Cinema> cinemaList = new ArrayList<>();
    private List<Hall> allHalls = new ArrayList<>(); // toàn bộ hall load từ Firestore, chưa filter

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
        adapter = new AdminHallAdapter(hallList, new AdminHallAdapter.OnHallClickListener() {
            @Override
            public void onHallClick(Hall hall) {
                startActivity(new Intent(ManageHallsActivity.this, HallEditActivity.class)
                        .putExtra("HALL_ID", hall.hallId));
            }
            @Override
            public void onDeleteClick(Hall hall) {
                confirmDeleteHall(hall);
            }
        });
        rcvHalls.setLayoutManager(new LinearLayoutManager(this));
        rcvHalls.setAdapter(adapter);
        spinnerCinema = findViewById(R.id.spinner_filter_cinema);
        spinnerCinema = findViewById(R.id.spinner_filter_cinema);

        android.graphics.drawable.GradientDrawable spinnerBg = new android.graphics.drawable.GradientDrawable();
        spinnerBg.setColor(android.graphics.Color.parseColor("#222222"));
        spinnerBg.setCornerRadius(24f);
        spinnerBg.setStroke(2, android.graphics.Color.parseColor("#C9227A"));
        findViewById(R.id.container_spinner_cinema).setBackground(spinnerBg);
        loadCinemasForFilter();

        spinnerCinema.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterHallsByCinema();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHalls(); // load lại mỗi khi quay về (vd sau khi Generate hoặc Save xong)
    }

    private void loadHalls() {
        db.collection("Hall").get()
                .addOnSuccessListener(snapshot -> {
                    allHalls.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        allHalls.add(Hall.fromDocument(doc));
                    }
                    filterHallsByCinema();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadCinemasForFilter() {
        db.collection("Cinema").get().addOnSuccessListener(snapshot -> {
            cinemaList.clear();
            List<String> names = new ArrayList<>();

            cinemaList.add(null);              // null = đại diện cho "Tất cả rạp"
            names.add("Tất cả rạp");

            for (QueryDocumentSnapshot doc : snapshot) {
                Cinema c = doc.toObject(Cinema.class);
                cinemaList.add(c);
                cinemaNameMap.put(c.getCinemaId(), c.getName() != null ? c.getName() : c.getCinemaId());
                names.add(c.getName() != null ? c.getName() : c.getCinemaId());
            }

            ArrayAdapter<String> cinemaAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, names) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    ((android.widget.TextView) v).setTextColor(getResources().getColor(R.color.white));
                    return v;
                }
            };
            cinemaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCinema.setAdapter(cinemaAdapter);
            adapter.setCinemaNameMap(cinemaNameMap);
            spinnerCinema.setSelection(0); // mặc định chọn "Tất cả rạp"
        });
    }

    private void filterHallsByCinema() {
        hallList.clear();
        int pos = spinnerCinema.getSelectedItemPosition();

        if (pos <= 0 || cinemaList.isEmpty()) {
            // pos == 0 (Tất cả rạp) hoặc chưa load gì -> hiện hết
            hallList.addAll(allHalls);
        } else {
            String selectedCinemaId = cinemaList.get(pos).getCinemaId();
            for (Hall h : allHalls) {
                if (selectedCinemaId != null && selectedCinemaId.equals(h.cinemaId)) {
                    hallList.add(h);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void confirmDeleteHall(Hall hall) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Xóa phòng")
                .setMessage("Xóa phòng \"" + hall.name + "\"? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("Hall").document(hall.hallId).delete();
                    db.collection("HallOverrides").document(hall.hallId).delete();
                    Toast.makeText(this, "Đã xóa phòng", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }



}
