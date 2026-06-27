package com.example.apptvxemphim;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import java.util.Calendar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditMovieActivity extends AppCompatActivity {

    private EditText etTitle, etPoster, etTrailer, etDirector, etCast, etDuration, etGenres, etDescription;
    private EditText etReleaseDate;
    private Button btnPickDate;
    private LinearLayout layoutReleaseDate;
    private Spinner spinnerFormat, spinnerAge;
    private TextView tvHeader;
    private String movieId = null; // Biến lưu trữ ID nếu ở chế độ Sửa
    private String movieType = "Movie"; // "Movie" cho phim đang chiếu, "ComingMovie" cho phim sắp chiếu
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
        layoutReleaseDate = findViewById(R.id.layout_release_date);
        etReleaseDate = findViewById(R.id.et_release_date);
        btnPickDate = findViewById(R.id.btn_pick_date);

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

        // Kiểm tra xem có ID và loại phim truyền sang không (Chế độ Sửa)
        movieId = getIntent().getStringExtra("MOVIE_ID");
        movieType = getIntent().getStringExtra("MOVIE_TYPE");
        android.util.Log.d("AddEditMovie", "Received movieType: " + movieType);
        if (movieType == null) {
            movieType = "Movie"; // Mặc định là phim đang chiếu
        }
        // Show/Hide release date picker based on movie type
        if ("ComingMovie".equals(movieType)) {
            android.util.Log.d("AddEditMovie", "Showing release date picker");
            layoutReleaseDate.setVisibility(android.view.View.VISIBLE);
        } else {
            android.util.Log.d("AddEditMovie", "Hiding release date picker");
            layoutReleaseDate.setVisibility(android.view.View.GONE);
        }
        
        // Date picker button click
        btnPickDate.setOnClickListener(v -> showDatePicker());
        
        if (movieId != null) {
            if ("ComingMovie".equals(movieType)) {
                tvHeader.setText("Chỉnh sửa Phim Sắp Chiếu");
            } else {
                tvHeader.setText("Chỉnh sửa Phim");
            }
            loadMovieData(movieId);
        }

        btnSave.setOnClickListener(v -> saveMovieToFirebase());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    etReleaseDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void loadMovieData(String id) {
        android.util.Log.d("AddEditMovie", "Loading movie from collection: " + movieType + ", ID: " + id);
        db.collection(movieType).document(id).get().addOnSuccessListener(doc -> {
            android.util.Log.d("AddEditMovie", "Document exists: " + doc.exists());
            if (doc.exists()) {
                Movie movie = doc.toObject(Movie.class);
                android.util.Log.d("AddEditMovie", "Movie loaded: " + (movie != null ? movie.getTitle() : "null"));
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
                    
                    // Set release date for coming soon movies
                    android.util.Log.d("AddEditMovie", "Release date from movie: " + movie.getReleaseDate());
                    if (movie.getReleaseDate() != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(movie.getReleaseDate().toDate());
                        String date = String.format("%02d/%02d/%04d", 
                            cal.get(Calendar.DAY_OF_MONTH),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.YEAR));
                        android.util.Log.d("AddEditMovie", "Setting release date: " + date);
                        etReleaseDate.setText(date);
                    } else {
                        android.util.Log.w("AddEditMovie", "Release date is null!");
                    }

                    // Chuyển mảng List thành chuỗi cách nhau bằng dấu phẩy
                    if (movie.getGenres() != null) {
                        etGenres.setText(String.join(", ", movie.getGenres()));
                    }
                }
            } else {
                android.util.Log.w("AddEditMovie", "Document not found in collection: " + movieType);
            }
        }).addOnFailureListener(e -> {
            android.util.Log.e("AddEditMovie", "Error loading movie", e);
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
        
        // Add release date for coming soon movies
        if ("ComingMovie".equals(movieType)) {
            String releaseDateStr = etReleaseDate.getText().toString().trim();
            if (!releaseDateStr.isEmpty()) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                    java.util.Date date = sdf.parse(releaseDateStr);
                    com.google.firebase.Timestamp timestamp = new com.google.firebase.Timestamp(date);
                    movieData.put("releaseDate", timestamp);
                } catch (Exception e) {
                    android.util.Log.e("AddEditMovie", "Error parsing date", e);
                }
            }
        }

        if (movieId == null) {
            // Chế độ Thêm Mới
            db.collection(movieType).add(movieData)
                    .addOnSuccessListener(docRef -> {
                        String msg = "ComingMovie".equals(movieType) ? "Thêm phim sắp chiếu thành công!" : "Thêm phim thành công!";
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            // Chế độ Chỉnh Sửa
            db.collection(movieType).document(movieId).set(movieData)
                    .addOnSuccessListener(aVoid -> {
                        String msg = "ComingMovie".equals(movieType) ? "Cập nhật phim sắp chiếu thành công!" : "Cập nhật thành công!";
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }
}