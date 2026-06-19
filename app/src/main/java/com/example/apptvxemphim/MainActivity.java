package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rcvMovies;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;

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
        Intent intent = new Intent(MainActivity.this, com.example.apptvxemphim.SeatSelectionActivity.class);
        startActivity(intent);


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

        rcvMovies = findViewById(R.id.rcv_movies);
        rcvMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(movieList);
        rcvMovies.setAdapter(movieAdapter);

        // 2. Kéo dữ liệu từ Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Movie").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Xóa list cũ trước khi thêm mới để không bị trùng
                movieList.clear();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Firebase có hàm cực hay: Tự động map dữ liệu thành Class Java
                    Movie movie = document.toObject(Movie.class);
                    movieList.add(movie);
                }

                // Báo cho Adapter biết dữ liệu đã tải xong để nó vẽ lên màn hình
                movieAdapter.notifyDataSetChanged();
            } else {
                Log.w("FirebaseTest", "Lỗi lấy dữ liệu", task.getException());
            }
        });
    }
}