package com.gamex.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private final List<GameItem> items = new ArrayList<>();
    private final OnGameClickListener onGameClickListener;

    GameAdapter(OnGameClickListener onGameClickListener) {
        this.onGameClickListener = onGameClickListener;
    }

    void submitList(List<GameItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_tile, parent, false);
        return new GameViewHolder(view, onGameClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    interface OnGameClickListener {
        void onGameClick(GameItem item);
    }

    final class GameViewHolder extends RecyclerView.ViewHolder {
        private final ImageView gameLogo;
        private final TextView gameName;

        GameViewHolder(@NonNull View itemView, OnGameClickListener clickListener) {
            super(itemView);
            gameLogo = itemView.findViewById(R.id.gameLogo);
            gameName = itemView.findViewById(R.id.gameName);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    clickListener.onGameClick(items.get(position));
                }
            });
        }

        void bind(GameItem item) {
            gameLogo.setImageResource(item.drawableRes);
            gameName.setText(item.name);
        }
    }
}
