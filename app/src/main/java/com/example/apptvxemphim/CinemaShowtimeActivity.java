package com.example.apptvxemphim;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CinemaShowtimeActivity extends AppCompatActivity {

    private LinearLayout layoutDateContainer, layoutMovieList;
    private FirebaseFirestore db;
    private String cinemaId, cinemaName;

    private List<Showtime> allShowtimes = new ArrayList<>();
    private Map<String, Movie> movieMap = new HashMap<>();
    private List<String> availableDates = new ArrayList<>();
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_showtime);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        cinemaId   = getIntent().getStringExtra("CINEMA_ID");
        cinemaName = getIntent().getStringExtra("CINEMA_NAME");

        TextView tvTitle = findViewById(R.id.tvCinemaTitle);
        tvTitle.setText(cinemaName);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        layoutDateContainer = findViewById(R.id.layoutDateContainer);
        layoutMovieList     = findViewById(R.id.layoutMovieList);
        db = FirebaseFirestore.getInstance();

        loadMovies();
    }

    private void loadMovies() {
        db.collection("Movie").get().addOnSuccessListener(snap -> {
            movieMap.clear();
            for (QueryDocumentSnapshot doc : snap) {
                Movie m = doc.toObject(Movie.class);
                movieMap.put(doc.getId(), m);
            }
            loadShowtimes();
        });
    }

    private void loadShowtimes() {
        db.collection("Showtime")
                .whereEqualTo("cinemaId", cinemaId)
                .get()
                .addOnSuccessListener(snap -> {
                    allShowtimes.clear();
                    Set<String> dateSet = new TreeSet<>((a, b) -> {
                        try {
                            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            return fmt.parse(a).compareTo(fmt.parse(b));
                        } catch (Exception e) { return a.compareTo(b); }
                    });
                    for (QueryDocumentSnapshot doc : snap) {
                        Showtime st = doc.toObject(Showtime.class);
                        allShowtimes.add(st);
                        if (st.getDate() != null) dateSet.add(st.getDate());
                    }
                    availableDates.addAll(dateSet);
                    buildDateButtons();
                    if (!availableDates.isEmpty()) {
                        selectedDate = availableDates.get(0);
                        highlightDate(0);
                        renderMovieList();
                    }
                });
    }

    private void buildDateButtons() {
        layoutDateContainer.removeAllViews();
        SimpleDateFormat inFmt  = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dayFmt = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat monFmt = new SimpleDateFormat("MM", Locale.getDefault());
        SimpleDateFormat dowFmt = new SimpleDateFormat("EEE", new Locale("vi", "VN"));

        // Thêm nút "Hôm nay"
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        for (int i = 0; i < availableDates.size(); i++) {
            LinearLayout btn = new LinearLayout(this);
            btn.setOrientation(LinearLayout.VERTICAL);
            btn.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(65), dp(65));
            lp.setMarginEnd(dp(6));
            btn.setLayoutParams(lp);
            btn.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
            btn.setPadding(dp(4), dp(4), dp(4), dp(4));
            btn.getBackground().setTint(Color.WHITE);

            TextView tvDay = new TextView(this);
            TextView tvDow = new TextView(this);
            tvDay.setGravity(android.view.Gravity.CENTER);
            tvDow.setGravity(android.view.Gravity.CENTER);
            tvDay.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvDow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            String dateStr = availableDates.get(i);
            boolean isToday = dateStr.equals(today);
            try {
                Date d = inFmt.parse(dateStr);
                if (isToday) {
                    tvDay.setText(dayFmt.format(d) + "/" + monFmt.format(d));
                    tvDow.setText("Hôm nay");
                } else {
                    tvDay.setText(dayFmt.format(d) + "/" + monFmt.format(d));
                    tvDow.setText(dowFmt.format(d));
                }
            } catch (Exception e) {
                tvDay.setText(dateStr.substring(0, 5));
                tvDow.setText("");
            }

            tvDay.setTextColor(Color.parseColor("#333333"));
            tvDay.setTypeface(null, android.graphics.Typeface.BOLD);
            tvDay.setTextSize(13);
            tvDow.setTextColor(Color.parseColor("#888888"));
            tvDow.setTextSize(10);
            btn.setTag(new TextView[]{tvDay, tvDow});
            btn.addView(tvDay);
            btn.addView(tvDow);

            final int idx = i;
            btn.setOnClickListener(v -> {
                selectedDate = availableDates.get(idx);
                highlightDate(idx);
                renderMovieList();
            });
            layoutDateContainer.addView(btn);
        }
    }

    private void highlightDate(int activeIdx) {
        for (int i = 0; i < layoutDateContainer.getChildCount(); i++) {
            LinearLayout btn = (LinearLayout) layoutDateContainer.getChildAt(i);
            TextView[] tags = (TextView[]) btn.getTag();
            boolean on = (i == activeIdx);
            btn.getBackground().setTint(on ? Color.parseColor("#F5C518") : Color.WHITE);
            tags[0].setTextColor(on ? Color.parseColor("#111111") : Color.parseColor("#333333"));
            tags[1].setTextColor(on ? Color.parseColor("#111111") : Color.parseColor("#888888"));
        }
    }

    private void renderMovieList() {
        layoutMovieList.removeAllViews();

        // Group showtime theo movieId
        Map<String, List<Showtime>> grouped = new LinkedHashMap<>();
        for (Showtime st : allShowtimes) {
            if (!st.getDate().equals(selectedDate)) continue;
            String key = st.getMovieId() != null ? st.getMovieId() : "unknown";
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(st);
        }

        if (grouped.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Không có suất chiếu ngày này");
            tv.setTextColor(Color.parseColor("#888888"));
            tv.setPadding(0, dp(32), 0, 0);
            layoutMovieList.addView(tv);
            return;
        }

        for (Map.Entry<String, List<Showtime>> entry : grouped.entrySet()) {
            Movie movie = movieMap.get(entry.getKey());
            layoutMovieList.addView(buildMovieCard(movie, entry.getValue()));
        }
    }

    private View buildMovieCard(Movie movie, List<Showtime> times) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
        card.getBackground().setTint(Color.WHITE);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        card.setLayoutParams(lp);

        // Hàng trên: poster + info
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        ImageView ivPoster = new ImageView(this);
        LinearLayout.LayoutParams posterLp = new LinearLayout.LayoutParams(dp(80), dp(110));
        posterLp.setMarginEnd(dp(12));
        ivPoster.setLayoutParams(posterLp);
        ivPoster.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (movie != null && movie.getPoster() != null) {
            Glide.with(this).load(movie.getPoster()).into(ivPoster);
        }

        LinearLayout infoCol = new LinearLayout(this);
        infoCol.setOrientation(LinearLayout.VERTICAL);
        infoCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(movie != null ? movie.getTitle() : "Phim chưa xác định");
        tvTitle.setTextColor(Color.parseColor("#111111"));
        tvTitle.setTextSize(14);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        // Thẻ thể loại
        LinearLayout tagRow = new LinearLayout(this);
        tagRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tagRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tagRowLp.topMargin = dp(6);
        tagRow.setLayoutParams(tagRowLp);

        if (movie != null && movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            TextView tvGenre = makeTag(movie.getGenres().get(0), "#E8F5E9", "#388E3C");
            tagRow.addView(tvGenre);
        }

        // Thẻ format (2D) + age
        // Thẻ format (2D) + age
        LinearLayout formatRow = new LinearLayout(this);
        formatRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams formatRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        formatRowLp.topMargin = dp(6);
        formatRow.setLayoutParams(formatRowLp);

        String fmt = movie != null && movie.getFormat() != null ? movie.getFormat() : "";
        TextView tv2D = makeTag(fmt, "#F5F5F5", "#333333");
        formatRow.addView(tv2D);

        if (movie != null && movie.getAge() != null) {
            LinearLayout.LayoutParams ageLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ageLp.setMarginStart(dp(6));
            TextView tvAge = makeTag(movie.getAge(), "#F5C518", "#111111");
            tvAge.setLayoutParams(ageLp);
            formatRow.addView(tvAge);
        }

        infoCol.addView(tvTitle);
        infoCol.addView(tagRow);
        infoCol.addView(formatRow);

        topRow.addView(ivPoster);
        topRow.addView(infoCol);
        card.addView(topRow);

        // Divider
        View div = new View(this);
        div.setBackgroundColor(Color.parseColor("#F0F0F0"));
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        divLp.topMargin = dp(10);
        divLp.bottomMargin = dp(6);
        div.setLayoutParams(divLp);
        card.addView(div);

        // Giờ chiếu — group theo ngôn ngữ
        Map<String, List<Showtime>> byLang = new LinkedHashMap<>();
        for (Showtime st : times) {
            String lang = st.getLanguage() != null ? st.getLanguage() : "Lồng tiếng";
            byLang.computeIfAbsent(lang, k -> new ArrayList<>()).add(st);
        }

        for (Map.Entry<String, List<Showtime>> le : byLang.entrySet()) {
            TextView tvLang = new TextView(this);
            String format = movie != null && movie.getFormat() != null ? movie.getFormat() : "";
            tvLang.setText(format + " " + le.getKey());
            tvLang.setTextColor(Color.parseColor("#333333"));
            tvLang.setTypeface(null, android.graphics.Typeface.BOLD);
            tvLang.setTextSize(13);
            LinearLayout.LayoutParams langLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            langLp.bottomMargin = dp(6);
            tvLang.setLayoutParams(langLp);
            card.addView(tvLang);

            com.google.android.flexbox.FlexboxLayout timeRow = new com.google.android.flexbox.FlexboxLayout(this);
            timeRow.setFlexWrap(com.google.android.flexbox.FlexWrap.WRAP);
            LinearLayout.LayoutParams timeRowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            timeRowLp.bottomMargin = dp(6);
            timeRow.setLayoutParams(timeRowLp);

            for (Showtime st : le.getValue()) {
                // Tính giờ kết thúc
                String startTime = st.getTime();
                String endTime = "";
                long dur = movie != null ? movie.getDuration() : 0;
                if (dur > 0 && startTime != null) {
                    try {
                        String[] parts = startTime.split(":");
                        int totalMin = Integer.parseInt(parts[0]) * 60
                                + Integer.parseInt(parts[1]) + (int) dur;
                        endTime = String.format("%02d:%02d", totalMin / 60 % 24, totalMin % 60);
                    } catch (Exception ignored) {}
                }

                TextView tvTime = new TextView(this);
                tvTime.setText(startTime + (endTime.isEmpty() ? "" : " ~ " + endTime));
                android.graphics.drawable.GradientDrawable timeBg = new android.graphics.drawable.GradientDrawable();
                timeBg.setColor(Color.parseColor("#EBF5FF"));
                timeBg.setCornerRadius(dp(6));
                tvTime.setBackground(timeBg);
                tvTime.setTextColor(Color.parseColor("#007AFF"));
                tvTime.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTime.setTextSize(13);
                tvTime.setPadding(dp(12), dp(6), dp(12), dp(6));
                LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                btnLp.setMarginEnd(dp(8));
                btnLp.bottomMargin = dp(8);
                tvTime.setLayoutParams(btnLp);

                final String stId = st.getShowtimeId();
                final String stTime = st.getTime();
                final String stDate = st.getDate();
                final String stLang = st.getLanguage();
                final String stHallId = st.getHallId();
                final String movieTitle = movie != null ? movie.getTitle() : "";
                final String movieId = movie != null ? movie.getId() : "";

                tvTime.setOnClickListener(v -> {
                    Intent intent = new Intent(this, SeatSelectionActivity.class);
                    intent.putExtra("SHOWTIME_ID", stId);
                    intent.putExtra("MOVIE_TITLE", movieTitle);
                    intent.putExtra("SHOWTIME_TIME", stTime);
                    intent.putExtra("SHOWTIME_DATE", stDate);
                    intent.putExtra("SHOWTIME_LANG", stLang);
                    intent.putExtra("HALL_ID", stHallId);
                    intent.putExtra("MOVIE_ID_FOR_FORMAT", movieId);
                    startActivity(intent);
                });
                timeRow.addView(tvTime);
            }
            card.addView(timeRow);
        }

        return card;
    }

    private TextView makeTag(String text, String bgHex, String textHex) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(textHex));
        tv.setTextSize(11);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tv.setPadding(dp(8), dp(3), dp(8), dp(3));
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.parseColor(bgHex));
        bg.setCornerRadius(dp(4));
        tv.setBackground(bg);
        return tv;
    }

    private int dp(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }
}