package com.example.apptvxemphim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminHallAdapter extends RecyclerView.Adapter<AdminHallAdapter.ViewHolder> {

    public interface OnHallClickListener {
        void onHallClick(Hall hall);
    }

    private final List<Hall> hallList;
    private final OnHallClickListener listener;

    public AdminHallAdapter(List<Hall> hallList, OnHallClickListener listener) {
        this.hallList = hallList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_hall, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hall hall = hallList.get(position);
        holder.tvName.setText(hall.name != null ? hall.name : "(Chưa đặt tên)");
        holder.tvInfo.setText(hall.rows + " hàng x " + hall.cols + " cột"
                + (hall.vipRows > 0 ? " · " + hall.vipRows + " hàng VIP" : "")
                + (hall.coupleRows > 0 ? " · " + hall.coupleRows + " hàng đôi" : ""));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHallClick(hall);
        });
    }

    @Override
    public int getItemCount() { return hallList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvInfo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_admin_hall_name);
            tvInfo = itemView.findViewById(R.id.tv_admin_hall_info);
        }
    }
}