package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    private RecyclerView rvNews;
    private NewsAdapter adapter;
    private List<News> newsList = new ArrayList<>();
    private FirebaseFirestore db;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private LinearLayout emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Tin tức & Khuyến mãi");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        rvNews = findViewById(R.id.rvNews);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);

        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_news);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(NewsActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_ticket) {
                    startActivity(new Intent(NewsActivity.this, CinemaListActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_news) {
                    return true;
                } else if (id == R.id.nav_account) {
                    startActivity(new Intent(NewsActivity.this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return false;
            }
        });

        // Cấu hình LayoutManager
        rvNews.setLayoutManager(new LinearLayoutManager(NewsActivity.this));

        // Tải dữ liệu
        loadNewsFromFirebase();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void loadNewsFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        rvNews.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        db.collection("News").get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                newsList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    News news = document.toObject(News.class);
                    newsList.add(news);
                }

                if (newsList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    rvNews.setVisibility(View.GONE);
                } else {
                    adapter = new NewsAdapter(newsList, NewsActivity.this);
                    rvNews.setAdapter(adapter);
                    rvNews.setVisibility(View.VISIBLE);
                }
            } else {
                emptyState.setVisibility(View.VISIBLE);
                rvNews.setVisibility(View.GONE);
                Toast.makeText(this, "Không tải được dữ liệu! Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
