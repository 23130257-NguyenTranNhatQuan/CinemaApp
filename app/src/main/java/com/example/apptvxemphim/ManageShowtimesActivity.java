package com.example.apptvxemphim;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.*;
import java.util.*;

public class ManageShowtimesActivity extends AppCompatActivity {

    private RecyclerView rcv;
    private AdminShowtimeAdapter adapter;
    private List<Showtime> showtimeList = new ArrayList<>();
    private List<Showtime> allShowtimes = new ArrayList<>();

    private Map<String, String> movieNames   = new HashMap<>();
    private Map<String, String> cinemaNames  = new HashMap<>();
    private Map<String, String> hallNames    = new HashMap<>();
    private Map<String, String> movieFormats = new HashMap<>();

    private List<Movie>  movieList   = new ArrayList<>();
    private List<Cinema> cinemaList  = new ArrayList<>();
    private List<Hall>   hallList    = new ArrayList<>();
    private List<Hall>   filteredHalls = new ArrayList<>();

    private Spinner spinnerFilterMovie, spinnerFilterCinema, spinnerFilterHall;
    private Button btnFilterDate;

    private String filterMovieId  = "";
    private String filterCinemaId = "";
    private String filterHallId   = "";
    private String filterDate     = "";

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_showtimes);

        db = FirebaseFirestore.getInstance();

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        findViewById(R.id.btn_back_showtime).setOnClickListener(v -> finish());

        rcv = findViewById(R.id.rcv_showtimes);
        rcv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminShowtimeAdapter(showtimeList, movieNames, cinemaNames, hallNames, movieFormats);
        rcv.setAdapter(adapter);

        spinnerFilterMovie  = findViewById(R.id.spinner_filter_movie);
        spinnerFilterCinema = findViewById(R.id.spinner_filter_cinema);
        spinnerFilterHall   = findViewById(R.id.spinner_filter_hall);
        btnFilterDate       = findViewById(R.id.btn_filter_date);

        // Nút chọn ngày
        btnFilterDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                filterDate = String.format("%02d/%02d/%04d", d, m + 1, y);
                btnFilterDate.setText(filterDate);
                applyFilter();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // FAB
        FloatingActionButton fab = findViewById(R.id.fab_add_showtime);
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditShowtimeActivity.class)));

        loadAllData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!movieList.isEmpty()) loadShowtimes();
    }

    private void loadAllData() {
        db.collection("Movie").get().addOnSuccessListener(snap -> {
            movieList.clear();
            for (QueryDocumentSnapshot doc : snap) {
                Movie m = doc.toObject(Movie.class);
                movieList.add(m);
                movieNames.put(doc.getId(), doc.getString("title"));
                String fmt = doc.getString("format");
                movieFormats.put(doc.getId(), fmt != null ? fmt : "");
            }
            db.collection("Cinema").get().addOnSuccessListener(snap2 -> {
                cinemaList.clear();
                for (QueryDocumentSnapshot doc : snap2) {
                    Cinema c = doc.toObject(Cinema.class);
                    cinemaList.add(c);
                    cinemaNames.put(doc.getId(), doc.getString("name"));
                }
                db.collection("Hall").get().addOnSuccessListener(snap3 -> {
                    hallList.clear();
                    for (QueryDocumentSnapshot doc : snap3) {
                        Hall h = Hall.fromDocument(doc);
                        hallList.add(h);
                        hallNames.put(doc.getId(), h.name);
                    }
                    setupFilterSpinners();
                    loadShowtimes();
                });
            });
        });
    }

    private void setupFilterSpinners() {
        // Movie
        List<String> mNames = new ArrayList<>();
        mNames.add("Tất cả phim");
        for (Movie m : movieList) mNames.add(m.getTitle());
        ArrayAdapter<String> ma = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mNames);
        ma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterMovie.setAdapter(ma);
        spinnerFilterMovie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
                filterMovieId = pos == 0 ? "" : movieList.get(pos - 1).getId();
                applyFilter();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // Cinema
        List<String> cNames = new ArrayList<>();
        cNames.add("Tất cả rạp");
        for (Cinema c : cinemaList) cNames.add(c.getName());
        ArrayAdapter<String> ca = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cNames);
        ca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterCinema.setAdapter(ca);
        spinnerFilterCinema.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
                filterCinemaId = pos == 0 ? "" : cinemaList.get(pos - 1).getCinemaId();
                updateHallSpinner(filterCinemaId);
                applyFilter();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        updateHallSpinner("");
    }

    private void updateHallSpinner(String cinemaId) {
        filteredHalls.clear();
        List<String> hNames = new ArrayList<>();
        hNames.add("Tất cả phòng");
        for (Hall h : hallList) {
            if (cinemaId.isEmpty() || cinemaId.equals(h.cinemaId)) {
                filteredHalls.add(h);
                hNames.add(h.name);
            }
        }
        ArrayAdapter<String> ha = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hNames);
        ha.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterHall.setAdapter(ha);
        spinnerFilterHall.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) {
                filterHallId = pos == 0 ? "" : filteredHalls.get(pos - 1).hallId;
                applyFilter();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void loadShowtimes() {
        db.collection("Showtime").get().addOnSuccessListener(snap -> {
            allShowtimes.clear();
            for (QueryDocumentSnapshot doc : snap)
                allShowtimes.add(doc.toObject(Showtime.class));
            applyFilter();
        });
    }

    private void applyFilter() {
        showtimeList.clear();
        for (Showtime st : allShowtimes) {
            if (!filterMovieId.isEmpty()  && !filterMovieId.equals(st.getMovieId()))   continue;
            if (!filterCinemaId.isEmpty() && !filterCinemaId.equals(st.getCinemaId())) continue;
            if (!filterHallId.isEmpty()   && !filterHallId.equals(st.getHallId()))     continue;
            if (!filterDate.isEmpty()     && !filterDate.equals(st.getDate()))          continue;
            showtimeList.add(st);
        }
        adapter.notifyDataSetChanged();
    }
}