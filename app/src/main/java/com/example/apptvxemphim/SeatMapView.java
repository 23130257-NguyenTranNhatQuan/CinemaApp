package com.example.apptvxemphim;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import android.view.ScaleGestureDetector;

public class SeatMapView extends View {

    public interface OnSeatSelectedListener {
        void onSeatSelectionChanged(List<Seat> selectedSeats);
    }

    public static class Seat {
        public String name;    // VD: "A12", "D9"
        public int type;       // 1: Thường, 2: VIP, 3: Đôi
        public boolean isBooked;
        public boolean isSelected = false;

        // Tọa độ ảo để tính toán vị trí trên grid (0-indexed)
        public int row;
        public int col;

        public Seat(String name, int type, boolean isBooked, int row, int col) {
            this.name = name;
            this.type = type;
            this.isBooked = isBooked;
            this.row = row;
            this.col = col;
        }
    }

    private List<Seat> seatList = new ArrayList<>();
    private OnSeatSelectedListener listener;

    private Paint paintSeat = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Cấu hình kích thước sơ đồ (Tổng số hàng tối đa là 9: A,B,C,D,E,F,G,H,I và tối đa 13 cột)
    private final int MAX_ROWS = 9;
    private final int MAX_COLS = 13;

    private float seatSize = 0;
    private float seatGap = 3;
    private float rowGap = 3;
    private float paddingLeftRight = 4;
    private float paddingTop = 4;



    public SeatMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintText.setColor(Color.WHITE);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintBorder.setColor(Color.parseColor("#2F6946")); // Viền vùng trung tâm
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(1f);


    }

    public void setSeats(List<Seat> seats) {
        this.seatList = seats;
        invalidate();
    }

    public void setOnSeatSelectedListener(OnSeatSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Tính seatSize theo chiều rộng
        float seatSizeByWidth = (width - (paddingLeftRight * 2) - (seatGap * (MAX_COLS - 1))) / MAX_COLS;

        // Nếu có chiều cao cố định từ XML (height > 0 và mode EXACTLY)
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY && height > 0) {
            float seatSizeByHeight = (height - (paddingTop * 2) - (rowGap * (MAX_ROWS - 1))) / MAX_ROWS;
            seatSize = Math.min(seatSizeByWidth, seatSizeByHeight);
            setMeasuredDimension(width, height);
        } else {
            seatSize = seatSizeByWidth;
            float totalHeight = (paddingTop * 2) + (MAX_ROWS * seatSize) + ((MAX_ROWS - 1) * rowGap);
            setMeasuredDimension(width, (int) totalHeight);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (seatList == null || seatList.isEmpty()) return;



        paintText.setTextSize(seatSize * 0.4f);

        // 1. Vẽ các ô ghế
        for (Seat seat : seatList) {
            float[] coords = getSeatCoordinates(seat);
            float left = coords[0];
            float top = coords[1];
            float right = left + (seat.type == 3 ? (seatSize * 2 + seatGap) : seatSize);
            float bottom = top + seatSize;

            RectF rect = new RectF(left, top, right, bottom);

            if (seat.isBooked) {
                paintSeat.setColor(Color.parseColor("#434343"));
            } else if (seat.isSelected) {
                paintSeat.setColor(Color.parseColor("#C9227A"));
            } else {
                if (seat.type == 1) paintSeat.setColor(Color.parseColor("#722ED1"));
                else if (seat.type == 2) paintSeat.setColor(Color.parseColor("#F5222D"));
                else paintSeat.setColor(Color.parseColor("#EB2F96"));
            }

            canvas.drawRoundRect(rect, seatSize * 0.2f, seatSize * 0.2f, paintSeat);

            Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
            float textY = rect.centerY() - (fontMetrics.top + fontMetrics.bottom) / 2;
            canvas.drawText(seat.name, rect.centerX(), textY, paintText);
        }

        // 2. Vẽ viền Vùng Trung Tâm
        float xD5 = paddingLeftRight + (4 * (seatSize + seatGap)) - (seatGap / 2);
        float yD5 = paddingTop + (3 * (seatSize + rowGap)) - (rowGap / 2);
        float xF8 = paddingLeftRight + (9 * (seatSize + seatGap)) - (seatGap / 2);
        float yF8 = paddingTop + (5 * (seatSize + rowGap)) + seatSize + (rowGap / 2);

        RectF centerZone = new RectF(xD5, yD5, xF8, yF8);
        canvas.drawRoundRect(centerZone, 2, 2, paintBorder);


    }


    // Hàm căn chỉnh vị trí dịch chuyển của các hàng đặc biệt (A, B, I) để giống ảnh gốc
    private float[] getSeatCoordinates(Seat seat) {
        float x = paddingLeftRight + (seat.col * (seatSize + seatGap));
        float y = paddingTop + (seat.row * (seatSize + rowGap));

        // Xử lý lệch cột cho các hàng không đủ 13 ghế để căn đều vào giữa
        if (seat.row == 0) { // Hàng A có 12 ghế, lùi vào nửa ô
            x += (seatSize + seatGap) / 2;
        } else if (seat.row == 1) { // Hàng B có 10 ghế, lùi vào 2 ô rưỡi
            x += (seatSize + seatGap) * 2.5f;
        } else if (seat.row == 8) { // Hàng I (Ghế đôi) gồm 4 block đôi lùi vào giữa
            x += (seatSize + seatGap) * 2.5f;
        }
        return new float[]{x, y};
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1 && event.getAction() == MotionEvent.ACTION_UP) {
            handleSeatTap(event.getX(), event.getY());
        }
        // Luôn trả true để nhận đủ chuỗi DOWN→UP
        // ZoomLayout vẫn zoom được vì hasClickableChildren=true
        return true;
    }

    private void handleSeatTap(float x, float y) {
        for (Seat seat : seatList) {
            if (seat.isBooked) continue;
            float[] coords = getSeatCoordinates(seat);
            float left = coords[0];
            float top = coords[1];
            float right = left + (seat.type == 3 ? (seatSize * 2 + seatGap) : seatSize);
            float bottom = top + seatSize;

            if (x >= left && x <= right && y >= top && y <= bottom) {
                if (seat.type == 3) {
                    // Ghế đôi: tìm ghế partner cùng block (col liền kề trong hàng I)
                    toggleDoubleSeat(seat);
                } else {
                    seat.isSelected = !seat.isSelected;
                }
                invalidate();
                notifyListener();
                return;
            }
        }
    }

    private void toggleDoubleSeat(Seat tapped) {
        // Tìm partner: ghế đôi khai báo theo cặp col liền nhau (0-1, 3-4, 6-7, 9-10)
        Seat partner = null;
        for (Seat s : seatList) {
            if (s == tapped || s.row != tapped.row || s.type != 3) continue;
            if (Math.abs(s.col - tapped.col) == 1) {
                // Kiểm tra cùng block (không qua lối đi - col cách nhau đúng 1)
                int minCol = Math.min(s.col, tapped.col);
                if (minCol == 0 || minCol == 3 || minCol == 6 || minCol == 9) {
                    partner = s;
                    break;
                }
            }
        }
        boolean newState = !tapped.isSelected;
        tapped.isSelected = newState;
        if (partner != null && !partner.isBooked) partner.isSelected = newState;
    }

    private void notifyListener() {
        if (listener != null) {
            List<Seat> selected = new ArrayList<>();
            for (Seat s : seatList) { if (s.isSelected) selected.add(s); }
            listener.onSeatSelectionChanged(selected);
        }
    }

    public void clearAllSelected() {
        for (Seat s : seatList) s.isSelected = false;
        invalidate();
    }
        public List<Seat> getSelectedSeats() {
            List<Seat> selected = new ArrayList<>();
            for (Seat s : seatList) {
                if (s.isSelected) {
                    selected.add(s);
                }
            }
            return selected;
        }

}