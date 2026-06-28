package com.example.apptvxemphim;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

import java.util.Properties;

public class CheckoutActivity extends AppCompatActivity {
    private LinearLayout layoutOrderDetails, paymentContainer;
    private ScrollView mainScrollView;
    private TextView tvFinalTotal, tvMovieName, tvSeats, tvTimer;
    private EditText etPromoCode;
    // Đã thay thế RadioGroup bằng 3 RadioButton độc lập
    private RadioButton rbMomo, rbATM, rbVisa;
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
        mainScrollView = findViewById(R.id.mainScrollView);
        paymentContainer = findViewById(R.id.paymentContainer);
        webView = findViewById(R.id.webViewPayment);
        tvTimer = findViewById(R.id.tvTimer);

        // Khởi tạo các RadioButton từ XML
        rbMomo = findViewById(R.id.rbMomo);
        rbATM = findViewById(R.id.rbATM);
        rbVisa = findViewById(R.id.rbVisa);

        // Logic chọn 1 bỏ các cái còn lại
        View.OnClickListener radioListener = v -> {
            rbMomo.setChecked(v.getId() == R.id.rbMomo);
            rbATM.setChecked(v.getId() == R.id.rbATM);
            rbVisa.setChecked(v.getId() == R.id.rbVisa);
        };
        rbMomo.setOnClickListener(radioListener);
        rbATM.setOnClickListener(radioListener);
        rbVisa.setOnClickListener(radioListener);
    }

    private void setupListeners() {
        findViewById(R.id.btnPayNow).setOnClickListener(v -> {
            if (!rbMomo.isChecked() && !rbATM.isChecked() && !rbVisa.isChecked()) {
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
                    webView.evaluateJavascript("if(document.getElementById('orderId')) { document.getElementById('orderId').innerText = '" + currentOrderId + "'; }", null);
                    String formattedPrice = String.format("%,d đ", totalAmount);
                    webView.evaluateJavascript("if(document.getElementById('amount')) { document.getElementById('amount').innerText = '" + formattedPrice + "'; }", null);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return handlePaymentRedirection(request.getUrl().toString());
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return handlePaymentRedirection(url);
                }
            });
            webView.loadUrl("file:///android_asset/payment.html");
        });
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
                long itemTotal = c.getPrice() * c.getQuantity();
                comboTotal += itemTotal;
                addOrderItem(c.getName() + " x" + c.getQuantity(), itemTotal);
            }
        }
        totalAmount = seatPrice + comboTotal;
        updateTotalDisplay();
        currentOrderId = "CS" + System.currentTimeMillis();
    }

    private boolean handlePaymentRedirection(String url) {
        if (url.contains("payment_success")) {
            if (countDownTimer != null) countDownTimer.cancel();

            String showtimeId = getIntent().getStringExtra("SHOWTIME_ID");
            ArrayList<String> selectedSeats = getIntent().getStringArrayListExtra("SELECTED_SEATS");

            updateSeatsToBooked(showtimeId, selectedSeats);
            saveBookingToFirebase();
            sendEmailConfirmation("Mã đơn: " + currentOrderId);
            Intent intent = new Intent(CheckoutActivity.this, PaymentResultActivity.class);
            intent.putExtra("IS_SUCCESS", true);
            intent.putExtra("ORDER_ID", currentOrderId);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    private void saveBookingToFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "Khách_Vãng_Lai";

        Intent intent = getIntent();
        String title = intent.getStringExtra("MOVIE_TITLE");
        String time = intent.getStringExtra("SHOWTIME_TIME");
        String date = intent.getStringExtra("SHOWTIME_DATE");
        String hallId = intent.getStringExtra("HALL_ID");
        ArrayList<String> seats = intent.getStringArrayListExtra("SELECTED_SEATS");
        ArrayList<Combo> selectedCombos = intent.getParcelableArrayListExtra("selected_combos");

        StringBuilder comboString = new StringBuilder();
        if (selectedCombos != null && !selectedCombos.isEmpty()) {
            for (int i = 0; i < selectedCombos.size(); i++) {
                Combo c = selectedCombos.get(i);
                comboString.append(c.getName()).append(" x").append(c.getQuantity());
                if (i < selectedCombos.size() - 1) comboString.append(", ");
            }
        } else {
            comboString.append("Không có");
        }

        String paymentMethod = "Trực tuyến";
        if (rbMomo.isChecked()) paymentMethod = "MoMo";
        else if (rbATM.isChecked()) paymentMethod = "Thẻ ATM";
        else if (rbVisa.isChecked()) paymentMethod = "Thẻ Visa";

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("orderId", currentOrderId);
        bookingData.put("userId", userId);
        bookingData.put("movieTitle", title != null ? title : "");
        bookingData.put("showTime", (time != null ? time : "") + " - " + (date != null ? date : ""));
        bookingData.put("hallId", hallId != null ? hallId : "");
        bookingData.put("seats", seats != null ? TextUtils.join(", ", seats) : "");
        bookingData.put("combos", comboString.toString()); // Lưu thẳng danh sách Combo đã gộp
        bookingData.put("totalPrice", totalAmount);
        bookingData.put("paymentMethod", paymentMethod);
        bookingData.put("status", "Đã thanh toán");
        bookingData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("Booking").document(currentOrderId).set(bookingData)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu đơn hàng lên hệ thống", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendEmailConfirmation(String orderInfo) {
        final String senderEmail = "nhatquanqn2005@gmail.com";
        final String senderPassword = "kuar cvkz zdmf ddqq";
        String recipientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        new Thread(() -> {
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(currentOrderId, BarcodeFormat.QR_CODE, 400, 400);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] imageBytes = baos.toByteArray();

                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("Xác nhận đặt vé Cinestar thành công");

                MimeMultipart multipart = new MimeMultipart("related"); // "related" báo cho email biết các thành phần có liên kết với nhau

                MimeBodyPart textPart = new MimeBodyPart();
                String htmlContent = "<h2>Chào bạn,</h2>" +
                        "<p>Đơn hàng của bạn đã thanh toán thành công!</p>" +
                        "<p>Vui lòng xuất trình mã QR dưới đây cho nhân viên tại rạp:</p>" +
                        "<img src=\"cid:qrcode_image\" style=\"width:250px; height:250px;\" />" +
                        "<p>Mã đơn hàng: <b>" + currentOrderId + "</b></p>" +
                        "<p>Cảm ơn bạn đã sử dụng dịch vụ!</p>";
                textPart.setContent(htmlContent, "text/html; charset=utf-8");
                multipart.addBodyPart(textPart);

                MimeBodyPart imagePart = new MimeBodyPart();
                ByteArrayDataSource bds = new ByteArrayDataSource(imageBytes, "image/png");
                imagePart.setDataHandler(new DataHandler(bds));
                imagePart.setContentID("<qrcode_image>");
                imagePart.setDisposition(MimeBodyPart.INLINE);
                multipart.addBodyPart(imagePart);

                message.setContent(multipart);
                Transport.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void addOrderItem(String name, long price) {
        TextView tv = new TextView(this);
        tv.setText(name + ": " + String.format("%,d đ", price));
        tv.setTextSize(16);
        layoutOrderDetails.addView(tv);
    }

    private void updateTotalDisplay() {
        if (tvFinalTotal != null) tvFinalTotal.setText(String.format("%,d đ", totalAmount));
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        if (webView != null) webView.destroy();
        super.onDestroy();
    }

    private void updateSeatsToBooked(String showtimeId, ArrayList<String> selectedSeats) {
        if (showtimeId == null || selectedSeats == null) return;
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        for (String seatId : selectedSeats) updates.put(seatId, true);
        db.collection("BookedSeats").document(showtimeId).set(updates, com.google.firebase.firestore.SetOptions.merge());
    }
}