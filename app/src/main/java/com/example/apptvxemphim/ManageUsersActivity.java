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

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView rcvUsers;
    private AdminUserAdapter adapter;
    private List<UserAccount> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btn_back_admin_users).setOnClickListener(v -> finish());

        rcvUsers = findViewById(R.id.rcv_admin_users);
        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(userList);
        rcvUsers.setLayoutManager(new LinearLayoutManager(this));
        rcvUsers.setAdapter(adapter);

        // Load danh sách người dùng từ Firebase
        db.collection("User").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            userList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    UserAccount user = doc.toObject(UserAccount.class);
                    userList.add(user);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}