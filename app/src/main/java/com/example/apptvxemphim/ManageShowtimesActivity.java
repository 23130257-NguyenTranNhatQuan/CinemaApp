package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ManageShowtimesActivity extends AppCompatActivity {

    private TextView tvSelectedCinema, tvSelectedDate;
    private RecyclerView rcvShowtimes;
    private ShowtimeOverviewAdapter adapter;
    private List<Showtime> showtimeList;
    private FirebaseFirestore db;

    private String selectedCinemaId = "";
    private String selectedCinemaName = "";
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_showtimes);

        db = FirebaseFirestore.getInstance();

        // Back button
        findViewById(R.id.btn_back_showtimes).setOnClickListener(v -> finish());

        // Cinema and date selection
        tvSelectedCinema = findViewById(R.id.tv_selected_cinema);
        tvSelectedDate = findViewById(R.id.tv_selected_date);

        // Set default date to today
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate = dateFormat.format(calendar.getTime());
        tvSelectedDate.setText(selectedDate);

        // Cinema selection
        tvSelectedCinema.setOnClickListener(v -> showCinemaSelectionDialog());

        // Date selection
        tvSelectedDate.setOnClickListener(v -> showDateSelectionDialog());

        // RecyclerView
        rcvShowtimes = findViewById(R.id.rcv_showtimes);
        showtimeList = new ArrayList<>();
        adapter = new ShowtimeOverviewAdapter(showtimeList);
        rcvShowtimes.setLayoutManager(new LinearLayoutManager(this));
        rcvShowtimes.setAdapter(adapter);

        // Add showtime button
        findViewById(R.id.btn_add_showtime).setOnClickListener(v -> showAddShowtimeDialog());

        // Load initial data
        loadCinemas();
    }

    private void loadCinemas() {
        db.collection("Cinema")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Auto-select first cinema
                        QueryDocumentSnapshot firstCinema = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        selectedCinemaId = firstCinema.getId();
                        selectedCinemaName = firstCinema.getString("name");
                        tvSelectedCinema.setText(selectedCinemaName);
                        loadShowtimes();
                    } else {
                        Toast.makeText(this, "Chưa có rạp nào. Vui lòng thêm rạp trước.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCinemaSelectionDialog() {
        db.collection("Cinema")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> cinemaNames = new ArrayList<>();
                        List<String> cinemaIds = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            cinemaNames.add(doc.getString("name"));
                            cinemaIds.add(doc.getId());
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Chọn rạp");
                        builder.setItems(cinemaNames.toArray(new String[0]), (dialog, which) -> {
                            selectedCinemaId = cinemaIds.get(which);
                            selectedCinemaName = cinemaNames.get(which);
                            tvSelectedCinema.setText(selectedCinemaName);
                            loadShowtimes();
                        });
                        builder.show();
                    }
                });
    }

    private void showDateSelectionDialog() {
        // Simple date picker using current date + 7 days
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        String[] dates = new String[7];
        final int[] selectedIndex = {0};

        for (int i = 0; i < 7; i++) {
            dates[i] = dayFormat.format(calendar.getTime()) + ", " + dateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ngày");
        builder.setSingleChoiceItems(dates, 0, (dialog, which) -> {
            selectedIndex[0] = which;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedDate = dates[selectedIndex[0]];
            tvSelectedDate.setText(selectedDate);
            loadShowtimes();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void loadShowtimes() {
        if (selectedCinemaId.isEmpty()) return;

        db.collection("Showtime")
                .whereEqualTo("cinemaId", selectedCinemaId)
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showtimeList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Showtime showtime = doc.toObject(Showtime.class);
                            showtimeList.add(showtime);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void showAddShowtimeDialog() {
        // Load movies and halls for selection
        db.collection("Movie")
                .get()
                .addOnCompleteListener(movieTask -> {
                    if (movieTask.isSuccessful()) {
                        List<String> movieNames = new ArrayList<>();
                        List<String> movieIds = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : movieTask.getResult()) {
                            movieNames.add(doc.getString("title"));
                            movieIds.add(doc.getId());
                        }

                                db.collection("Hall")
                                        .whereEqualTo("cinemaId", selectedCinemaId)
                                        .get()
                                        .addOnCompleteListener(hallTask -> {
                                            if (hallTask.isSuccessful()) {
                                                List<String> hallNames = new ArrayList<>();
                                                List<String> hallIds = new ArrayList<>();

                                                for (QueryDocumentSnapshot doc : hallTask.getResult()) {
                                                    String hallName = doc.getString("name");
                                                    if (hallName != null && !hallName.isEmpty()) {
                                                        hallNames.add(hallName);
                                                        hallIds.add(doc.getId());
                                                    }
                                                }

                                                if (hallNames.isEmpty()) {
                                                    Toast.makeText(this, "Chưa có phòng chiếu nào cho rạp này", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                showAddShowtimeForm(movieNames, movieIds, hallNames, hallIds);
                                            } else {
                                                Toast.makeText(this, "Lỗi tải danh sách phòng: " + hallTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                    }
                });
    }

    private void showAddShowtimeForm(List<String> movieNames, List<String> movieIds,
                                     List<String> hallNames, List<String> hallIds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_showtime, null);
        builder.setView(dialogView);

        Spinner spinnerMovie = dialogView.findViewById(R.id.spinner_movie);
        Spinner spinnerHall = dialogView.findViewById(R.id.spinner_hall);
        Spinner spinnerTime = dialogView.findViewById(R.id.spinner_time);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        // Setup movie spinner
        ArrayAdapter<String> movieAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, movieNames);
        movieAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMovie.setAdapter(movieAdapter);

        // Setup hall spinner
        ArrayAdapter<String> hallAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hallNames);
        hallAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHall.setAdapter(hallAdapter);

        // Setup time spinner (9:00 to 23:00)
        List<String> times = new ArrayList<>();
        for (int hour = 9; hour <= 23; hour++) {
            times.add(String.format(Locale.getDefault(), "%02d:00", hour));
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(timeAdapter);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String selectedMovieId = movieIds.get(spinnerMovie.getSelectedItemPosition());
            String selectedHallId = hallIds.get(spinnerHall.getSelectedItemPosition());
            String selectedTime = times.get(spinnerTime.getSelectedItemPosition());

            saveShowtime(selectedMovieId, selectedHallId, selectedTime);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveShowtime(String movieId, String hallId, String time) {
        // Create showtime object
        Showtime showtime = new Showtime();
        showtime.setMovieId(movieId);
        showtime.setCinemaId(selectedCinemaId);
        showtime.setHallId(hallId);
        showtime.setDate(selectedDate);
        showtime.setTime(time);
        showtime.setLanguage("Phụ đề");

        // Save to Firestore
        db.collection("Showtime")
                .add(showtime)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã thêm suất chiếu thành công", Toast.LENGTH_SHORT).show();
                    loadShowtimes();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}