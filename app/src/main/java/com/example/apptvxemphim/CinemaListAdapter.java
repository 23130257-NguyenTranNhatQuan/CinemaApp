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
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.app.Dialog;
import android.view.Window;
import android.view.ViewGroup;
import android.widget.Toast;
public class CinemaListAdapter extends RecyclerView.Adapter<CinemaListAdapter.VH> {

    public interface OnCinemaClickListener {
        void onClick(Cinema cinema);
    }

    private final List<Cinema> list;
    private final OnCinemaClickListener listener;

    public CinemaListAdapter(List<Cinema> list, OnCinemaClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cinema, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Cinema c = list.get(pos);
        h.tvName.setText(c.getName());
        h.tvAddress.setText(c.getAddress());
        h.tvPhone.setText(c.getPhone() != null ?  c.getPhone() : "");
        String imageUrl = c.getPhoto() != null ? c.getPhoto() : c.getLogo();
        Glide.with(h.itemView.getContext())
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(h.ivPhoto);
        h.itemView.setOnClickListener(v -> listener.onClick(c));
        h.tvMap.setOnClickListener(v -> showMapDialog(h.itemView.getContext(), c.getGgmap()));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        TextView tvName, tvAddress, tvPhone, tvMap;
        VH(View v) {
            super(v);
            ivPhoto   = v.findViewById(R.id.iv_cinema_photo);
            tvName    = v.findViewById(R.id.tv_cinema_name);
            tvAddress = v.findViewById(R.id.tv_cinema_address);
            tvPhone   = v.findViewById(R.id.tv_cinema_phone);
            tvMap     = v.findViewById(R.id.tv_cinema_map);
        }
    }

    private void showMapDialog(android.content.Context context, String mapUrl) {
        if (mapUrl == null || mapUrl.trim().isEmpty()) {
            Toast.makeText(context, "Rạp này chưa có link bản đồ", Toast.LENGTH_SHORT).show();
            return;
        }
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_map);
        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (context.getResources().getDisplayMetrics().heightPixels * 0.85));

        WebView webView = dialog.findViewById(R.id.webview_map_dialog);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new android.webkit.WebViewClient());
        webView.loadUrl(mapUrl);

        dialog.findViewById(R.id.btn_close_map).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_close_map_bottom).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

}