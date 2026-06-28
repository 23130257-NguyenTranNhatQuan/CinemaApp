package com.example.apptvxemphim;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddEditCinemaActivity extends AppCompatActivity {

    private EditText etName, etAddress, etBrand, etLogo, etPhone, etPhoto, etGgmap;
    private WebView webViewMap;
    private Button btnSave;
    private TextView tvHeader;
    private String cinemaId = null;
    private FirebaseFirestore db;

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
        webViewMap = findViewById(R.id.webview_map);
        btnSave = findViewById(R.id.btn_save_cinema);

        // Setup WebView
        WebSettings webSettings = webViewMap.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webViewMap.setWebViewClient(new WebViewClient());

        // Load default map (HCM City center)
        webViewMap.loadUrl("https://www.google.com/maps/@10.762622,106.660172,13z");

        // Listen for URL changes in the EditText
        etGgmap.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String mapUrl = etGgmap.getText().toString().trim();
                if (!mapUrl.isEmpty()) {
                    webViewMap.loadUrl(mapUrl);
                }
            }
        });

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
                    
                    // Load Google Maps if URL exists
                    if (cinema.getGgmap() != null && !cinema.getGgmap().isEmpty()) {
                        webViewMap.loadUrl(cinema.getGgmap());
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
}