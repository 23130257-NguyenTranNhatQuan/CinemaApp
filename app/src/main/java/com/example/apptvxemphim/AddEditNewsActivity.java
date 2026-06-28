package com.example.apptvxemphim;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddEditNewsActivity extends AppCompatActivity {

    private EditText etTitle, etImageUrl, etContent;
    private TextView tvHeader;
    private String newsId = null;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_news);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ
        tvHeader = findViewById(R.id.tv_add_edit_news_header);
        etTitle = findViewById(R.id.et_news_title);
        etImageUrl = findViewById(R.id.et_news_image_url);
        etContent = findViewById(R.id.et_news_content);
        Button btnSave = findViewById(R.id.btn_save_news);

        // Nút quay lại
        findViewById(R.id.btn_back_add_edit_news).setOnClickListener(v -> finish());

        // Kiểm tra xem có ID truyền sang không (Chế độ Sửa)
        newsId = getIntent().getStringExtra("NEWS_ID");

        if (newsId != null) {
            tvHeader.setText("Chỉnh sửa Tin tức");
            loadNewsData();
        } else {
            tvHeader.setText("Thêm Tin tức mới");
        }

        btnSave.setOnClickListener(v -> saveNewsToFirebase());
    }

    private void loadNewsData() {
        db.collection("News").document(newsId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                News news = documentSnapshot.toObject(News.class);
                if (news != null) {
                    etTitle.setText(news.getTitle());
                    etImageUrl.setText(news.getImageUrl());
                    etContent.setText(news.getContent());
                }
            } else {
                Toast.makeText(this, "Không tìm thấy tin tức!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveNewsToFirebase() {
        String title = etTitle.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tiêu đề!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> newsData = new HashMap<>();
        newsData.put("title", title);
        newsData.put("imageUrl", imageUrl);
        newsData.put("content", content);

        if (newsId == null) {
            // Chế độ Thêm Mới
            db.collection("News").add(newsData)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Thêm tin tức thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi thêm tin tức!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Chế độ Chỉnh Sửa
            db.collection("News").document(newsId).set(newsData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật tin tức thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi khi cập nhật tin tức!", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
