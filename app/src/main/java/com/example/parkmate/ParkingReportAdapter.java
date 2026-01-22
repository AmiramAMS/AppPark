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

    private final List<ParkingReport> items;
    private final OnItemClickListener listener;

    public ParkingReportAdapter(List<ParkingReport> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
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
        holder.binding.tvStatus.setText(report.status);

        holder.binding.getRoot().setOnClickListener(v -> listener.onItemClick(report));
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
