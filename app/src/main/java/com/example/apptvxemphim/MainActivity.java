package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rcvMovies;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;
    private ImageView[] bannerImages;
    private ImageButton btnPrev, btnNext;
    private LinearLayout layoutDots;
    private ImageView[] dots;
    private int currentPage = 0;
    private int totalBanners = 0;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Timer slideTimer;
    private TimerTask slideTimerTask;
    private static final long SLIDE_DELAY = 5000; // 5 seconds
    private List<Banner> bannerList = new ArrayList<>();

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
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

        // Initialize banner ImageViews
        bannerImages = new ImageView[]{
            findViewById(R.id.banner1),
            findViewById(R.id.banner2),
            findViewById(R.id.banner3),
            findViewById(R.id.banner4)
        };
        
        // Verify ImageViews were found
        for (int i = 0; i < bannerImages.length; i++) {
            if (bannerImages[i] == null) {
                Log.e("FirebaseTest", "banner" + (i+1) + " ImageView is NULL!");
            } else {
                Log.d("FirebaseTest", "banner" + (i+1) + " ImageView found: " + bannerImages[i]);
            }
        }

        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        layoutDots = findViewById(R.id.layout_dots);
        
        dots = new ImageView[]{
            findViewById(R.id.dot1),
            findViewById(R.id.dot2),
            findViewById(R.id.dot3),
            findViewById(R.id.dot4)
        };
        
        // Verify dots were found
        for (int i = 0; i < dots.length; i++) {
            if (dots[i] == null) {
                Log.e("FirebaseTest", "dot" + (i+1) + " ImageView is NULL!");
            }
        }

        // Load placeholder banners immediately so user sees something
        loadPlaceholderBanners();
        
        // Then try to load from Firebase
        loadBannersFromFirebase();
        
        setupDots(0);
        updateArrowsVisibility(0);

        // Setup navigation arrows
        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
            } else {
                currentPage = totalBanners - 1;
            }
            showBanner(currentPage);
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalBanners - 1) {
                currentPage++;
            } else {
                currentPage = 0;
            }
            showBanner(currentPage);
        });

        // Setup Movie List
        rcvMovies = findViewById(R.id.rcv_movies);
        rcvMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(movieList);
        rcvMovies.setAdapter(movieAdapter);

        // Load dữ liệu từ Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Movie").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                movieList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Movie movie = document.toObject(Movie.class);
                    movieList.add(movie);
                }
                movieAdapter.notifyDataSetChanged();
            } else {
                Log.w("FirebaseTest", "Lỗi lấy dữ liệu", task.getException());
            }
        });
    }

    private void loadBannersFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("FirebaseTest", "Starting to load banners from Firebase...");
        db.collection("Banner").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseTest", "Firebase query successful. Documents found: " + task.getResult().size());
                bannerList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d("FirebaseTest", "Document ID: " + document.getId() + ", Data: " + document.getData());
                    Banner banner = document.toObject(Banner.class);
                    bannerList.add(banner);
                    Log.d("FirebaseTest", "Loaded banner: " + banner.getBanner() + " order: " + banner.getOrder());
                }
                // Sort by order
                Collections.sort(bannerList, (b1, b2) -> Integer.compare(b1.getOrder(), b2.getOrder()));
                // Update totalBanners to actual count
                totalBanners = bannerList.size();
                Log.d("FirebaseTest", "Total banners loaded: " + totalBanners);
                
                if (totalBanners > 0) {
                    // Load images into ImageViews
                    loadBannerImages();
                    // Show first banner
                    showBanner(0);
                    // Start auto-slide
                    startAutoSlide();
                } else {
                    Log.w("FirebaseTest", "No banners found in Firebase, loading placeholders");
                    loadPlaceholderBanners();
                }
            } else {
                Log.w("FirebaseTest", "Lỗi lấy dữ liệu banner", task.getException());
                Log.w("FirebaseTest", "Loading placeholder banners instead");
                loadPlaceholderBanners();
            }
        });
    }
    
    private void loadPlaceholderBanners() {
        // Fallback: Load placeholder banners if Firebase fails
        String[] placeholderUrls = {
            "https://via.placeholder.com/800x400/9C27B0/FFFFFF?text=Banner+1",
            "https://via.placeholder.com/800x400/9C27B0/FFFFFF?text=Banner+2",
            "https://via.placeholder.com/800x400/9C27B0/FFFFFF?text=Banner+3",
            "https://via.placeholder.com/800x400/9C27B0/FFFFFF?text=Banner+4"
        };
        
        bannerList.clear();
        for (int i = 0; i < placeholderUrls.length; i++) {
            bannerList.add(new Banner(placeholderUrls[i], i));
        }
        totalBanners = bannerList.size();
        loadBannerImages();
        showBanner(0);
        startAutoSlide();
    }

    private void loadBannerImages() {
        Log.d("FirebaseTest", "Loading banner images...");
        for (int i = 0; i < bannerImages.length && i < bannerList.size(); i++) {
            String imageUrl = bannerList.get(i).getBanner();
            Log.d("FirebaseTest", "Loading image " + i + ": " + imageUrl);
            
            com.bumptech.glide.Glide.with(this)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(bannerImages[i]);
        }
    }

    private void showBanner(int position) {
        Log.d("FirebaseTest", "Showing banner at position: " + position + ", totalBanners: " + totalBanners);
        // Hide all banners
        for (ImageView banner : bannerImages) {
            banner.setVisibility(View.GONE);
        }
        // Show only the selected banner
        if (position < bannerImages.length && position < totalBanners && position >= 0) {
            bannerImages[position].setVisibility(View.VISIBLE);
            Log.d("FirebaseTest", "Banner " + position + " is now VISIBLE");
        } else {
            Log.w("FirebaseTest", "Position " + position + " out of range. totalBanners=" + totalBanners);
        }
        // Update dots
        setupDots(position);
    }

    private void setupDots(int position) {
        for (int i = 0; i < dots.length; i++) {
            if (i == position) {
                dots[i].setSelected(true);
            } else {
                dots[i].setSelected(false);
            }
        }
    }

    private void updateArrowsVisibility(int position) {
        if (totalBanners <= 1) {
            btnPrev.setVisibility(View.INVISIBLE);
            btnNext.setVisibility(View.INVISIBLE);
        } else {
            btnPrev.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);
        }
    }

    private void startAutoSlide() {
        stopAutoSlide();
        slideTimer = new Timer();
        slideTimerTask = new TimerTask() {
            @Override
            public void run() {
                sliderHandler.post(() -> {
                    if (totalBanners > 0) {
                        // Advance by exactly 1 banner
                        if (currentPage < totalBanners - 1) {
                            currentPage++;
                        } else {
                            currentPage = 0;
                        }
                        showBanner(currentPage);
                    }
                });
            }
        };
        slideTimer.scheduleAtFixedRate(slideTimerTask, SLIDE_DELAY, SLIDE_DELAY);
    }

    private void stopAutoSlide() {
        if (slideTimer != null) {
            slideTimer.cancel();
            slideTimer = null;
        }
        if (slideTimerTask != null) {
            slideTimerTask.cancel();
            slideTimerTask = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoSlide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoSlide();
    }
}