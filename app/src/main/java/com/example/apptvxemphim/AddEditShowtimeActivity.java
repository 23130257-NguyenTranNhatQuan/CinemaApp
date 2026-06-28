package com.example.apptvxemphim;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.*;
import java.util.*;

public class AddEditShowtimeActivity extends AppCompatActivity {

    private Spinner spinnerMovie, spinnerCinema, spinnerHall, spinnerLanguage;
    private Button btnPickDate, btnPickTime, btnSave;
    private TextView tvTitle;

    private FirebaseFirestore db;
    private String selectedDate = "", selectedTime = "";
    private String editShowtimeId = null;

    private List<Movie>  movieList     = new ArrayList<>();
    private List<Cinema> cinemaList    = new ArrayList<>();
    private List<Hall>   hallList      = new ArrayList<>();
    private List<Hall>   filteredHalls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_showtime);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = FirebaseFirestore.getInstance();
        editShowtimeId = getIntent().getStringExtra("SHOWTIME_ID");

        tvTitle         = findViewById(R.id.tv_title_add_showtime);
        spinnerMovie    = findViewById(R.id.spinner_movie);
        spinnerCinema   = findViewById(R.id.spinner_cinema);
        spinnerHall     = findViewById(R.id.spinner_hall);
        spinnerLanguage = findViewById(R.id.spinner_language);
        btnPickDate     = findViewById(R.id.btn_pick_date);
        btnPickTime     = findViewById(R.id.btn_pick_time);
        btnSave         = findViewById(R.id.btn_save_showtime);

        tvTitle.setText(editShowtimeId != null ? "Sửa suất chiếu" : "Thêm suất chiếu");

        findViewById(R.id.btn_back_add_showtime).setOnClickListener(v -> finish());

        // Spinner ngôn ngữ
        ArrayAdapter<String> langAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Lồng tiếng", "Phụ đề"});
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(langAdapter);

        // DatePicker
        btnPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate = String.format("%02d/%02d/%04d", d, m + 1, y);
                btnPickDate.setText("📅 " + selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // TimePicker
        btnPickTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, min) -> {
                selectedTime = String.format("%02d:%02d", h, min);
                btnPickTime.setText("🕒 " + selectedTime);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        // Khi chọn Cinema → lọc Hall
        spinnerCinema.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
                if (!cinemaList.isEmpty())
                    filterHalls(cinemaList.get(pos).getCinemaId());
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        btnSave.setOnClickListener(v -> saveShowtime());

        loadData();
    }

    private void loadData() {
        db.collection("Movie").get().addOnSuccessListener(snap -> {
            movieList.clear();
            List<String> names = new ArrayList<>();
            for (QueryDocumentSnapshot doc : snap) {
                movieList.add(doc.toObject(Movie.class));
                names.add(doc.getString("title"));
            }
            ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
            a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerMovie.setAdapter(a);

            db.collection("Cinema").get().addOnSuccessListener(snap2 -> {
                cinemaList.clear();
                List<String> cNames = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snap2) {
                    cinemaList.add(doc.toObject(Cinema.class));
                    cNames.add(doc.getString("name"));
                }
                ArrayAdapter<String> ca = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cNames);
                ca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCinema.setAdapter(ca);

                db.collection("Hall").get().addOnSuccessListener(snap3 -> {
                    hallList.clear();
                    for (QueryDocumentSnapshot doc : snap3)
                        hallList.add(Hall.fromDocument(doc));

                    if (!cinemaList.isEmpty())
                        filterHalls(cinemaList.get(0).getCinemaId());

                    if (editShowtimeId != null) loadExisting();
                });
            });
        });
    }

    private void filterHalls(String cinemaId) {
        filteredHalls.clear();
        List<String> names = new ArrayList<>();
        for (Hall h : hallList) {
            if (cinemaId.equals(h.cinemaId)) {
                filteredHalls.add(h);
                names.add(h.name);
            }
        }
        ArrayAdapter<String> ha = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        ha.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHall.setAdapter(ha);
    }

    private void loadExisting() {
        db.collection("Showtime").document(editShowtimeId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    Showtime st = doc.toObject(Showtime.class);

                    for (int i = 0; i < movieList.size(); i++)
                        if (movieList.get(i).getId().equals(st.getMovieId())) {
                            spinnerMovie.setSelection(i); break;
                        }

                    for (int i = 0; i < cinemaList.size(); i++)
                        if (cinemaList.get(i).getCinemaId().equals(st.getCinemaId())) {
                            spinnerCinema.setSelection(i);
                            filterHalls(st.getCinemaId());
                            break;
                        }

                    for (int i = 0; i < filteredHalls.size(); i++)
                        if (filteredHalls.get(i).hallId.equals(st.getHallId())) {
                            spinnerHall.setSelection(i); break;
                        }

                    selectedDate = st.getDate();
                    selectedTime = st.getTime();
                    btnPickDate.setText("📅 " + selectedDate);
                    btnPickTime.setText("🕒 " + selectedTime);

                    String lang = st.getLanguage();
                    spinnerLanguage.setSelection(
                            lang != null && lang.equals("Phụ đề") ? 1 : 0);
                });
    }

    private void saveShowtime() {
        // Validate
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày chiếu", Toast.LENGTH_SHORT).show(); return;
        }
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn giờ chiếu", Toast.LENGTH_SHORT).show(); return;
        }
        if (filteredHalls.isEmpty()) {
            Toast.makeText(this, "Không có phòng chiếu", Toast.LENGTH_SHORT).show(); return;
        }

        Movie  movie  = movieList.get(spinnerMovie.getSelectedItemPosition());
        Cinema cinema = cinemaList.get(spinnerCinema.getSelectedItemPosition());
        Hall   hall   = filteredHalls.get(spinnerHall.getSelectedItemPosition());
        String lang   = spinnerLanguage.getSelectedItem().toString();

        // Kiểm tra trùng giờ trước khi lưu
        checkOverlapThenSave(movie, cinema, hall, lang);
    }

    private void checkOverlapThenSave(Movie movie, Cinema cinema, Hall hall, String lang) {
        db.collection("Showtime")
                .whereEqualTo("hallId", hall.hallId)
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener(snap -> {
                    // Tính giờ kết thúc của suất mới
                    int newStart = timeToMinutes(selectedTime);
                    int newEnd   = newStart + (int) movie.getDuration();

                    for (QueryDocumentSnapshot doc : snap) {
                        // Bỏ qua chính nó khi edit
                        if (editShowtimeId != null && doc.getId().equals(editShowtimeId)) continue;

                        String existTime = doc.getString("time");
                        String existMovieId = doc.getString("movieId");
                        if (existTime == null) continue;

                        int existStart = timeToMinutes(existTime);
                        // Lấy duration phim cũ (nếu có trong movieList)
                        int existDur = 120; // mặc định 2h nếu không tìm thấy
                        for (Movie m : movieList)
                            if (m.getId().equals(existMovieId)) { existDur = (int) m.getDuration(); break; }

                        int existEnd = existStart + existDur;

                        // Kiểm tra chồng lấp
                        if (newStart < existEnd && newEnd > existStart) {
                            Toast.makeText(this,
                                    "❌ Phòng đã có suất chiếu " + existTime + " ~ " +
                                            minutesToTime(existEnd) + " trong ngày này!",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    // Không trùng → lưu
                    doSave(movie.getId(), cinema.getCinemaId(), hall.hallId, lang);
                });
    }

    private void doSave(String movieId, String cinemaId, String hallId, String lang) {
        Map<String, Object> data = new HashMap<>();
        data.put("movieId",  movieId);
        data.put("cinemaId", cinemaId);
        data.put("hallId",   hallId);
        data.put("date",     selectedDate);
        data.put("time",     selectedTime);
        data.put("language", lang);

        if (editShowtimeId != null) {
            db.collection("Showtime").document(editShowtimeId).update(data)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this, "✅ Đã cập nhật suất chiếu!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            db.collection("Showtime").add(data)
                    .addOnSuccessListener(ref -> {
                        Toast.makeText(this, "✅ Đã thêm suất chiếu!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private int timeToMinutes(String time) {
        try {
            String[] p = time.split(":");
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) { return 0; }
    }

    private String minutesToTime(int minutes) {
        return String.format("%02d:%02d", (minutes / 60) % 24, minutes % 60);
    }
}