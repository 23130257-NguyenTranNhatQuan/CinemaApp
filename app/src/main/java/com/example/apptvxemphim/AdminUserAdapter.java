package com.example.apptvxemphim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private List<UserAccount> userList;

    public AdminUserAdapter(List<UserAccount> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserAccount user = userList.get(position);
        // Hiển thị tên: ưu tiên full_name, fallback user (username), fallback email
        String displayName = user.getFullName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getUser();
        }
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getEmail();
        }
        holder.tvName.setText(displayName != null ? displayName : "Chưa có tên");
        holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");

        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            holder.tvPhone.setText("SĐT: " + user.getPhone());
            holder.tvPhone.setVisibility(View.VISIBLE);
        } else {
            holder.tvPhone.setVisibility(View.GONE);
        }

        String role = user.getRole() != null ? user.getRole() : "user";
        holder.tvRole.setText(role.equals("admin") ? "Admin" : "Người dùng");
        holder.tvRole.setTextColor(holder.itemView.getContext().getResources().getColor(
                role.equals("admin") ? android.R.color.holo_orange_dark : android.R.color.holo_blue_dark
        ));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_admin_user_name);
            tvEmail = itemView.findViewById(R.id.tv_admin_user_email);
            tvPhone = itemView.findViewById(R.id.tv_admin_user_phone);
            tvRole = itemView.findViewById(R.id.tv_admin_user_role);
        }
    }
}