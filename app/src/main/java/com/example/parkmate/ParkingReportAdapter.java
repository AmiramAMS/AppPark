package com.example.parkmate;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkmate.databinding.ItemParkingReportBinding;

import java.util.List;

public class ParkingReportAdapter extends RecyclerView.Adapter<ParkingReportAdapter.ReportVH> {

    public interface OnItemClickListener {
        void onItemClick(ParkingReport report);
    }

    public interface OnMarkAsTakenListener {
        void onMarkAsTaken(ParkingReport report);
    }

    private final List<ParkingReport> items;
    private final OnItemClickListener listener;
    private final OnMarkAsTakenListener markAsTakenListener;

    public ParkingReportAdapter(List<ParkingReport> items, OnItemClickListener listener) {
        this(items, listener, null);
    }

    public ParkingReportAdapter(List<ParkingReport> items, OnItemClickListener listener, OnMarkAsTakenListener markAsTakenListener) {
        this.items = items;
        this.listener = listener;
        this.markAsTakenListener = markAsTakenListener;
    }

    @NonNull
    @Override
    public ReportVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemParkingReportBinding binding = ItemParkingReportBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ReportVH(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportVH holder, int position) {
        ParkingReport report = items.get(position);

        holder.binding.tvAddress.setText(report.address);
        holder.binding.tvStatus.setText(statusToDisplay(report.status));

        holder.binding.cbMarkAsTaken.setVisibility(android.view.View.VISIBLE);
        holder.binding.cbMarkAsTaken.setOnCheckedChangeListener(null);
        holder.binding.cbMarkAsTaken.setChecked(false);
        holder.binding.cbMarkAsTaken.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked && markAsTakenListener != null) markAsTakenListener.onMarkAsTaken(report);
        });

        holder.binding.getRoot().setOnClickListener(v -> listener.onItemClick(report));
    }

    private static String statusToDisplay(String status) {
        if (status == null) return "";
        switch (status) {
            case "occupied": return "Taken";
            case "available": return "Available";
            case "not_relevant": return "Not relevant";
            default: return status;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ReportVH extends RecyclerView.ViewHolder {
        final ItemParkingReportBinding binding;

        ReportVH(@NonNull ItemParkingReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
