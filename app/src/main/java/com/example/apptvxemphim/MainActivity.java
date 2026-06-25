package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apptvxemphim.News;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rcvMovies;
    private MovieCarouselAdapter movieCarouselAdapter;
    private List<Movie> movieList;

    private RecyclerView rcvComingSoon;
    private ComingSoonMovieAdapter comingSoonAdapter;
    private List<Movie> comingSoonList;

    private RecyclerView rcvNews;
    private NewsAdapter newsAdapter;
    private List<News> newsList;

    private ImageView[] bannerImages;
    private ImageButton btnPrev, btnNext;
    private ImageView[] dots;
    private int currentPage = 0;
    private int totalBanners = 0;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
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
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        bannerImages = new ImageView[]{
                findViewById(R.id.banner1),
                findViewById(R.id.banner2),
                findViewById(R.id.banner3),
                findViewById(R.id.banner4)
        };

        // Verify ImageViews were found
        for (int i = 0; i < bannerImages.length; i++) {
            if (bannerImages[i] == null) {
                Log.e("FirebaseTest", "banner" + (i + 1) + " ImageView is NULL!");
            } else {
                Log.d("FirebaseTest", "banner" + (i + 1) + " ImageView found: " + bannerImages[i]);
            }
        }

        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);

        dots = new ImageView[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3),
                findViewById(R.id.dot4)
        };

        // Verify dots were found
        for (int i = 0; i < dots.length; i++) {
            if (dots[i] == null) {
                Log.e("FirebaseTest", "dot" + (i + 1) + " ImageView is NULL!");
            }
        }

        // Load placeholder banners immediately so user sees something
        loadPlaceholderBanners();
        loadBannersFromFirebase();

        setupDots(0);
        updateArrowsVisibility(0);

        btnPrev.setOnClickListener(v -> {
            if (currentPage > 0) currentPage--;
            else currentPage = totalBanners - 1;
            showBanner(currentPage);
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalBanners - 1) currentPage++;
            else currentPage = 0;
            showBanner(currentPage);
        });

        // Setup Coming Soon Movies (horizontal)
        rcvComingSoon = findViewById(R.id.rcv_coming_soon);
        rcvComingSoon.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        comingSoonList = new ArrayList<>();
        comingSoonAdapter = new ComingSoonMovieAdapter(comingSoonList);
        rcvComingSoon.setAdapter(comingSoonAdapter);

        // Setup Now Showing Movies - RecyclerView carousel with snap + scale effect
        rcvMovies = findViewById(R.id.rcv_movies);
        LinearLayoutManager carouselLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public void onLayoutCompleted(RecyclerView.State state) {
                super.onLayoutCompleted(state);
                scaleCarouselItems();
            }

            @Override
            public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
                int scrolled = super.scrollHorizontallyBy(dx, recycler, state);
                scaleCarouselItems();
                return scrolled;
            }

            private void scaleCarouselItems() {
                if (getChildCount() == 0) return;
                int centerX = (getWidth() / 2);
                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    if (child == null) continue;
                    int childCenter = (child.getLeft() + child.getRight()) / 2;
                    int distance = Math.abs(childCenter - centerX);
                    float scale = 1.0f - (0.18f * Math.min(1.0f, (float) distance / ((float) getWidth() * 0.9f)));
                    float alpha = 1.0f - (0.4f * Math.min(1.0f, (float) distance / ((float) getWidth() * 0.9f)));
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                    child.setAlpha(alpha);
                }
            }
        };
        rcvMovies.setLayoutManager(carouselLayoutManager);

        // Attach snap helper for snapping to center
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rcvMovies);

        movieList = new ArrayList<>();
        movieCarouselAdapter = new MovieCarouselAdapter(movieList);
        rcvMovies.setAdapter(movieCarouselAdapter);

        // Setup News
        rcvNews = findViewById(R.id.rcv_news);
        rcvNews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(newsList, MainActivity.this);
        rcvNews.setAdapter(newsAdapter);

        // Load data from Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("ComingMovie").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                comingSoonList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Movie movie = document.toObject(Movie.class);
                        comingSoonList.add(movie);
                    } catch (Exception e) {
                        Log.e("Loi_Firebase", "Phim sắp chiếu bị lỗi kiểu dữ liệu ở ID: " + document.getId() + " - " + e.getMessage());
                    }
                }
                comingSoonAdapter.notifyDataSetChanged();
            }
        });

        db.collection("Movie").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                movieList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Movie movie = document.toObject(Movie.class);
                        movieList.add(movie);
                    } catch (Exception e) {
                        Log.e("Loi_Firebase", "Phim đang chiếu bị lỗi kiểu dữ liệu ở ID: " + document.getId() + " - " + e.getMessage());
                    }
                }
                movieCarouselAdapter.notifyDataSetChanged();
                // Trigger initial scale after data load
                rcvMovies.post(() -> carouselLayoutManager.scrollHorizontallyBy(0, null, null));
            }
        });

        db.collection("News").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                newsList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    News news = document.toObject(News.class);
                    newsList.add(news);
                }
                newsAdapter.notifyDataSetChanged();
            } else {
                Log.e("Loi_Firebase", "Lỗi tải tin tức: " + (task.getException() != null ? task.getException().getMessage() : "Unknown"));
                Log.w("FirebaseTest", "Lỗi lấy dữ liệu tin tức", task.getException());
                loadPlaceholderNews();
            }
        });
    }
    private void loadPlaceholderComingSoon() {
        comingSoonList.clear();
        String[] placeholderPosters = {
                "https://via.placeholder.com/300x400/3F51B5/FFFFFF?text=Phim+Sap+Chieu+1",
                "https://via.placeholder.com/300x400/009688/FFFFFF?text=Phim+Sap+Chieu+2",
                "https://via.placeholder.com/300x400/FF5722/FFFFFF?text=Phim+Sap+Chieu+3",
                "https://via.placeholder.com/300x400/607D8B/FFFFFF?text=Phim+Sap+Chieu+4"
        };
        String[] titles = {"Phim Sắp Chiếu 1", "Phim Sắp Chiếu 2", "Phim Sắp Chiếu 3", "Phim Sắp Chiếu 4"};

        for (int i = 0; i < placeholderPosters.length; i++) {
            Movie movie = new Movie();
            movie.setTitle(titles[i]);
            movie.setPoster(placeholderPosters[i]);
            comingSoonList.add(movie);
        }
        comingSoonAdapter.notifyDataSetChanged();
    }


    private void loadPlaceholderNews() {
        newsList.clear();
        // Tạo dữ liệu mẫu kiểu News
        newsList.add(new News("Ưu đãi đặc biệt", "url_anh_1", "Nội dung ưu đãi 1"));
        newsList.add(new News("Mua 1 tặng 1", "url_anh_2", "Nội dung ưu đãi 2"));

        if (newsAdapter != null) {
            newsAdapter.notifyDataSetChanged();
        }
        String[] placeholderImages = {
                "https://via.placeholder.com/400x200/FF9800/FFFFFF?text=Uu+ Dai+1",
                "https://via.placeholder.com/400x200/E91E63/FFFFFF?text=Uu+ Dai+2",
                "https://via.placeholder.com/400x200/4CAF50/FFFFFF?text=Uu+ Dai+3",
                "https://via.placeholder.com/400x200/9C27B0/FFFFFF?text=Uu+ Dai+4"
        };
        String[] names = {
                "Ưu đãi đặc biệt cuối tuần - Giảm 30% vé xem phim",
                "Mua 1 tặng 1 cho khách hàng thân thiết",
                "Combo bỏng nước chỉ 49.000đ khi mua vé online",
                "Sự kiện ra mắt phim mới - Nhận quà tặng hấp dẫn"
        };

        for (int i = 0; i < placeholderImages.length; i++) {
            News news = new News();

            // SỬA TẠI ĐÂY: Gán trực tiếp vào thuộc tính public title và imageUrl của bạn
            news.title = names[i];
            news.imageUrl = placeholderImages[i]; // (Nếu dòng dưới của bạn kia bị lỗi poster)

            newsList.add(news);
        }
        newsAdapter.notifyDataSetChanged();
    }
    private void loadBannersFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Banner").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                bannerList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Banner banner = document.toObject(Banner.class);
                    bannerList.add(banner);
                }
                Collections.sort(bannerList, (b1, b2) -> Integer.compare(b1.getOrder(), b2.getOrder()));
                totalBanners = bannerList.size();
                if (totalBanners > 0) {
                    loadBannerImages();
                    showBanner(0);
                    startAutoSlide();
                } else {
                    loadPlaceholderBanners();
                }
            } else {
                loadPlaceholderBanners();
            }
        });
    }

    private void loadPlaceholderBanners() {
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
        for (int i = 0; i < bannerImages.length && i < bannerList.size(); i++) {
            String imageUrl = bannerList.get(i).getBanner();
            com.bumptech.glide.Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(bannerImages[i]);
        }
    }

    private void showBanner(int position) {
        // Fade out all banners
        for (ImageView banner : bannerImages) {
            if (banner.getVisibility() == View.VISIBLE) {
                AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                fadeOut.setDuration(400);
                fadeOut.setFillAfter(true);
                banner.startAnimation(fadeOut);
            }
            banner.setVisibility(View.GONE);
        }
        // Fade in the new active banner
        if (position < bannerImages.length && position < totalBanners && position >= 0) {
            ImageView activeBanner = bannerImages[position];
            activeBanner.setVisibility(View.VISIBLE);

            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(500);
            fadeIn.setFillAfter(true);
            activeBanner.startAnimation(fadeIn);
        }
        setupDots(position);
    }

    private void setupDots(int position) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setSelected(i == position);
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
        sliderHandler.postDelayed(slideRunnable, 5000);
    }

    private final Runnable slideRunnable = new Runnable() {
        @Override
        public void run() {
            if (totalBanners > 0) {
                currentPage = (currentPage + 1) % totalBanners;
                showBanner(currentPage);
                sliderHandler.postDelayed(this, 5000);
            }
        }
    };

    private void stopAutoSlide() {
        sliderHandler.removeCallbacks(slideRunnable);
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