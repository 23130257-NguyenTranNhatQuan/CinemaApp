package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.DividerItemDecoration;
public class CinemaListActivity extends AppCompatActivity {

    private RecyclerView rcvCinemas;
    private CinemaListAdapter cinemaAdapter;
    private List<Cinema> cinemaList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_list);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        rcvCinemas = findViewById(R.id.rcv_cinemas);
        rcvCinemas.setLayoutManager(new LinearLayoutManager(this));
        cinemaAdapter = new CinemaListAdapter(cinemaList, cinema -> {
            // Bấm vào rạp → mở CinemaShowtimeActivity
            Intent intent = new Intent(this, CinemaShowtimeActivity.class);
            intent.putExtra("CINEMA_ID", cinema.getCinemaId());
            intent.putExtra("CINEMA_NAME", cinema.getName());
            intent.putExtra("CINEMA_GGMAP", cinema.getGgmap());
            startActivity(intent);
        });
        rcvCinemas.setAdapter(cinemaAdapter);
        rcvCinemas.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        loadCinemas();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_ticket);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(CinemaListActivity.this, MainActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_ticket) {
                    return true;
                } else if (id == R.id.nav_news) {
                    startActivity(new Intent(CinemaListActivity.this, NewsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (id == R.id.nav_account) {
                    startActivity(new Intent(CinemaListActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void loadCinemas() {
        FirebaseFirestore.getInstance().collection("Cinema").get()
                .addOnSuccessListener(snap -> {
                    cinemaList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Cinema c = doc.toObject(Cinema.class);
                        cinemaList.add(c);
                    }
                    cinemaAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e("CinemaList", "Lỗi tải rạp: " + e.getMessage()));
    }
}