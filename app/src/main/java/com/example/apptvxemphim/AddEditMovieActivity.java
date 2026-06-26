package com.example.apptvxemphim;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditMovieActivity extends AppCompatActivity {

    private EditText etTitle, etPoster, etTrailer, etDirector, etCast, etDuration, etGenres, etDescription;
    private Spinner spinnerFormat, spinnerAge;
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
        spinnerFormat = findViewById(R.id.spinner_format);
        spinnerAge = findViewById(R.id.spinner_age);
        etGenres = findViewById(R.id.et_movie_genres);

        // Setup spinners with custom layouts
        ArrayAdapter<CharSequence> formatAdapter = ArrayAdapter.createFromResource(this,
                R.array.movie_formats, R.layout.spinner_item_white);
        formatAdapter.setDropDownViewResource(R.layout.dropdown_item_purple);
        spinnerFormat.setAdapter(formatAdapter);

        ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(this,
                R.array.age_ratings, R.layout.spinner_item_white);
        ageAdapter.setDropDownViewResource(R.layout.dropdown_item_purple);
        spinnerAge.setAdapter(ageAdapter);
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
                    // Set spinner selections based on movie data
                    String format = movie.getFormat();
                    if (format != null) {
                        ArrayAdapter formatAdapter = (ArrayAdapter) spinnerFormat.getAdapter();
                        int pos = formatAdapter.getPosition(format);
                        if (pos >= 0) spinnerFormat.setSelection(pos);
                    }

                    String age = movie.getAge();
                    if (age != null) {
                        ArrayAdapter ageAdapter = (ArrayAdapter) spinnerAge.getAdapter();
                        int pos = ageAdapter.getPosition(age);
                        if (pos >= 0) spinnerAge.setSelection(pos);
                    }
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
        // Get selected values from Spinner
        String selectedFormat = spinnerFormat.getSelectedItem().toString();
        String selectedAge = spinnerAge.getSelectedItem().toString();
        
        movieData.put("age", selectedAge);
        movieData.put("format", selectedFormat);
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