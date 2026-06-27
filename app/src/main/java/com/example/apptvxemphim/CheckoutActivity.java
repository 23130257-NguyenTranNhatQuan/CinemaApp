package com.example.apptvxemphim;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {
    private LinearLayout layoutOrderDetails, paymentContainer;
    private ScrollView mainScrollView;
    private TextView tvFinalTotal, tvMovieName, tvSeats, tvTimer;
    private EditText etPromoCode;
    private RadioGroup rgPaymentMethod;
    private WebView webView;
    private long totalAmount = 0;
    private CountDownTimer countDownTimer;
    private String currentOrderId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        setupData();
        setupListeners();
        startPaymentTimer();
    }

    private void initViews() {
        layoutOrderDetails = findViewById(R.id.layoutOrderDetails);
        tvFinalTotal = findViewById(R.id.tvFinalTotal);
        tvMovieName = findViewById(R.id.tvCheckoutMovieName);
        tvSeats = findViewById(R.id.tvCheckoutSeats);
        etPromoCode = findViewById(R.id.etPromoCode);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        mainScrollView = findViewById(R.id.mainScrollView);
        paymentContainer = findViewById(R.id.paymentContainer);
        webView = findViewById(R.id.webViewPayment);
        tvTimer = findViewById(R.id.tvTimer);
    }

    private void startPaymentTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(300000, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                if (tvTimer != null) tvTimer.setText(String.format("Thời gian giữ vé: %02d:%02d", minutes, seconds));
            }
            public void onFinish() {
                if (tvTimer != null) tvTimer.setText("Đã hết thời gian!");
                Toast.makeText(CheckoutActivity.this, "Hết thời gian giữ vé!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }.start();
    }

    private void setupData() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("MOVIE_TITLE");
        ArrayList<String> seats = intent.getStringArrayListExtra("SELECTED_SEATS");
        long seatPrice = intent.getLongExtra("SEAT_PRICE", 0);
        ArrayList<Combo> selectedCombos = intent.getParcelableArrayListExtra("selected_combos");

        if (tvMovieName != null) tvMovieName.setText(title != null ? title : "Chưa xác định");
        if (tvSeats != null) tvSeats.setText("Ghế: " + (seats != null ? TextUtils.join(", ", seats) : "Chưa chọn"));

        if (layoutOrderDetails != null) layoutOrderDetails.removeAllViews();
        addOrderItem("Vé xem phim", seatPrice);

        long comboTotal = 0;
        if (selectedCombos != null) {
            for (Combo c : selectedCombos) {
                long itemTotal = (long) c.price * c.quantity;
                comboTotal += itemTotal;
                addOrderItem(c.name + " x" + c.quantity, itemTotal);
            }
        }
        totalAmount = seatPrice + comboTotal;
        updateTotalDisplay();

        // Khởi tạo sẵn mã đơn hàng duy nhất cho lượt thanh toán này
        currentOrderId = "CS" + System.currentTimeMillis();
    }

    private void setupListeners() {
        findViewById(R.id.btnPayNow).setOnClickListener(v -> {
            if (rgPaymentMethod != null && rgPaymentMethod.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            mainScrollView.setVisibility(View.GONE);
            paymentContainer.setVisibility(View.GONE);
            tvTimer.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);

            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setAllowFileAccess(true);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    // Đổ mã đơn hàng vào HTML
                    webView.evaluateJavascript("if(document.getElementById('orderId')) { document.getElementById('orderId').innerText = '" + currentOrderId + "'; }", null);

                    // Đổ tổng tiền đã định dạng vào HTML
                    String formattedPrice = String.format("%,d đ", totalAmount);
                    webView.evaluateJavascript("if(document.getElementById('amount')) { document.getElementById('amount').innerText = '" + formattedPrice + "'; }", null);
                }

                // Hỗ trợ bắt URL cho các thiết bị Android mới (API 24+)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    if (handlePaymentRedirection(url)) {
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, request);
                }

                // Hỗ trợ bắt URL cho các thiết bị Android cũ (API < 24)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (handlePaymentRedirection(url)) {
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, url);
                }
            });

            webView.loadUrl("file:///android_asset/payment.html");
        });
    }

    private boolean handlePaymentRedirection(String url) {
        if (url.contains("payment_success")) {
            if (countDownTimer != null) countDownTimer.cancel();

            // Mở ứng dụng Email để người dùng gửi xác nhận (Mục đích demo học tập)
            sendEmailConfirmation("Mã đơn: " + currentOrderId + " - Tổng tiền: " + String.format("%,d đ", totalAmount));

            // Truyền trạng thái thành công sang màn hình kết quả
            Intent intent = new Intent(CheckoutActivity.this, PaymentResultActivity.class);
            intent.putExtra("IS_SUCCESS", true);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    private void sendEmailConfirmation(String orderInfo) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Xác nhận đặt vé thành công - CINESTAR");
        intent.putExtra(Intent.EXTRA_TEXT, "Chúc mừng bạn đã đặt vé thành công!\nThông tin đơn hàng: " + orderInfo);
        try {
            startActivity(Intent.createChooser(intent, "Gửi xác nhận qua..."));
        } catch (Exception e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
        }
    }

    private void addOrderItem(String name, long price) {
        TextView tv = new TextView(this);
        tv.setText(name + ": " + String.format("%,d đ", price));
        tv.setTextSize(16);
        layoutOrderDetails.addView(tv);
    }

    private void updateTotalDisplay() {
        if (tvFinalTotal != null) {
            tvFinalTotal.setText(String.format("%,d đ", totalAmount));
        }
    }

    @Override
    protected void onDestroy() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (webView != null) {
            webView.destroy();
        }

        super.onDestroy();
    }
}