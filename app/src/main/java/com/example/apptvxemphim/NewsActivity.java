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

public class NewsActivity extends AppCompatActivity {

    private RecyclerView rvNews;
    private NewsAdapter adapter;
    private List<News> newsList = new ArrayList<>(); // Dùng List<News> thay vì Combo
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        rvNews = findViewById(R.id.rvNews);
        db = FirebaseFirestore.getInstance();

        // Cấu hình LayoutManager
        rvNews.setLayoutManager(new LinearLayoutManager(this));

        // Tải dữ liệu
        loadNewsFromFirebase();
    }

    private void loadNewsFromFirebase() {
        // Lưu ý: Tên collection trong Firestore của bạn là "News" hay "news"?
        // Hãy chắc chắn viết hoa/thường khớp với trên Console.
        db.collection("News").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                newsList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Chuyển đổi dữ liệu từ Firestore sang Object News
                    News news = document.toObject(News.class);
                    newsList.add(news);
                }

                adapter = new NewsAdapter(newsList, NewsActivity.this);
                rvNews.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Không tải được dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}