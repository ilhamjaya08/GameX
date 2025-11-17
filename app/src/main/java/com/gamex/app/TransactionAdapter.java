package com.gamex.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamex.app.models.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.bind(transaction);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView productName;
        private final TextView targetId;
        private final TextView amount;
        private final TextView status;
        private final TextView createdAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.transactionProductName);
            targetId = itemView.findViewById(R.id.transactionTarget);
            amount = itemView.findViewById(R.id.transactionAmount);
            status = itemView.findViewById(R.id.transactionStatusBadge);
            createdAt = itemView.findViewById(R.id.transactionDate);
        }

        public void bind(Transaction transaction) {
            if (transaction.getProduct() != null) {
                productName.setText(transaction.getProduct().getNama());
            } else {
                productName.setText("Unknown Product");
            }

            targetId.setText(transaction.getTargetId());
            amount.setText(CurrencyUtils.formatToRupiah(transaction.getAmountAsInt()));
            createdAt.setText(DateUtils.formatDate(transaction.getCreatedAt()));

            // Status text and color
            String statusText;
            int statusColor;
            if (transaction.isPending()) {
                statusText = itemView.getContext().getString(R.string.transaction_item_status_pending);
                statusColor = itemView.getContext().getColor(R.color.gamex_text_secondary);
            } else if (transaction.isPaid()) {
                statusText = itemView.getContext().getString(R.string.transaction_item_status_paid);
                statusColor = itemView.getContext().getColor(R.color.gamex_green);
            } else if (transaction.isProcess()) {
                statusText = itemView.getContext().getString(R.string.transaction_item_status_process);
                statusColor = itemView.getContext().getColor(R.color.gamex_green);
            } else if (transaction.isSuccess()) {
                statusText = itemView.getContext().getString(R.string.transaction_item_status_success);
                statusColor = itemView.getContext().getColor(R.color.gamex_green);
            } else if (transaction.isFailed()) {
                statusText = itemView.getContext().getString(R.string.transaction_item_status_failed);
                statusColor = itemView.getContext().getColor(android.R.color.holo_red_dark);
            } else if (transaction.isRefund()) {
                statusText = itemView.getContext().getString(R.string.transaction_item_status_refund);
                statusColor = itemView.getContext().getColor(android.R.color.holo_orange_dark);
            } else {
                statusText = transaction.getStatus();
                statusColor = itemView.getContext().getColor(R.color.gamex_text_secondary);
            }

            status.setText(statusText);
            status.setTextColor(statusColor);
        }
    }
}
