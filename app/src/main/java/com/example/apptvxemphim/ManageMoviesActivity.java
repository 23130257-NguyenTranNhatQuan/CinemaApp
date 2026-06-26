package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ManageMoviesActivity extends AppCompatActivity {

    private RecyclerView rcvMovies;
    private AdminMovieAdapter adapter;
    private List<Movie> movieList;
    private FirebaseFirestore db;

    private TextView tabNowShowing, tabComingSoon;
    private boolean isShowingNowShowing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_movies);

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btn_back_admin).setOnClickListener(v -> finish());

        tabNowShowing = findViewById(R.id.tab_now_showing);
        tabComingSoon = findViewById(R.id.tab_coming_soon);

        rcvMovies = findViewById(R.id.rcv_admin_movies);
        movieList = new ArrayList<>();
        adapter = new AdminMovieAdapter(movieList);
        rcvMovies.setLayoutManager(new LinearLayoutManager(this));
        rcvMovies.setAdapter(adapter);

        // Tab click listeners
        tabNowShowing.setOnClickListener(v -> {
            isShowingNowShowing = true;
            updateTabStyles();
            loadMovies();
        });

        tabComingSoon.setOnClickListener(v -> {
            isShowingNowShowing = false;
            updateTabStyles();
            loadMovies();
        });

        // Initial load
        updateTabStyles();
        loadMovies();

        // Mở trang Thêm phim
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_movie);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ManageMoviesActivity.this, AddEditMovieActivity.class);
            startActivity(intent);
        });
    }

    private void updateTabStyles() {
        if (isShowingNowShowing) {
            tabNowShowing.setBackgroundResource(R.drawable.tab_active_bg);
            tabNowShowing.setTextColor(getResources().getColor(android.R.color.white));
            tabComingSoon.setBackgroundResource(R.drawable.tab_inactive_bg);
            tabComingSoon.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            tabComingSoon.setBackgroundResource(R.drawable.tab_active_bg);
            tabComingSoon.setTextColor(getResources().getColor(android.R.color.white));
            tabNowShowing.setBackgroundResource(R.drawable.tab_inactive_bg);
            tabNowShowing.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void loadMovies() {
        String collection = isShowingNowShowing ? "Movie" : "ComingMovie";
        db.collection(collection).addSnapshotListener((value, error) -> {
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
    }
}
