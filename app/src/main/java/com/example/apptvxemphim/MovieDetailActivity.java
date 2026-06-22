package com.example.apptvxemphim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {
    private ImageView imgPoster;
    private TextView tvTitle, tvInfo, tvDescription;
    private Button btnWatchTrailer;

    private RecyclerView rcvShowtimes;
    private ShowtimeDetailAdapter showtimeAdapter;
    private List<Showtime> showtimeList;

    private FirebaseFirestore db;
    private String movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // 1. Ánh xạ View
        imgPoster = findViewById(R.id.img_detail_poster);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvInfo = findViewById(R.id.tv_detail_info);
        tvDescription = findViewById(R.id.tv_detail_description);
        btnWatchTrailer = findViewById(R.id.btn_watch_trailer);
        rcvShowtimes = findViewById(R.id.rcv_detail_showtimes);

        db = FirebaseFirestore.getInstance();

        // 2. Nhận ID phim từ Adapter Trang chủ gửi sang
        movieId = getIntent().getStringExtra("MOVIE_ID");
        if (movieId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy phim!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. Cài đặt RecyclerView cho Suất chiếu
        showtimeList = new ArrayList<>();
        showtimeAdapter = new ShowtimeDetailAdapter(showtimeList);
        rcvShowtimes.setLayoutManager(new LinearLayoutManager(this));
        rcvShowtimes.setAdapter(showtimeAdapter);

        // 4. Bắt đầu tải dữ liệu
        loadMovieDetail();
        loadShowtimes();
    }

    private void loadMovieDetail() {
        // Truy vấn trực tiếp vào Document ID của phim
        db.collection("Movie").document(movieId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Movie movie = documentSnapshot.toObject(Movie.class);
                        if (movie != null) {
                            // Gán dữ liệu lên UI
                            tvTitle.setText(movie.getTitle());
                            tvDescription.setText(movie.getDescription());

                            // Xử lý Thể loại (Nối mảng thành chuỗi)
                            String genresStr = "Đang cập nhật";
                            if (movie.getGenres() != null) {
                                genresStr = TextUtils.join(", ", movie.getGenres());
                            }
                            tvInfo.setText(movie.getDuration() + " phút • " + genresStr);

                            // Tải ảnh bằng Glide
                            Glide.with(this)
                                    .load(movie.getPoster())
                                    .placeholder(android.R.drawable.ic_menu_report_image)
                                    .into(imgPoster);

                            // Xử lý nút mở Trailer YouTube
                            btnWatchTrailer.setOnClickListener(v -> {
                                if (movie.getTrailer() != null && !movie.getTrailer().isEmpty()) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(movie.getTrailer()));
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(this, "Chưa có trailer cho phim này", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải phim", Toast.LENGTH_SHORT).show());
    }

    private void loadShowtimes() {
        // Lọc trong bảng Showtime những suất chiếu có mã phim bằng với movieId
        db.collection("Showtime")
                .whereEqualTo("movie_id", movieId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showtimeList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Showtime showtime = doc.toObject(Showtime.class);
                            showtimeList.add(showtime);
                        }
                        showtimeAdapter.notifyDataSetChanged();
                    }
                });
    }

}
