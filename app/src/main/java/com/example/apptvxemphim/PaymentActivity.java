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

public class PaymentActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // 1. Ánh xạ view
        webView = findViewById(R.id.webViewPayment);
        progressBar = findViewById(R.id.progressBar);

        // 2. Cấu hình WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36");

        // 3. Nhận URL thanh toán từ CheckoutActivity
        String url = getIntent().getStringExtra("PAY_URL");
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "URL thanh toán không hợp lệ!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 4. Thiết lập WebViewClient để lắng nghe luồng thanh toán
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

                // Lắng nghe tín hiệu thanh toán thành công (thường là URL chứa từ khóa "success" hoặc "callback")
                if (currentUrl.contains("success")) {
                    Intent intent = new Intent(PaymentActivity.this, PaymentResultActivity.class);
                    intent.putExtra("IS_SUCCESS", true);
                    startActivity(intent);
                    finish(); // Kết thúc PaymentActivity để người dùng không quay lại WebView được nữa
                    return true;
                }

                // Nếu trang là lỗi hoặc hủy, có thể xử lý thêm ở đây
                if (currentUrl.contains("cancel")) {
                    Toast.makeText(PaymentActivity.this, "Giao dịch đã bị hủy", Toast.LENGTH_SHORT).show();
                    finish();
                    return true;
                }

                return false; // Tiếp tục load trang bình thường
            }
        });
        if (url == null || url.startsWith("https://onepay.vn/checkout") == false) {
            android.util.Log.e("DEBUG_URL", "URL bị thiếu hoặc sai đường dẫn: " + url);
        }
        // 5. Tải trang thanh toán
        webView.loadUrl(url);
        android.util.Log.e("DEBUG_URL", "Đang load URL: " + url);
    }

    // Xử lý nút Back của điện thoại để không thoát app đột ngột
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}