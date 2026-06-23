package com.example.apptvxemphim;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ShowtimeSelectionActivity extends AppCompatActivity {

    LinearLayout btnDate17, btnDate18, btnDate19;
    TextView tvNum17, tvTxt17, tvNum18, tvTxt18, tvNum19, tvTxt19;
    LinearLayout btnBrandAll, btnBrandCGV, btnBrandBeta;
    TextView imgAll, imgCGV, imgBeta;
    LinearLayout layoutCinemaList;
    LinearLayout btnLocation;

    private FirebaseFirestore db;
    private String movieId;
    private List<Showtime> allShowtimes = new ArrayList<>();
    private Map<String, Cinema> cinemaMap = new HashMap<>();
    private List<String> availableDates = new ArrayList<>();
    private String selectedDate = "";
    private String selectedBrand = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtime_selection);

        movieId = getIntent().getStringExtra("MOVIE_ID");
        if (movieId == null) {
            Toast.makeText(this, "Lỗi: Không có ID phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        btnLocation      = findViewById(R.id.btnLocation);
        btnDate17        = findViewById(R.id.btnDate17);
        btnDate18        = findViewById(R.id.btnDate18);
        btnDate19        = findViewById(R.id.btnDate19);
        tvNum17 = findViewById(R.id.tvNum17); tvTxt17 = findViewById(R.id.tvTxt17);
        tvNum18 = findViewById(R.id.tvNum18); tvTxt18 = findViewById(R.id.tvTxt18);
        tvNum19 = findViewById(R.id.tvNum19); tvTxt19 = findViewById(R.id.tvTxt19);
        btnBrandAll      = findViewById(R.id.btnBrandAll);
        btnBrandCGV      = findViewById(R.id.btnBrandCGV);
        btnBrandBeta     = findViewById(R.id.btnBrandBeta);
        imgAll  = findViewById(R.id.imgAll);
        imgCGV  = findViewById(R.id.imgCGV);
        imgBeta = findViewById(R.id.imgBeta);
        layoutCinemaList = findViewById(R.id.layoutCinemaList);

        btnBrandAll.setOnClickListener(v  -> { selectedBrand = "ALL";  highlightBrand("ALL");  renderCinemaList(); });
        btnBrandCGV.setOnClickListener(v  -> { selectedBrand = "CGV";  highlightBrand("CGV");  renderCinemaList(); });
        btnBrandBeta.setOnClickListener(v -> { selectedBrand = "BETA"; highlightBrand("BETA"); renderCinemaList(); });

        loadCinemas();
    }

    private void loadCinemas() {
        db.collection("Cinema").get()
                .addOnSuccessListener(snap -> {
                    cinemaMap.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Cinema c = doc.toObject(Cinema.class);
                        cinemaMap.put(doc.getId(), c);
                    }
                    loadShowtimes();
                })
                .addOnFailureListener(e -> loadShowtimes()); // nếu chưa có cinemas thì vẫn chạy tiếp
    }

    private void loadShowtimes() {
        db.collection("Showtime")
                .whereEqualTo("movieId", movieId)
                .get()
                .addOnSuccessListener(snap -> {
                    allShowtimes.clear();
                    availableDates.clear();
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
                        highlightDateButton(0);
                        renderCinemaList();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải suất chiếu: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void buildDateButtons() {
        LinearLayout[] btns = {btnDate17, btnDate18, btnDate19};
        TextView[] nums     = {tvNum17, tvNum18, tvNum19};
        TextView[] txts     = {tvTxt17, tvTxt18, tvTxt19};

        for (LinearLayout b : btns) b.setVisibility(View.GONE);

        // Date trong DB là "26/06/2026"
        SimpleDateFormat inFmt  = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dayFmt = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat dowFmt = new SimpleDateFormat("EEE", new Locale("vi", "VN"));

        for (int i = 0; i < Math.min(availableDates.size(), 3); i++) {
            try {
                Date d = inFmt.parse(availableDates.get(i));
                nums[i].setText(dayFmt.format(d));
                txts[i].setText(dowFmt.format(d));
            } catch (Exception e) {
                nums[i].setText(availableDates.get(i).substring(0, 2));
                txts[i].setText("");
            }
            btns[i].setVisibility(View.VISIBLE);
            final int idx = i;
            btns[i].setOnClickListener(v -> {
                selectedDate = availableDates.get(idx);
                highlightDateButton(idx);
                renderCinemaList();
            });
        }
    }

    private void renderCinemaList() {
        layoutCinemaList.removeAllViews();

        List<Showtime> filtered = new ArrayList<>();
        for (Showtime st : allShowtimes) {
            if (!st.getDate().equals(selectedDate)) continue;
            if (!selectedBrand.equals("ALL")) {
                // Lọc theo tên rạp chứa từ khóa hãng

                Cinema c = cinemaMap.get(st.getCinemaId());
                if (c == null) continue;
                if (!c.getName().toUpperCase().contains(selectedBrand)) continue;
            }
            filtered.add(st);
        }

        // Group theo cinemaId (hoặc cinema_name nếu chưa có cinemaId)
        Map<String, List<Showtime>> grouped = new LinkedHashMap<>();
        for (Showtime st : filtered) {
            String key = st.getCinemaId() != null ? st.getCinemaId() : "unknown";
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(st);
        }

        if (grouped.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("Không có suất chiếu ngày này");
            tv.setTextColor(Color.parseColor("#888888"));
            tv.setPadding(0, dp(32), 0, 0);
            layoutCinemaList.addView(tv);
            return;
        }

        for (Map.Entry<String, List<Showtime>> entry : grouped.entrySet()) {
            String key = entry.getKey();
            Cinema cinema = cinemaMap.get(key);

            layoutCinemaList.addView(buildCinemaCard(cinema, entry.getValue()));
        }
    }

    private View buildCinemaCard(Cinema cinema, List<Showtime> times) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
        card.getBackground().setTint(Color.WHITE);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        card.setLayoutParams(lp);

        // Tên rạp: ưu tiên từ cinemaMap, fallback về cinema_name trong Showtime
        TextView tvName = new TextView(this);
        tvName.setText(cinema != null ? cinema.getName() : "Rạp chưa xác định");
        tvName.setTextColor(Color.parseColor("#222222"));
        tvName.setTextSize(15);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tvName);

        // Địa chỉ
        if (cinema != null && cinema.getAddress() != null) {
            TextView tvAddr = new TextView(this);
            tvAddr.setText(cinema.getAddress());
            tvAddr.setTextColor(Color.parseColor("#777777"));
            tvAddr.setTextSize(12);
            tvAddr.setPadding(0, dp(2), 0, dp(10));
            card.addView(tvAddr);
        }

        // Divider
        View div = new View(this);
        div.setBackgroundColor(Color.parseColor("#F0F0F0"));
        div.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
        card.addView(div);

        // Group theo ngôn ngữ
        Map<String, List<Showtime>> byLang = new LinkedHashMap<>();
        for (Showtime st : times) {
            String lang = st.getLanguage() != null ? st.getLanguage() : "Lồng tiếng";
            byLang.computeIfAbsent(lang, k -> new ArrayList<>()).add(st);
        }

        for (Map.Entry<String, List<Showtime>> le : byLang.entrySet()) {
            TextView tvLang = new TextView(this);
            tvLang.setText("2D " + le.getKey());
            tvLang.setTextColor(Color.parseColor("#111111"));
            tvLang.setTypeface(null, android.graphics.Typeface.BOLD);
            tvLang.setPadding(0, dp(10), 0, dp(8));
            card.addView(tvLang);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dp(8);
            row.setLayoutParams(rowLp);

            for (Showtime st : le.getValue()) {
                TextView tvTime = new TextView(this);
                tvTime.setText(st.getTime());
                tvTime.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
                tvTime.getBackground().setTint(Color.parseColor("#EBF5FF"));
                tvTime.setTextColor(Color.parseColor("#007AFF"));
                tvTime.setTypeface(null, android.graphics.Typeface.BOLD);
                tvTime.setPadding(dp(12), dp(6), dp(12), dp(6));
                LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                btnLp.setMarginEnd(dp(8));
                tvTime.setLayoutParams(btnLp);

                final String stId = st.getShowtimeId();
                tvTime.setOnClickListener(v -> {
                    Intent intent = new Intent(this, SeatSelectionActivity.class);
                    intent.putExtra("SHOWTIME_ID", stId);
                    startActivity(intent);
                });
                row.addView(tvTime);
            }
            card.addView(row);
        }
        return card;
    }

    private void highlightDateButton(int active) {
        LinearLayout[] btns = {btnDate17, btnDate18, btnDate19};
        TextView[] nums = {tvNum17, tvNum18, tvNum19};
        TextView[] txts = {tvTxt17, tvTxt18, tvTxt19};
        for (int i = 0; i < 3; i++) {
            boolean on = (i == active);
            btns[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    on ? Color.parseColor("#E91E63") : Color.WHITE));
            nums[i].setTextColor(on ? Color.WHITE : Color.parseColor("#333333"));
            txts[i].setTextColor(on ? Color.WHITE : Color.parseColor("#888888"));
        }
    }

    private void highlightBrand(String brand) {
        imgAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        imgAll.setTextColor(Color.parseColor("#E91E63"));
        imgCGV.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        imgCGV.setTextColor(Color.parseColor("#E51937"));
        imgBeta.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        imgBeta.setTextColor(Color.parseColor("#00BCD4"));

        TextView t = brand.equals("CGV") ? imgCGV : brand.equals("BETA") ? imgBeta : imgAll;
        t.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E91E63")));
        t.setTextColor(Color.WHITE);
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}