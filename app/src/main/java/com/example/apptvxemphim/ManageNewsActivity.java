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

public class ManageNewsActivity extends AppCompatActivity {

    private RecyclerView rcvNews;
    private AdminNewsAdapter adapter;
    private List<News> newsList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_news);

        db = FirebaseFirestore.getInstance();

        // Nút quay lại
        findViewById(R.id.btn_back_admin_news).setOnClickListener(v -> finish());

        rcvNews = findViewById(R.id.rcv_admin_news);
        newsList = new ArrayList<>();
        adapter = new AdminNewsAdapter(newsList);
        rcvNews.setLayoutManager(new LinearLayoutManager(this));
        rcvNews.setAdapter(adapter);

        loadNews();

        // FAB Thêm tin tức
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_news);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ManageNewsActivity.this, AddEditNewsActivity.class);
            startActivity(intent);
        });
    }

    private void loadNews() {
        db.collection("News").addSnapshotListener((value, error) -> {
            if (error != null) return;
            newsList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    News news = doc.toObject(News.class);
                    newsList.add(news);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
