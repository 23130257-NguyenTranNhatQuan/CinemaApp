package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvName;
    private AppCompatButton btnViewHistory;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvName);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnViewHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            // Đọc Tên người dùng từ Firestore collection "User"
            db.collection("User").document(uid).get().addOnSuccessListener(document -> {
                if (document.exists()) {
                    String fullName = document.getString("full_name");
                    tvName.setText(fullName != null ? fullName : "Khách hàng");
                }
            });
        }
    }
}