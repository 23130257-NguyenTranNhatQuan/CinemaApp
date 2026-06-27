package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmailLogin, edtPasswordLogin;
    private Button btnLogin;
    private TextView tvRegisterNow;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        edtEmailLogin = findViewById(R.id.edtEmailLogin);
        edtPasswordLogin = findViewById(R.id.edtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterNow = findViewById(R.id.tvRegisterNow);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmailLogin.getText().toString().trim();
                String password = edtPasswordLogin.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập Email và Mật khẩu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tiến hành đăng nhập bằng Firebase Auth
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();

                                // KIỂM TRA XEM EMAIL ĐÃ ĐƯỢC XÁC THỰC CHƯA
                                if (user != null && user.isEmailVerified()) {
                                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Nếu chưa xác thực -> Thông báo và kích văng ra ngoài
                                    Toast.makeText(LoginActivity.this, "Vui lòng vào hộp thư Email để xác thực tài khoản trước khi đăng nhập!", Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                }
                            } else {
                                String error = task.getException() != null ? task.getException().getMessage() : "Lỗi";
                                Toast.makeText(LoginActivity.this, "Lỗi đăng nhập: " + error, Toast.LENGTH_LONG).show();
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
    }
}