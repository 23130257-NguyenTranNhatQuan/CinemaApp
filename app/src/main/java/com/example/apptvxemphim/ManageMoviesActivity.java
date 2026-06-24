package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ManageMoviesActivity extends AppCompatActivity {

    private RecyclerView rcvMovies;
    private AdminMovieAdapter adapter;
    private List<Movie> movieList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_movies);

        db = FirebaseFirestore.getInstance();
        rcvMovies = findViewById(R.id.rcv_admin_movies);
        findViewById(R.id.btn_back_admin).setOnClickListener(v -> finish());

        movieList = new ArrayList<>();
        adapter = new AdminMovieAdapter(movieList);
        rcvMovies.setLayoutManager(new LinearLayoutManager(this));
        rcvMovies.setAdapter(adapter);

        // Load dữ liệu Realtime (Tự động cập nhật khi có thay đổi)
        db.collection("Movie").addSnapshotListener((value, error) -> {
            if (error != null) return;
            movieList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot doc : value) {
                    Movie movie = doc.toObject(Movie.class);
                    movieList.add(movie);
                }
                adapter.notifyDataSetChanged();
            }
        });

        // Mở trang Thêm phim
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_movie);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ManageMoviesActivity.this, AddEditMovieActivity.class);
            startActivity(intent);
        });
    }
}