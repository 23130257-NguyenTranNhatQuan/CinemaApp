package com.example.apptvxemphim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class AdminCinemaAdapter extends RecyclerView.Adapter<AdminCinemaAdapter.ViewHolder> {

    private List<Cinema> cinemaList;
    private FirebaseFirestore db;
    private OnCinemaActionListener listener;

    public interface OnCinemaActionListener {
        void onEditClick(Cinema cinema);
        void onDeleteClick(Cinema cinema);
    }

    public AdminCinemaAdapter(List<Cinema> cinemaList, OnCinemaActionListener listener) {
        this.cinemaList = cinemaList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_cinema, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cinema cinema = cinemaList.get(position);
        holder.tvName.setText(cinema.getName());
        holder.tvAddress.setText("Địa chỉ: " + cinema.getAddress());
        if (cinema.getBrand() != null && !cinema.getBrand().isEmpty()) {
            holder.tvBrand.setText("Thương hiệu: " + cinema.getBrand());
        } else {
            holder.tvBrand.setVisibility(View.GONE);
        }

        if (cinema.getLogo() != null && !cinema.getLogo().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(cinema.getLogo())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.imgLogo);
        } else {
            holder.imgLogo.setImageResource(android.R.drawable.ic_menu_mapmode);
        }

        // Set click listeners for edit and delete buttons
        holder.imgEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(cinema);
            }
        });

        holder.imgDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(cinema);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cinemaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogo, imgEdit, imgDelete;
        TextView tvName, tvAddress, tvBrand;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogo = itemView.findViewById(R.id.img_admin_cinema_logo);
            tvName = itemView.findViewById(R.id.tv_admin_cinema_name);
            tvAddress = itemView.findViewById(R.id.tv_admin_cinema_address);
            tvBrand = itemView.findViewById(R.id.tv_admin_cinema_brand);
            imgEdit = itemView.findViewById(R.id.img_edit_cinema);
            imgDelete = itemView.findViewById(R.id.img_delete_cinema);
        }
    }
}