package com.rtometer.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.rtometer.R;
import com.rtometer.calculator.QuarterStats;
import com.rtometer.data.db.Quarter;
import com.rtometer.history.PastQuarterEntry;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnQuarterClickListener {
        void onQuarterClick(PastQuarterEntry entry);
    }

    private List<PastQuarterEntry> entries = Collections.emptyList();
    @Nullable private OnQuarterClickListener clickListener;

    public void setEntries(List<PastQuarterEntry> entries) {
        this.entries = entries;
        notifyDataSetChanged();
    }

    public void setOnQuarterClickListener(@Nullable OnQuarterClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_quarter, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PastQuarterEntry entry = entries.get(position);
        holder.bind(entry);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onQuarterClick(entry);
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private static final DateTimeFormatter MONTH_DAY = DateTimeFormatter.ofPattern("MMM d");

        final TextView quarterLabel;
        final TextView percentage;
        final TextView daysAttended;
        final TextView targetStatus;

        ViewHolder(View itemView) {
            super(itemView);
            quarterLabel = itemView.findViewById(R.id.historyQuarterLabel);
            percentage = itemView.findViewById(R.id.historyPercentage);
            daysAttended = itemView.findViewById(R.id.historyDaysAttended);
            targetStatus = itemView.findViewById(R.id.historyTargetStatus);
        }

        void bind(PastQuarterEntry entry) {
            Quarter q = entry.quarter;
            QuarterStats stats = entry.stats;

            quarterLabel.setText(itemView.getContext().getString(
                    R.string.dashboard_quarter_label,
                    q.quarterNumber,
                    q.startDate.format(MONTH_DAY),
                    q.endDate.format(MONTH_DAY)));

            int pct = Math.round(stats.percentage * 100);
            percentage.setText(String.format(Locale.getDefault(), "%d%%", pct));

            daysAttended.setText(itemView.getContext().getString(
                    R.string.history_days_fraction,
                    stats.daysAttended,
                    stats.totalWorkingDays));

            boolean metTarget = stats.percentage >= q.targetPercentage;
            targetStatus.setText(metTarget
                    ? itemView.getContext().getString(R.string.history_target_met)
                    : itemView.getContext().getString(R.string.history_target_missed));
        }
    }
}
