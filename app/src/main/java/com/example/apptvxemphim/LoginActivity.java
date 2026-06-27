package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmailLogin, edtPasswordLogin;
    private Button btnLogin, btnGoogleLogin;
    private TextView tvRegisterNow;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private TextView tvForgotPassword;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        edtEmailLogin = findViewById(R.id.edtEmailLogin);
        edtPasswordLogin = findViewById(R.id.edtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("590586581626-9o7cfvvnchjkvbth7pn23e201f3a6f2l.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmailLogin.getText().toString().trim();
                String password = edtPasswordLogin.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập Email và Mật khẩu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String userId = mAuth.getCurrentUser().getUid();
                                com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

                                db.collection("User").document(userId)
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                String role = documentSnapshot.getString("role");

                                                if ("admin".equals(role)) {
                                                    Toast.makeText(LoginActivity.this, "Xin chào Quản trị viên!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(LoginActivity.this, "Lỗi đọc dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                String error = task.getException() != null ? task.getException().getMessage() : "Lỗi";
                                Toast.makeText(LoginActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        tvRegisterNow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);

            }
        });
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Lỗi đăng nhập Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Đăng nhập thành công, lấy thông tin người dùng hiện tại
                String userId = mAuth.getCurrentUser().getUid();
                String email = mAuth.getCurrentUser().getEmail();
                String name = mAuth.getCurrentUser().getDisplayName();

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Kiểm tra xem User đã có trong Firestore chưa
                db.collection("User").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // NẾU LÀ NGƯỜI DÙNG MỚI (chưa có trong Firestore) -> Tạo mới
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("full_name", name != null ? name : "Người dùng Google");
                        userData.put("role", "user"); // Mặc định là khách
                        userData.put("user", name != null ? name : "GoogleUser");

                        db.collection("User").document(userId).set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Đăng nhập & Lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Nếu User đã tồn tại rồi thì vào thẳng app luôn
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                });

            } else {
                Toast.makeText(this, "Lỗi Firebase Auth: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}