package com.example.apptvxemphim;

import java.util.HashMap;
import java.util.Map;

public class Hall {
    public String hallId;
    public String cinemaId;
    public String name;

    public int rows;
    public int cols;

    public int vipRows;     // số hàng VIP, tính từ giữa
    public int coupleRows;  // số hàng ghế đôi, tính từ cuối

    // Vùng trung tâm, null nếu không có
    public Integer centerStartRow, centerEndRow, centerStartCol, centerEndCol;

    public Hall() {}

    public Hall(String name, int rows, int cols, int vipRows, int coupleRows) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;
        this.vipRows = vipRows;
        this.coupleRows = coupleRows;
    }

    /** Loại ghế mặc định tại (row, col) khi CHƯA áp override - dùng cả lúc generate và lúc diff để lưu override */
    public int defaultTypeAt(int row, int col) {
        int coupleStartRow = rows - coupleRows;
        int vipStartRow = (rows / 2) - (vipRows / 2);
        int vipEndRow = vipStartRow + vipRows;

        if (coupleRows > 0 && row >= coupleStartRow) return 3;
        if (vipRows > 0 && row >= vipStartRow && row < vipEndRow) return 2;
        return 1;
    }

    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("cinemaId", cinemaId);
        map.put("name", name);
        map.put("rows", rows);
        map.put("cols", cols);
        map.put("vipRows", vipRows);
        map.put("coupleRows", coupleRows);
        if (centerStartRow != null) {
            Map<String, Object> cz = new HashMap<>();
            cz.put("startRow", centerStartRow);
            cz.put("endRow", centerEndRow);
            cz.put("startCol", centerStartCol);
            cz.put("endCol", centerEndCol);
            map.put("centerZone", cz);
        }
        return map;
    }
}