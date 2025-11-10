package com.gamex.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamex.app.models.PaymentMethod;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {

    private List<PaymentMethod> paymentMethods = new ArrayList<>();
    private int selectedPosition = -1;
    private OnPaymentMethodSelectedListener listener;

    public interface OnPaymentMethodSelectedListener {
        void onPaymentMethodSelected(PaymentMethod paymentMethod);
    }

    public PaymentMethodAdapter(OnPaymentMethodSelectedListener listener) {
        this.listener = listener;
    }

    public void submitList(List<PaymentMethod> methods) {
        this.paymentMethods = methods;
        notifyDataSetChanged();
    }

    public PaymentMethod getSelectedPaymentMethod() {
        if (selectedPosition >= 0 && selectedPosition < paymentMethods.size()) {
            return paymentMethods.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_payment_method, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PaymentMethod method = paymentMethods.get(position);
        holder.bind(method, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final ImageView icon;
        private final TextView name;
        private final TextView description;
        private final TextView unavailable;
        private final MaterialRadioButton radioButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.paymentMethodCard);
            icon = itemView.findViewById(R.id.paymentIcon);
            name = itemView.findViewById(R.id.paymentName);
            description = itemView.findViewById(R.id.paymentDescription);
            unavailable = itemView.findViewById(R.id.paymentUnavailable);
            radioButton = itemView.findViewById(R.id.paymentRadioButton);
        }

        void bind(PaymentMethod method, boolean isSelected) {
            icon.setImageResource(method.getLogoResource());
            name.setText(method.getName());
            description.setText(method.getDescription());
            radioButton.setChecked(isSelected);

            if (method.isAvailable()) {
                unavailable.setVisibility(View.GONE);
                card.setEnabled(true);
                card.setAlpha(1.0f);

                card.setOnClickListener(v -> {
                    int previousPosition = selectedPosition;
                    selectedPosition = getAdapterPosition();
                    notifyItemChanged(previousPosition);
                    notifyItemChanged(selectedPosition);

                    if (listener != null) {
                        listener.onPaymentMethodSelected(method);
                    }
                });
            } else {
                unavailable.setVisibility(View.VISIBLE);
                card.setEnabled(false);
                card.setAlpha(0.5f);
                card.setOnClickListener(null);
                radioButton.setEnabled(false);
            }
        }
    }
}
