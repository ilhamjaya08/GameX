package com.gamex.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamex.app.models.User;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<User> users = new ArrayList<>();
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView userName;
        private final TextView userEmail;
        private final TextView userBalance;
        private final Chip userRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userBalance = itemView.findViewById(R.id.userBalance);
            userRole = itemView.findViewById(R.id.userRole);
        }

        public void bind(User user, OnUserClickListener listener) {
            userName.setText(user.getName());
            userEmail.setText(user.getEmail());
            userBalance.setText(CurrencyUtils.formatToRupiah(user.getBalanceAsInt()));

            if ("admin".equals(user.getRole())) {
                userRole.setText(itemView.getContext().getString(R.string.manage_users_role_admin));
                userRole.setChipBackgroundColorResource(R.color.gamex_green);
            } else {
                userRole.setText(itemView.getContext().getString(R.string.manage_users_role_user));
                userRole.setChipBackgroundColorResource(android.R.color.darker_gray);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
        }
    }
}
