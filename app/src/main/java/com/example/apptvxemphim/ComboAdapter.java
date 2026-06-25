package com.example.apptvxemphim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ComboAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Combo> comboList;
    private OnQuantityChangeListener listener;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    public ComboAdapter(List<Combo> comboList, OnQuantityChangeListener listener) {
        this.comboList = comboList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return comboList.get(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_combo, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Combo item = comboList.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvHeader.setText(item.name);
        } else {
            ItemViewHolder h = (ItemViewHolder) holder;
            h.tvName.setText(item.name);
            h.tvDesc.setText(item.desc);
            h.tvPrice.setText(String.format("%,d đ", item.price));
            h.tvQuantity.setText(String.valueOf(item.quantity));

            Glide.with(h.itemView.getContext()).load(item.imageUrl).into(h.imgCombo);

            h.btnPlus.setOnClickListener(v -> {
                item.quantity++;
                notifyItemChanged(position);
                listener.onQuantityChanged();
            });

            h.btnMinus.setOnClickListener(v -> {
                if (item.quantity > 0) {
                    item.quantity--;
                    notifyItemChanged(position);
                    listener.onQuantityChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() { return comboList.size(); }

    // ViewHolder cho thanh tiêu đề tím
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
    }

    // ViewHolder cho món ăn
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice, tvQuantity, btnPlus, btnMinus;
        ImageView imgCombo;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvComboName);
            tvDesc = itemView.findViewById(R.id.tvComboDesc);
            tvPrice = itemView.findViewById(R.id.tvComboPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            imgCombo = itemView.findViewById(R.id.imgCombo);
        }
    }
}