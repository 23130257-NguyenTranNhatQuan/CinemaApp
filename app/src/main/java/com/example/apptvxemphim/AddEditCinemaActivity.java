package com.example.apptvxemphim;

import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Address;
import android.location.Geocoder;
import java.util.List;
import java.util.Locale;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddEditCinemaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText etName, etAddress, etBrand, etLogo, etPhone, etPhoto, etGgmap;
    private TextView tvSelectedCoordinate;
    private Button btnSave;
    private TextView tvHeader;
    private String cinemaId = null;
    private FirebaseFirestore db;

    private GoogleMap googleMap;
    private Double selectedLat = null, selectedLng = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_cinema);

        db = FirebaseFirestore.getInstance();

        // Ánh xạ
        tvHeader = findViewById(R.id.tv_add_edit_cinema_header);
        etName = findViewById(R.id.et_cinema_name);
        etAddress = findViewById(R.id.et_cinema_address);
        etBrand = findViewById(R.id.et_cinema_brand);
        etLogo = findViewById(R.id.et_cinema_logo);
        etPhone = findViewById(R.id.et_cinema_phone);
        etPhoto = findViewById(R.id.et_cinema_photo);
        etGgmap = findViewById(R.id.et_cinema_ggmap);
        tvSelectedCoordinate = findViewById(R.id.tv_selected_coordinate);
        btnSave = findViewById(R.id.btn_save_cinema);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapPickerContainer);
        if (mapFragment != null) mapFragment.getMapAsync(this);
        Button btnSearchAddress = findViewById(R.id.btn_search_address);
        btnSearchAddress.setOnClickListener(v -> searchAddressOnMap());

        // Kiểm tra xem có ID truyền sang không (Chế độ Sửa)
        cinemaId = getIntent().getStringExtra("CINEMA_ID");
        if (cinemaId != null) {
            tvHeader.setText("Chỉnh sửa Rạp");
            loadCinemaData(cinemaId);
        } else {
            tvHeader.setText("Thêm Rạp Mới");
        }

        btnSave.setOnClickListener(v -> saveCinemaToFirebase());
    }

    private void loadCinemaData(String id) {
        db.collection("Cinema").document(id).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Cinema cinema = doc.toObject(Cinema.class);
                if (cinema != null) {
                    etName.setText(cinema.getName());
                    etAddress.setText(cinema.getAddress());
                    etBrand.setText(cinema.getBrand());
                    etLogo.setText(cinema.getLogo());
                    etPhone.setText(cinema.getPhone());
                    etPhoto.setText(cinema.getPhoto());
                    etGgmap.setText(cinema.getGgmap());

                    if (cinema.getLatitude() != null && cinema.getLongitude() != null) {
                        selectedLat = cinema.getLatitude();
                        selectedLng = cinema.getLongitude();
                        if (googleMap != null) placeMarker(selectedLat, selectedLng);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveCinemaToFirebase() {
        String name = etName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên rạp và Địa chỉ!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> cinemaData = new HashMap<>();
        cinemaData.put("name", name);
        cinemaData.put("address", address);
        cinemaData.put("brand", etBrand.getText().toString().trim());
        cinemaData.put("logo", etLogo.getText().toString().trim());
        cinemaData.put("phone", etPhone.getText().toString().trim());
        cinemaData.put("photo", etPhoto.getText().toString().trim());
        cinemaData.put("ggmap", etGgmap.getText().toString().trim());
        if (selectedLat != null && selectedLng != null) {
            cinemaData.put("latitude", selectedLat);
            cinemaData.put("longitude", selectedLng);
        } else {
            Toast.makeText(this, "Chưa chọn vị trí trên bản đồ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cinemaId == null) {
            // Chế độ Thêm Mới
            db.collection("Cinema").add(cinemaData)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Thêm rạp thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi thêm rạp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Chế độ Chỉnh Sửa
            db.collection("Cinema").document(cinemaId).set(cinemaData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Cập nhật rạp thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        LatLng defaultPos = new LatLng(10.762622, 106.660172); // Trung tâm TP.HCM
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 12f));

        // Nếu đang sửa rạp đã có tọa độ, load lại sau khi loadCinemaData() chạy xong
        if (selectedLat != null && selectedLng != null) {
            placeMarker(selectedLat, selectedLng);
        }

        map.setOnMapClickListener(latLng -> {
            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;
            placeMarker(selectedLat, selectedLng);
        });
    }

    private void placeMarker(double lat, double lng) {
        if (googleMap == null) return;
        googleMap.clear();
        LatLng pos = new LatLng(lat, lng);
        googleMap.addMarker(new MarkerOptions().position(pos));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f));
        tvSelectedCoordinate.setText(String.format("Đã chọn: %.6f, %.6f", lat, lng));
    }
    private void searchAddressOnMap() {
        String query = etAddress.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Địa chỉ trước khi tìm", Toast.LENGTH_SHORT).show();
            return;
        }
        if (googleMap == null) {
            Toast.makeText(this, "Bản đồ chưa sẵn sàng, thử lại sau", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> results = geocoder.getFromLocationName(query, 1);
            if (results != null && !results.isEmpty()) {
                Address address = results.get(0);
                LatLng pos = new LatLng(address.getLatitude(), address.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f));
                Toast.makeText(this, "Đã tìm thấy khu vực, chạm vào bản đồ để chọn đúng vị trí rạp", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Không tìm thấy địa chỉ này, thử nhập rõ hơn (kèm quận/thành phố)", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tìm kiếm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}