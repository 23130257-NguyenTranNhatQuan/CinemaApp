package com.example.apptvxemphim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class ComingSoonDetailActivity extends AppCompatActivity {
    private ImageView imgPoster;
    private TextView tvTitle, tvInfo, tvDescription, tvDirector, tvCast, tvFormat, tvAgeRating, tvReleaseDate;
    private ImageButton btnWatchTrailer;

    private FirebaseFirestore db;
    private String movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coming_soon_detail);

        // 1. Ánh xạ View
        imgPoster = findViewById(R.id.img_detail_poster);
        tvTitle = findViewById(R.id.tv_detail_title);
        tvInfo = findViewById(R.id.tv_detail_info);
        tvDescription = findViewById(R.id.tv_detail_description);
        tvDirector = findViewById(R.id.tv_director);
        tvCast = findViewById(R.id.tv_cast);
        tvFormat = findViewById(R.id.tv_format);
        tvAgeRating = findViewById(R.id.tv_age_rating);
        tvReleaseDate = findViewById(R.id.tv_release_date);
        btnWatchTrailer = findViewById(R.id.btn_watch_trailer);
        db = FirebaseFirestore.getInstance();

        // 2. Nhận ID phim
        movieId = getIntent().getStringExtra("MOVIE_ID");
        if (movieId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy phim!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 3. Tải dữ liệu
        loadMovieDetail();
    }

    private void loadMovieDetail() {
        db.collection("ComingMovie").document(movieId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Movie movie = documentSnapshot.toObject(Movie.class);
                        if (movie != null) {
                            // Title
                            tvTitle.setText(movie.getTitle());

                            // Genre
                            String genresStr = "Đang cập nhật";
                            if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                                genresStr = TextUtils.join(", ", movie.getGenres());
                            }
                            tvInfo.setText(movie.getDuration() + " phút • " + genresStr);

                            // Format & Age Rating
                            if (movie.getFormat() != null && !movie.getFormat().isEmpty()) {
                                tvFormat.setText(movie.getFormat());
                            } else {
                                tvFormat.setText("2D");
                            }
                            if (movie.getAge() != null && !movie.getAge().isEmpty()) {
                                tvAgeRating.setText(movie.getAge());
                            } else {
                                tvAgeRating.setText("K");
                            }

                            // Director
                            String directorName = (movie.getDirector() != null && !movie.getDirector().isEmpty())
                                ? movie.getDirector() : "Đang cập nhật";
                            tvDirector.setText(Html.fromHtml("<b>Đạo diễn:</b> " + directorName, Html.FROM_HTML_MODE_LEGACY));

                            // Cast
                            String castNames = (movie.getCast() != null && !movie.getCast().isEmpty())
                                ? movie.getCast() : "Đang cập nhật";
                            tvCast.setText(Html.fromHtml("<b>Diễn viên:</b> " + castNames, Html.FROM_HTML_MODE_LEGACY));

                            // Release Date
                            String releaseDateStr = movie.getFormattedReleaseDate();
                            if (!releaseDateStr.isEmpty()) {
                                tvReleaseDate.setText("Khởi chiếu: " + releaseDateStr);
                            } else {
                                tvReleaseDate.setText("Khởi chiếu: Đang cập nhật");
                            }

                            // Description
                            if (movie.getDescription() != null && !movie.getDescription().isEmpty()) {
                                tvDescription.setText(movie.getDescription());
                            } else {
                                tvDescription.setText("Chưa có mô tả cho phim này.");
                            }

                            // Poster
                            Glide.with(this)
                                    .load(movie.getPoster())
                                    .placeholder(android.R.drawable.ic_menu_report_image)
                                    .into(imgPoster);

                            // Trailer button
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
}