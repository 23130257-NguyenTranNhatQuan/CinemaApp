package com.example.apptvxemphim;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.ArrayList;
import java.util.List;

public class ManageBookingsActivity extends AppCompatActivity {

    private RecyclerView rvBookings;
    private EditText etSearch;
    private Spinner spinnerFilter;
    private Button btnScanQR;
    private AdminBookingAdapter adapter;
    private List<Booking> allBookings = new ArrayList<>();
    private FirebaseFirestore db;

    // Bộ quét QR
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    etSearch.setText(result.getContents());
                    filterData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_bookings);

        db = FirebaseFirestore.getInstance();
        rvBookings = findViewById(R.id.rvBookings);
        etSearch = findViewById(R.id.etSearchBooking);
        spinnerFilter = findViewById(R.id.spinnerFilterStatus);
        btnScanQR = findViewById(R.id.btnScanQR);

        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminBookingAdapter(this, allBookings);
        rvBookings.setAdapter(adapter);

        setupSpinner();
        loadDataFromFirebase();

        // Nút quét QR
        btnScanQR.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Hướng camera vào mã QR trên vé của khách");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            barcodeLauncher.launch(options);

        });

        // Xử lý ô tìm kiếm
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterData(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSpinner() {
        String[] statuses = {"Tất cả", "Đã thanh toán", "Đã sử dụng"};
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerFilter.setAdapter(spinAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { filterData(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadDataFromFirebase() {
        // Sắp xếp đơn hàng mới nhất lên đầu
        db.collection("Booking").orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    allBookings.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        Booking b = doc.toObject(Booking.class);
                        allBookings.add(b);
                    }
                    filterData();
                });
    }

    private void filterData() {
        String searchText = etSearch.getText().toString().toLowerCase().trim();
        String filterStatus = spinnerFilter.getSelectedItem().toString();

        List<Booking> filteredList = new ArrayList<>();
        for (Booking b : allBookings) {
            boolean matchSearch = b.getOrderId() != null && b.getOrderId().toLowerCase().contains(searchText);
            boolean matchStatus = filterStatus.equals("Tất cả") || (b.getStatus() != null && b.getStatus().equals(filterStatus));

            if (matchSearch && matchStatus) {
                filteredList.add(b);
            }
        }
        adapter.updateList(filteredList);
    }
}