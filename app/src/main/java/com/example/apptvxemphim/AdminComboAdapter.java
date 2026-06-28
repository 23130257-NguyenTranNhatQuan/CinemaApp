package com.example.apptvxemphim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AdminComboAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Combo> comboList;
    private OnComboActionListener listener;

    public interface OnComboActionListener {
        void onEditClick(Combo combo);
        void onDeleteClick(Combo combo);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public AdminComboAdapter(List<Combo> comboList, OnComboActionListener listener) {
        this.comboList = comboList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return comboList.get(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_combo, parent, false);
            return new ComboViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Combo item = comboList.get(position);
        
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder h = (HeaderViewHolder) holder;
            h.tvHeader.setText(item.getCategory());
        } else {
            ComboViewHolder h = (ComboViewHolder) holder;
            h.tvName.setText(item.getName());
            h.tvCategory.setText(item.getCategory());
            h.tvDesc.setText(item.getDesc());
            
            // Format price with Vietnamese currency
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            format.setMaximumFractionDigits(0);
            h.tvPrice.setText(format.format(item.getPrice()));

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(h.itemView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(h.imgImage);
            } else {
                h.imgImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Set click listeners for edit and delete buttons
            h.imgEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(item);
                }
            });

            h.imgDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(item);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return comboList.size();
    }

    // ViewHolder cho header
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
    }

    // ViewHolder cho combo
    public static class ComboViewHolder extends RecyclerView.ViewHolder {
        ImageView imgImage, imgEdit, imgDelete;
        TextView tvName, tvCategory, tvDesc, tvPrice;

        public ComboViewHolder(@NonNull View itemView) {
            super(itemView);
            imgImage = itemView.findViewById(R.id.img_admin_combo_image);
            tvName = itemView.findViewById(R.id.tv_admin_combo_name);
            tvCategory = itemView.findViewById(R.id.tv_admin_combo_category);
            tvDesc = itemView.findViewById(R.id.tv_admin_combo_desc);
            tvPrice = itemView.findViewById(R.id.tv_admin_combo_price);
            imgEdit = itemView.findViewById(R.id.img_edit_combo);
            imgDelete = itemView.findViewById(R.id.img_delete_combo);
        }
    }
}
