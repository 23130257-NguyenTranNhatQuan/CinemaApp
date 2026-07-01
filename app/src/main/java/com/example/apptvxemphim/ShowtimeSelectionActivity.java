package com.example.apptvxemphim;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.*;
import java.text.SimpleDateFormat;
import java.util.*;
import com.bumptech.glide.Glide;
import android.widget.ImageView;

public class ShowtimeSelectionActivity extends AppCompatActivity {

    LinearLayout layoutDateContainer;

    LinearLayout layoutCinemaList;
    LinearLayout btnLocation;

    private FirebaseFirestore db;
    private String movieId;
    private List<Showtime> allShowtimes = new ArrayList<>();
    private Map<String, Cinema> cinemaMap = new HashMap<>();
    private List<String> availableDates = new ArrayList<>();
    private String selectedDate = "";
    private String selectedBrand = "ALL";
    private long movieDuration = 0;
    private String movieFormat = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtime_selection);
        layoutDateContainer = findViewById(R.id.layoutDateContainer);
        movieId = getIntent().getStringExtra("MOVIE_ID");
        if (movieId == null) {
            Toast.makeText(this, "Lỗi: Không có ID phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        btnLocation      = findViewById(R.id.btnLocation);

        layoutCinemaList = findViewById(R.id.layoutCinemaList);



        loadCinemas();

        // Lấy tên phim từ Firebase
        TextView tvMovieTitle = findViewById(R.id.tvMovieTitle);
        db.collection("Movie").document(movieId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String title = doc.getString("title");
                        tvMovieTitle.setText("Lịch chiếu " + (title != null ? title : ""));
                        Long dur = doc.getLong("duration");
                        if (dur != null) movieDuration = dur;
                        String fmt = doc.getString("format");
                        if (fmt != null) movieFormat = fmt;
                    }
                });
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
                    buildBrandButtons();
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

    private void buildBrandButtons() {
        // Lấy LinearLayout chứa các nút hãng trong XML
        LinearLayout brandContainer = findViewById(R.id.brandContainer); // tạo ID này ở bước sau
        brandContainer.removeAllViews();

        // Lấy danh sách hãng duy nhất từ cinemaMap của các showtime
        Set<String> brands = new LinkedHashSet<>();
        brands.add("ALL"); // luôn có nút Tất cả đầu tiên
        for (Showtime st : allShowtimes) {
            Cinema c = cinemaMap.get(st.getCinemaId());
            if (c != null && c.getName() != null) {

                String brandName = c.getBrand() != null ? c.getBrand() : c.getName().split(" ")[0];
                brands.add(brandName);
            }
        }

        for (String brand : brands) {
            LinearLayout btnBrand = new LinearLayout(this);
            btnBrand.setOrientation(LinearLayout.VERTICAL);
            btnBrand.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(16));
            btnBrand.setLayoutParams(lp);

            ImageView tvIcon = new ImageView(this);
            tvIcon.setLayoutParams(new LinearLayout.LayoutParams(dp(48), dp(48)));
            tvIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            tvIcon.setPadding(dp(4), dp(4), dp(4), dp(4));
            tvIcon.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
            tvIcon.getBackground().setTint(Color.WHITE);

            TextView tvLabel = new TextView(this);
            tvLabel.setTextSize(11);
            tvLabel.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelLp.topMargin = dp(4);
            tvLabel.setLayoutParams(labelLp);

            if (brand.equals("ALL")) {
                tvIcon.setImageResource(android.R.drawable.btn_star_big_on);
                tvIcon.getBackground().setTint(Color.parseColor("#E91E63"));
                tvLabel.setText("Tất cả");
            } else {
                // Tìm cinema đầu tiên có brand này để lấy logo
                String logoUrl = null;
                for (Cinema c : cinemaMap.values()) {
                    String b = c.getBrand() != null ? c.getBrand() : "";
                    if (b.equalsIgnoreCase(brand) && c.getLogo() != null) {
                        logoUrl = c.getLogo();
                        break;
                    }
                }
                if (logoUrl != null) {
                    Glide.with(this).load(logoUrl).into(tvIcon);
                } else {
                    tvIcon.setImageResource(android.R.drawable.ic_menu_report_image);
                }
                tvIcon.getBackground().setTint(Color.WHITE);
                tvLabel.setText(brand);
            }

            btnBrand.addView(tvIcon);
            btnBrand.addView(tvLabel);

            final String selectedBrandKey = brand;
            btnBrand.setOnClickListener(v -> {
                selectedBrand = brand.equals("ALL") ? "ALL" : brand;
                // Highlight nút được chọn
                for (int i = 0; i < brandContainer.getChildCount(); i++) {
                    LinearLayout b = (LinearLayout) brandContainer.getChildAt(i);
                    View icon = b.getChildAt(0);  // ← dùng View thay TextView
                    icon.getBackground().setTint(Color.WHITE);
                }
                tvIcon.getBackground().setTint(Color.parseColor("#E91E63"));
                renderCinemaList();
            });

            brandContainer.addView(btnBrand);
        }

        // Mặc định highlight "Tất cả"
        if (brandContainer.getChildCount() > 0) {
            LinearLayout first = (LinearLayout) brandContainer.getChildAt(0);
            View icon = first.getChildAt(0);
            icon.getBackground().setTint(Color.parseColor("#E91E63"));

        }
    }
    private void buildDateButtons() {
        layoutDateContainer.removeAllViews();

        SimpleDateFormat inFmt  = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dayFmt = new SimpleDateFormat("dd/MM", Locale.getDefault());
        SimpleDateFormat dowFmt = new SimpleDateFormat("EEE", new Locale("vi", "VN"));

        for (int i = 0; i < availableDates.size(); i++) {
            LinearLayout btn = new LinearLayout(this);
            btn.setOrientation(LinearLayout.VERTICAL);
            btn.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(65), dp(65));
            lp.setMarginEnd(dp(8));
            btn.setLayoutParams(lp);
            btn.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
            btn.getBackground().setTint(Color.WHITE);

            TextView tvNum = new TextView(this);
            TextView tvTxt = new TextView(this);

            try {
                Date d = inFmt.parse(availableDates.get(i));
                tvNum.setText(dayFmt.format(d));
                tvTxt.setText(dowFmt.format(d));
            } catch (Exception e) {
                tvNum.setText(availableDates.get(i).substring(0, 2));
                tvTxt.setText("");
            }

            tvNum.setTextColor(Color.parseColor("#333333"));
            tvNum.setTypeface(null, android.graphics.Typeface.BOLD);
            tvNum.setTextSize(13);
            tvTxt.setTextColor(Color.parseColor("#888888"));
            tvTxt.setTextSize(10);
            tvNum.setGravity(android.view.Gravity.CENTER);
            tvTxt.setGravity(android.view.Gravity.CENTER);
            tvNum.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            tvTxt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            btn.addView(tvNum);
            btn.addView(tvTxt);
            btn.setTag(new TextView[]{tvNum, tvTxt});

            final int idx = i;
            btn.setOnClickListener(v -> {
                selectedDate = availableDates.get(idx);
                highlightDateButton(idx);
                renderCinemaList();
            });

            layoutDateContainer.addView(btn);
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
                String cinemaBrand = c.getBrand() != null ? c.getBrand() : c.getName().split(" ")[0];
                if (!cinemaBrand.equalsIgnoreCase(selectedBrand)) continue;
            }
            filtered.add(st);
        }

        // Group theo cinemaId
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

        // Tự động mở card đầu tiên
        if (layoutCinemaList.getChildCount() > 0) {
            LinearLayout firstCard = (LinearLayout) layoutCinemaList.getChildAt(0);
            View last = firstCard.getChildAt(firstCard.getChildCount() - 1);
            if (last instanceof LinearLayout) last.setVisibility(View.VISIBLE);
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
        // Hàng ngang: logo + tên + địa chỉ
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        headerLp.bottomMargin = dp(8);
        headerRow.setLayoutParams(headerLp);

// Logo rạp
        ImageView ivLogo = new ImageView(this);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        logoLp.setMarginEnd(dp(10));
        ivLogo.setLayoutParams(logoLp);
        ivLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (cinema != null && cinema.getLogo() != null && !cinema.getLogo().isEmpty()) {
            Glide.with(this).load(cinema.getLogo()).into(ivLogo);
        } else {
            ivLogo.setImageResource(android.R.drawable.ic_menu_report_image);
        }

// Cột tên + địa chỉ
        LinearLayout infoCol = new LinearLayout(this);
        infoCol.setOrientation(LinearLayout.VERTICAL);

        TextView tvName = new TextView(this);
        tvName.setText(cinema != null ? cinema.getName() : "Rạp chưa xác định");
        tvName.setTextColor(Color.parseColor("#222222"));
        tvName.setTextSize(15);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

        infoCol.addView(tvName);

        if (cinema != null && cinema.getAddress() != null) {
            TextView tvAddr = new TextView(this);
            tvAddr.setText(cinema.getAddress());
            tvAddr.setTextColor(Color.parseColor("#777777"));
            tvAddr.setTextSize(12);
            tvAddr.setPadding(0, dp(2), 0, 0);
            infoCol.addView(tvAddr);
        }

        if (cinema != null && cinema.getLatitude() != null && cinema.getLongitude() != null) {
            TextView tvMapLink = new TextView(this);
            tvMapLink.setText("[ Bản đồ ]");
            tvMapLink.setTextColor(Color.parseColor("#007AFF"));
            tvMapLink.setTextSize(12);
            tvMapLink.setPadding(0, dp(2), 0, 0);
            tvMapLink.setOnClickListener(v -> {
                Intent intent = new Intent(this, CinemaMapActivity.class);
                intent.putExtra("CINEMA_NAME", cinema.getName());
                intent.putExtra("CINEMA_ADDRESS", cinema.getAddress());
                intent.putExtra("CINEMA_LAT", cinema.getLatitude());
                intent.putExtra("CINEMA_LNG", cinema.getLongitude());
                startActivity(intent);
            });
            infoCol.addView(tvMapLink);
        }

        headerRow.addView(ivLogo);
        headerRow.addView(infoCol);
        card.addView(headerRow);





        // Divider
        View div = new View(this);
        div.setBackgroundColor(Color.parseColor("#F0F0F0"));
        div.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
        card.addView(div);

// Ẩn phần giờ chiếu mặc định
        LinearLayout showtimeContent = new LinearLayout(this);
        showtimeContent.setOrientation(LinearLayout.VERTICAL);
        showtimeContent.setVisibility(View.GONE); // mặc định ẩn

        // Group theo ngôn ngữ
        Map<String, List<Showtime>> byLang = new LinkedHashMap<>();
        for (Showtime st : times) {
            String lang = st.getLanguage() != null ? st.getLanguage() : "Lồng tiếng";
            byLang.computeIfAbsent(lang, k -> new ArrayList<>()).add(st);
        }

        for (Map.Entry<String, List<Showtime>> le : byLang.entrySet()) {
            TextView tvLang = new TextView(this);
            String prefix = (movieFormat != null && !movieFormat.isEmpty()) ? movieFormat + " " : "";
            tvLang.setText(prefix + le.getKey());
            tvLang.setTextColor(Color.parseColor("#111111"));
            tvLang.setTypeface(null, android.graphics.Typeface.BOLD);
            tvLang.setPadding(0, dp(10), 0, dp(8));


            com.google.android.flexbox.FlexboxLayout row = new com.google.android.flexbox.FlexboxLayout(this);
            row.setFlexWrap(com.google.android.flexbox.FlexWrap.WRAP);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dp(8);
            row.setLayoutParams(rowLp);

            for (Showtime st : le.getValue()) {
                TextView tvTime = new TextView(this);
                String startTime = st.getTime();
                String endTime = "";
                if (movieDuration > 0 && startTime != null) {
                    try {
                        String[] parts = startTime.split(":");
                        int totalMin = Integer.parseInt(parts[0]) * 60
                                + Integer.parseInt(parts[1])
                                + (int) movieDuration;
                        endTime = String.format("%02d:%02d", totalMin / 60 % 24, totalMin % 60);
                    } catch (Exception ignored) {}
                }
                tvTime.setText(startTime + (endTime.isEmpty() ? "" : " ~ " + endTime));
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
                final String stTime = st.getTime();
                final String stDate = st.getDate();
                final String stLang = st.getLanguage();
                final String cinemaName = cinema != null ? cinema.getName() : "";
                final String stHallId = st.getHallId();

                tvTime.setOnClickListener(v -> {
                    Intent intent = new Intent(this, SeatSelectionActivity.class);
                    intent.putExtra("SHOWTIME_ID", stId);
                    intent.putExtra("MOVIE_TITLE", ((TextView)findViewById(R.id.tvMovieTitle)).getText().toString().replace("Lịch chiếu ", ""));
                    intent.putExtra("SHOWTIME_TIME", stTime);
                    intent.putExtra("SHOWTIME_DATE", stDate);
                    intent.putExtra("SHOWTIME_LANG", stLang);
                    intent.putExtra("HALL_ID", stHallId);
                    intent.putExtra("MOVIE_ID_FOR_FORMAT", movieId);
                    startActivity(intent);
                });
                row.addView(tvTime);
            }
            showtimeContent.addView(tvLang);
            showtimeContent.addView(row);
        }


        card.addView(showtimeContent);

// Bấm vào card → toggle
        card.setOnClickListener(v -> {
            boolean isShowing = showtimeContent.getVisibility() == View.VISIBLE;
            // Đóng tất cả card khác trước
            for (int i = 0; i < layoutCinemaList.getChildCount(); i++) {
                View child = layoutCinemaList.getChildAt(i);
                if (child instanceof LinearLayout) {
                    // Tìm showtimeContent bên trong card đó và ẩn đi
                    LinearLayout childCard = (LinearLayout) child;
                    if (childCard.getChildCount() > 0) {
                        View last = childCard.getChildAt(childCard.getChildCount() - 1);
                        if (last instanceof LinearLayout) last.setVisibility(View.GONE);
                    }
                }
            }
            // Toggle card hiện tại
            showtimeContent.setVisibility(isShowing ? View.GONE : View.VISIBLE);
        });

        return card;
    }

    private void highlightDateButton(int activeIdx) {
        for (int i = 0; i < layoutDateContainer.getChildCount(); i++) {
            LinearLayout btn = (LinearLayout) layoutDateContainer.getChildAt(i);
            TextView[] tags = (TextView[]) btn.getTag();
            boolean on = (i == activeIdx);
            btn.getBackground().setTint(on ? Color.parseColor("#E91E63") : Color.WHITE);
            tags[0].setTextColor(on ? Color.WHITE : Color.parseColor("#333333"));
            tags[1].setTextColor(on ? Color.WHITE : Color.parseColor("#888888"));
        }
    }



    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }


}