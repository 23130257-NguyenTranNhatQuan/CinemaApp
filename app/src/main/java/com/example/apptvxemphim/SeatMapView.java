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

public class SeatMapView extends View {

    // ===== Model ghế (giữ tên Seat để các Activity cũ ít phải đổi) =====
    public static class Seat {
        public String name;
        public int type;       // 0 trống, 1 thường, 2 VIP, 3 đôi
        public boolean isBooked;
        public boolean isSelected = false;
        public int row, col;

        public Seat(String name, int type, boolean isBooked, int row, int col) {
            this.name = name;
            this.type = type;
            this.isBooked = isBooked;
            this.row = row;
            this.col = col;
        }
    }

    public interface OnSeatSelectedListener {
        void onSeatSelectionChanged(List<Seat> selectedSeats);
    }

    // Dùng cho edit mode: admin tap 1 ghế -> callback ra ngoài để hiện dialog đổi loại
    public interface OnSeatEditListener {
        void onSeatTapped(Seat seat);
    }

    // Dùng cho edit mode kéo chọn vùng trung tâm
    public interface OnCenterZoneChangeListener {
        void onCenterZoneChanged(int startRow, int endRow, int startCol, int endCol);
    }

    private List<Seat> seatList = new ArrayList<>();
    private int rows = 0, cols = 0;

    private OnSeatSelectedListener listener;
    private OnSeatEditListener editListener;
    private OnCenterZoneChangeListener zoneListener;

    private boolean editMode = false;
    private boolean zoneSelectMode = false; // true: đang ở chế độ kéo chọn vùng trung tâm

    // Vùng trung tâm hiện tại (để vẽ + cho phép sửa)
    private Integer czStartRow, czEndRow, czStartCol, czEndCol;
    // Toạ độ kéo tạm trong lúc đang vuốt chọn vùng
    private int dragStartRow = -1, dragStartCol = -1, dragEndRow = -1, dragEndCol = -1;
    private boolean isDragging = false;

    private Paint paintSeat = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintDragZone = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float seatSize = 0;
    private final float seatGap = 2;
    private final float rowGap = 2;
    private final float paddingLeftRight = 2;
    private final float paddingTop = 2;
    private float offsetX = paddingLeftRight;

    public SeatMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paintText.setColor(Color.WHITE);
        paintText.setTextAlign(Paint.Align.CENTER);

        paintBorder.setColor(Color.parseColor("#4CAF50"));
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(0.5f);

        paintDragZone.setColor(Color.parseColor("#5500BFFF"));
        paintDragZone.setStyle(Paint.Style.FILL);
    }

    // ===== API thiết lập dữ liệu =====

    /** Gán dữ liệu ghế trực tiếp (dùng cho cả màn mua vé và màn edit) */
    public void setSeats(List<Seat> seats, int rows, int cols) {
        this.seatList = seats;
        this.rows = rows;
        this.cols = cols;
        requestLayout();
        invalidate();
    }

    /**
     * Sinh seatList TRONG RAM từ config Hall (rows/cols/vipRows/coupleRows),
     * sau đó áp các override (ngoại lệ từng ghế, lấy từ collection hallLayouts) lên trên.
     * @param hall        config phòng
     * @param overrides   map tên ghế -> type override, có thể null nếu chưa từng sửa gì
     * @param bookedNames tập tên ghế đã đặt (lấy từ bookedSeats theo showtime), có thể null khi ở màn Admin
     */
    public void generate(Hall hall, java.util.Map<String, Integer> overrides, java.util.Set<String> bookedNames) {
        List<Seat> list = new ArrayList<>();
        String[] labels = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N"};
        for (int r = 0; r < hall.rows; r++) {
            for (int c = 0; c < hall.cols; c++) {
                String name = labels[r] + String.valueOf(c + 1);
                int type = hall.defaultTypeAt(r, c);
                if (overrides != null && overrides.containsKey(name)) {
                    type = overrides.get(name);
                }
                if (type == 0) continue; // EMPTY -> không tạo ghế, để trống/lối đi
                boolean booked = bookedNames != null && bookedNames.contains(name);
                list.add(new Seat(name, type, booked, r, c));
            }
        }
        if (hall.centerStartRow != null) {
            czStartRow = hall.centerStartRow;
            czEndRow = hall.centerEndRow;
            czStartCol = hall.centerStartCol;
            czEndCol = hall.centerEndCol;
        } else {
            czStartRow = czEndRow = czStartCol = czEndCol = null;
        }
        setSeats(list, hall.rows, hall.cols);
    }

    public void setOnSeatSelectedListener(OnSeatSelectedListener l) { this.listener = l; }
    public void setOnSeatEditListener(OnSeatEditListener l) { this.editListener = l; }
    public void setOnCenterZoneChangeListener(OnCenterZoneChangeListener l) { this.zoneListener = l; }

    public void setEditMode(boolean editMode) { this.editMode = editMode; }
    public void setZoneSelectMode(boolean zoneSelectMode) {
        this.zoneSelectMode = zoneSelectMode;
        isDragging = false;
        invalidate();
    }

    /** Đổi loại ghế tại 1 vị trí (dùng khi admin chọn NORMAL/VIP/COUPLE/EMPTY trong dialog) */
    public void updateSeatType(Seat seat, int newType) {
        seat.type = newType;
        invalidate();
    }

    /**
     * Dùng ở màn Admin sau khi sửa ghế: so sánh seatList hiện tại với type mặc định của Hall,
     * trả về map tên ghế -> type chỉ cho NHỮNG GHẾ KHÁC mặc định (để ghi vào hallLayouts).
     * Ghế nào type == default thì không xuất hiện trong map (không cần lưu).
     */
    public java.util.Map<String, Integer> computeOverridesToSave(Hall hall) {
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        for (Seat s : seatList) {
            int def = hall.defaultTypeAt(s.row, s.col);
            if (s.type != def) result.put(s.name, s.type);
        }
        return result;
    }

    public List<Seat> getSeatList() { return seatList; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int[] getCenterZone() {
        if (czStartRow == null) return null;
        return new int[]{czStartRow, czEndRow, czStartCol, czEndCol};
    }

    // ===== Đo kích thước =====

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int effectiveCols = Math.max(cols, 1);
        int effectiveRows = Math.max(rows, 1);

        float seatSizeByWidth = (width - (paddingLeftRight * 2) - (seatGap * (effectiveCols - 1))) / effectiveCols;

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY && height > 0) {
            float seatSizeByHeight = (height - (paddingTop * 2) - (rowGap * (effectiveRows - 1))) / effectiveRows;
            seatSize = Math.min(seatSizeByWidth, seatSizeByHeight);
            setMeasuredDimension(width, height);
        } else {
            seatSize = seatSizeByWidth;
            float totalHeight = (paddingTop * 2) + (effectiveRows * seatSize) + ((effectiveRows - 1) * rowGap);
            setMeasuredDimension(width, (int) totalHeight);
        }
        float totalGridWidth = effectiveCols * seatSize + (effectiveCols - 1) * seatGap;
        offsetX = (MeasureSpec.getSize(widthMeasureSpec) - totalGridWidth) / 2f;
    }

    // ===== Vẽ =====

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (seatList == null || seatList.isEmpty()) return;

        paintText.setTextSize(seatSize * 0.32f);

        for (Seat seat : seatList) {
            if (seat.type == 0 && !editMode) continue; // EMPTY ở màn mua vé -> ẩn hẳn (lối đi)

            // Ghế đôi: chỉ vẽ 1 lần ở ô có col chẵn-nhỏ-hơn trong cặp để khỏi vẽ đè
            if (seat.type == 3 && isSecondOfCouple(seat)) continue;

            float left = offsetX + seat.col * (seatSize + seatGap);
            float top = paddingTop + seat.row * (seatSize + rowGap);
            float right = seat.type == 3
                    ? offsetX + (seat.col + 2) * (seatSize + seatGap) - seatGap
                    : left + seatSize;
            float bottom = top + seatSize;

            RectF rect = new RectF(left, top, right, bottom);

            if (seat.type == 0) {
                paintSeat.setColor(Color.parseColor("#222222")); // EMPTY: ô mờ, chỉ thấy ở edit mode
            } else if (seat.isBooked) {
                paintSeat.setColor(Color.parseColor("#434343"));
            } else if (seat.isSelected) {
                paintSeat.setColor(Color.parseColor("#C9227A"));
            } else {
                if (seat.type == 1) paintSeat.setColor(Color.parseColor("#722ED1"));
                else if (seat.type == 2) paintSeat.setColor(Color.parseColor("#F5222D"));
                else paintSeat.setColor(Color.parseColor("#EB2F96"));
            }

            canvas.drawRoundRect(rect, seatSize * 0.2f, seatSize * 0.2f, paintSeat);

            if (seat.type == 3) {
                String partnerName = "";
                for (Seat s : seatList) {
                    if (s != seat && s.row == seat.row && s.type == 3 && s.col == seat.col + 1) {
                        partnerName = s.name;
                        break;
                    }
                }
                Paint.FontMetrics fm3 = paintText.getFontMetrics();
                float textY = rect.centerY() - (fm3.top + fm3.bottom) / 2;
                paintText.setTextSize(seatSize * 0.28f); // nhỏ hơn để 2 tên vừa 1 dòng
                canvas.drawText(seat.name + "    " + partnerName, rect.centerX(), textY, paintText);
                paintText.setTextSize(seatSize * 0.32f); // reset lại
            } else {
                Paint.FontMetrics fm = paintText.getFontMetrics();
                float textY = rect.centerY() - (fm.top + fm.bottom) / 2;
                canvas.drawText(seat.type == 0 ? "·" : seat.name, rect.centerX(), textY, paintText);
            }
        }
        // Vẽ vùng trung tâm nếu có
        if (czStartRow != null) {
            drawZoneRect(canvas, czStartRow, czEndRow, czStartCol, czEndCol, paintBorder, false);
        }

        // Vẽ vùng đang kéo chọn (preview)
        if (zoneSelectMode && isDragging) {
            int sr = Math.min(dragStartRow, dragEndRow);
            int er = Math.max(dragStartRow, dragEndRow);
            int sc = Math.min(dragStartCol, dragEndCol);
            int ec = Math.max(dragStartCol, dragEndCol);
            drawZoneRect(canvas, sr, er, sc, ec, paintDragZone, true);
        }
    }

    private void drawZoneRect(Canvas canvas, int startRow, int endRow, int startCol, int endCol, Paint paint, boolean fill) {
        float left = offsetX + startCol * (seatSize + seatGap) - seatGap / 2f;
        float top = paddingTop + startRow * (seatSize + rowGap) - rowGap / 2f;
        float right = offsetX + (endCol + 1) * (seatSize + seatGap) - seatGap / 2f;
        float bottom = paddingTop + (endRow + 1) * (seatSize + rowGap) - rowGap / 2f;
        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, 2, 2, paint);
    }

    private boolean isSecondOfCouple(Seat seat) {
        // Ghế đôi ghép cặp theo col: (0,1), (2,3), (4,5)...
        // Col lẻ (index 1,3,5...) là ghế thứ 2, bỏ qua để tránh vẽ đè
        if (seat.col % 2 == 1) {
            // Kiểm tra xem col trước đó có phải ghế đôi không
            for (Seat s : seatList) {
                if (s != seat && s.row == seat.row && s.type == 3 && s.col == seat.col - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    // ===== Chạm =====

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (zoneSelectMode) {
            handleZoneDrag(event);
            return true;
        }
        if (event.getPointerCount() == 1 && event.getAction() == MotionEvent.ACTION_UP) {
            handleSeatTap(event.getX(), event.getY());
        }
        return true;
    }

    private void handleZoneDrag(MotionEvent event) {
        int[] rc = coordToRowCol(event.getX(), event.getY());
        if (rc == null) return;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragStartRow = rc[0]; dragStartCol = rc[1];
                dragEndRow = rc[0]; dragEndCol = rc[1];
                isDragging = true;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                dragEndRow = rc[0]; dragEndCol = rc[1];
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                isDragging = false;
                czStartRow = Math.min(dragStartRow, dragEndRow);
                czEndRow = Math.max(dragStartRow, dragEndRow);
                czStartCol = Math.min(dragStartCol, dragEndCol);
                czEndCol = Math.max(dragStartCol, dragEndCol);
                invalidate();
                if (zoneListener != null) zoneListener.onCenterZoneChanged(czStartRow, czEndRow, czStartCol, czEndCol);
                break;
        }
    }

    /** Trả về [row, col] ước lượng từ toạ độ chạm (kể cả ngoài ô ghế, để kéo vùng mượt) */
    private int[] coordToRowCol(float x, float y) {
        if (seatSize <= 0) return null;
        int col = (int) ((x - offsetX) / (seatSize + seatGap));
        int row = (int) ((y - paddingTop) / (seatSize + rowGap));
        if (row < 0) row = 0;
        if (col < 0) col = 0;
        if (rows > 0 && row >= rows) row = rows - 1;
        if (cols > 0 && col >= cols) col = cols - 1;
        return new int[]{row, col};
    }

    private void handleSeatTap(float x, float y) {
        for (Seat seat : seatList) {
            if (!editMode && seat.isBooked) continue;
            if (seat.type == 3 && isSecondOfCouple(seat)) continue;

            float left = offsetX + seat.col * (seatSize + seatGap);
            float top = paddingTop + seat.row * (seatSize + rowGap);
            // Ghế đôi: right chỉ kéo đến hết col kế tiếp THỰC SỰ
            float right;
            if (seat.type == 3) {
                // Tìm partner để tính right chính xác
                right = offsetX + (seat.col + 2) * (seatSize + seatGap) - seatGap;
            } else {
                right = left + seatSize;
            }
            float bottom = top + seatSize;

            if (x >= left && x <= right && y >= top && y <= bottom) {
                if (editMode) {
                    if (editListener != null) editListener.onSeatTapped(seat);
                    invalidate();
                } else {
                    if (seat.type == 3) {
                        toggleDoubleSeat(seat);
                    } else {
                        seat.isSelected = !seat.isSelected;
                    }
                    invalidate();
                    notifyListener();
                }
                return;
            }
        }
    }

    private void toggleDoubleSeat(Seat tapped) {
        Seat partner = null;
        // Tìm partner đúng cặp: col chẵn đi với col+1, col lẻ đi với col-1
        int partnerCol = (tapped.col % 2 == 0) ? tapped.col + 1 : tapped.col - 1;
        for (Seat s : seatList) {
            if (s != tapped && s.row == tapped.row && s.type == 3 && s.col == partnerCol) {
                partner = s;
                break;
            }
        }
        boolean newState = !tapped.isSelected;
        tapped.isSelected = newState;
        if (partner != null && !partner.isBooked) partner.isSelected = newState;
    }

    private void notifyListener() {
        if (listener != null) {
            List<Seat> selected = new ArrayList<>();
            for (Seat s : seatList) if (s.isSelected) selected.add(s);
            listener.onSeatSelectionChanged(selected);
        }
    }

    public void clearAllSelected() {
        for (Seat s : seatList) s.isSelected = false;
        invalidate();
    }
}