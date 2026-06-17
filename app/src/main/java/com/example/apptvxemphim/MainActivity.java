package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Gọi file giao diện XML

        // Ẩn thanh Action Bar mặc định đi cho giống app thật
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Ép ứng dụng tự động mở màn hình của bạn lên điện thoại thật
//        Intent intent = new Intent(MainActivity.this, com.example.apptvxemphim.ShowtimeSelectionActivity.class);
//        startActivity(intent);
//        finish(); // Thêm dòng này để đóng hẳn MainActivity lại, không cho chạy mống code phía dưới nữa

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Lắng nghe sự kiện click trên thanh điều hướng
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // Dùng if-else để tương thích tốt với các phiên bản Gradle mới
                if (id == R.id.nav_home) {
                    Toast.makeText(MainActivity.this, "Đang ở Trang chủ", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_ticket) {
                    Toast.makeText(MainActivity.this, "Chuyển sang Mua vé", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_news) {
                    Toast.makeText(MainActivity.this, "Chuyển sang Tin tức", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_account) {
                    Toast.makeText(MainActivity.this, "Chuyển sang Tài khoản", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }
}