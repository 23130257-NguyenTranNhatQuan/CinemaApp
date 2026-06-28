package com.example.apptvxemphim;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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
        adapter = new AdminUserAdapter(userList, new AdminUserAdapter.OnUserActionListener() {
            @Override
            public void onRoleChange(UserAccount user) {
                showRoleChangeDialog(user);
            }

            @Override
            public void onBanUnban(UserAccount user) {
                toggleBanStatus(user);
            }
        });
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

    private void showRoleChangeDialog(UserAccount user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_role, null);
        builder.setView(dialogView);

        Spinner spinnerRoles = dialogView.findViewById(R.id.spinner_roles);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        // Setup spinner with roles
        String[] roles = {"user", "admin"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoles.setAdapter(spinnerAdapter);

        // Set current role
        String currentRole = user.getRole() != null ? user.getRole() : "user";
        int position = currentRole.equals("admin") ? 1 : 0;
        spinnerRoles.setSelection(position);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newRole = spinnerRoles.getSelectedItem().toString();
            updateUserRole(user, newRole);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateUserRole(UserAccount user, String newRole) {
        db.collection("User").document(user.getId())
                .update("role", newRole)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật role thành công", Toast.LENGTH_SHORT).show();
                    user.setRole(newRole);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi cập nhật role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleBanStatus(UserAccount user) {
        boolean newBanStatus = !user.isBanned();
        String action = newBanStatus ? "ban" : "unban";

        db.collection("User").document(user.getId())
                .update("banned", newBanStatus)
                .addOnSuccessListener(aVoid -> {
                    String message = newBanStatus ? "Đã ban người dùng" : "Đã unban người dùng";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    user.setBanned(newBanStatus);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
