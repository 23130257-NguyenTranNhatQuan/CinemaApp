package com.example.apptvxemphim;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

public class PaymentActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        webView = findViewById(R.id.webViewPayment);
        progressBar = findViewById(R.id.progressBar);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36");

        String url = getIntent().getStringExtra("PAY_URL");
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "URL không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String currentUrl = request.getUrl().toString();

                if (currentUrl.contains("success")) {
                    ArrayList<String> selectedSeats = getIntent().getStringArrayListExtra("SELECTED_SEATS");
                    String showtimeId = getIntent().getStringExtra("SHOWTIME_ID");

                    updateSeatsToBooked(showtimeId, selectedSeats);

                    Intent intent = new Intent(PaymentActivity.this, PaymentResultActivity.class);
                    intent.putExtra("IS_SUCCESS", true);
                    startActivity(intent);
                    finish();
                    return true;
                }

                if (currentUrl.contains("cancel")) {
                    Toast.makeText(PaymentActivity.this, "Giao dịch đã hủy", Toast.LENGTH_SHORT).show();
                    finish();
                    return true;
                }

                return false;
            }
        });

        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void updateSeatsToBooked(String showtimeId, ArrayList<String> selectedSeats) {
        if (showtimeId == null || selectedSeats == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (String seatId : selectedSeats) {
            db.collection("Showtime").document(showtimeId)
                    .collection("Seats").document(seatId)
                    .update("status", "booked")
                    .addOnSuccessListener(aVoid -> android.util.Log.d("Firebase", "Update success: " + seatId))
                    .addOnFailureListener(e -> android.util.Log.e("Firebase", "Update error: " + e.getMessage()));
        }
    }
}
