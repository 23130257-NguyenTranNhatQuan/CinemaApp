package com.example.apptvxemphim;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditMovieActivity extends AppCompatActivity {

    private EditText etTitle, etPoster, etTrailer, etDirector, etCast, etDuration, etAge, etFormat, etGenres, etDescription;
    private TextView tvHeader;
    private String movieId = null; // Biến lưu trữ ID nếu ở chế độ Sửa
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_movie);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ
        tvHeader = findViewById(R.id.tv_add_edit_header);
        etTitle = findViewById(R.id.et_movie_title);
        etPoster = findViewById(R.id.et_movie_poster);
        etTrailer = findViewById(R.id.et_movie_trailer);
        etDirector = findViewById(R.id.et_movie_director);
        etCast = findViewById(R.id.et_movie_cast);
        etDuration = findViewById(R.id.et_movie_duration);
        etAge = findViewById(R.id.et_movie_age);
        etFormat = findViewById(R.id.et_movie_format);
        etGenres = findViewById(R.id.et_movie_genres);
        etDescription = findViewById(R.id.et_movie_description);
        Button btnSave = findViewById(R.id.btn_save_movie);

        // Kiểm tra xem có ID truyền sang không (Chế độ Sửa)
        movieId = getIntent().getStringExtra("MOVIE_ID");
        if (movieId != null) {
            tvHeader.setText("Chỉnh sửa Phim");
            loadMovieData(movieId);
        }

        btnSave.setOnClickListener(v -> saveMovieToFirebase());
    }

    private void loadMovieData(String id) {
        db.collection("Movie").document(id).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Movie movie = doc.toObject(Movie.class);
                if (movie != null) {
                    etTitle.setText(movie.getTitle());
                    etPoster.setText(movie.getPoster());
                    etTrailer.setText(movie.getTrailer());
                    etDirector.setText(movie.getDirector());
                    etCast.setText(movie.getCast());
                    etDuration.setText(String.valueOf(movie.getDuration()));
                    etAge.setText(movie.getAge());
                    etFormat.setText(movie.getFormat());
                    etDescription.setText(movie.getDescription());

                    // Chuyển mảng List thành chuỗi cách nhau bằng dấu phẩy
                    if (movie.getGenres() != null) {
                        etGenres.setText(String.join(", ", movie.getGenres()));
                    }
                }
            }
        });
    }

    private void saveMovieToFirebase() {
        String title = etTitle.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();

        if (title.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên phim và Thời lượng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Xử lý chuỗi Thể loại thành Mảng
        String genresStr = etGenres.getText().toString().trim();
        List<String> genresList = new ArrayList<>();
        if (!genresStr.isEmpty()) {
            String[] parts = genresStr.split(",");
            for (String part : parts) {
                genresList.add(part.trim());
            }
        }

        Map<String, Object> movieData = new HashMap<>();
        movieData.put("title", title);
        movieData.put("poster", etPoster.getText().toString().trim());
        movieData.put("trailer", etTrailer.getText().toString().trim());
        movieData.put("director", etDirector.getText().toString().trim());
        movieData.put("cast", etCast.getText().toString().trim());
        movieData.put("duration", Long.parseLong(durationStr));
        movieData.put("age", etAge.getText().toString().trim());
        movieData.put("format", etFormat.getText().toString().trim());
        movieData.put("genres", genresList);
        movieData.put("description", etDescription.getText().toString().trim());

        if (movieId == null) {
            // Chế độ Thêm Mới
            db.collection("Movie").add(movieData)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Thêm phim thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            // Chế độ Chỉnh Sửa
            db.collection("Movie").document(movieId).set(movieData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }
}