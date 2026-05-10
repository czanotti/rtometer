package com.rtometer.ui.calendar;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rtometer.R;
import com.rtometer.data.model.DayStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static final int TYPE_MONTH_HEADER = 0;
    static final int TYPE_WEEKDAY = 1;
    static final int TYPE_EMPTY = 2;
    static final int TYPE_DAY = 3;

    private static final String[] WEEKDAY_LABELS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());

    interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    interface OnBulkDayToggleListener {
        void onBulkDayToggle(CalendarDay day);
    }

    static class Item {
        final int type;
        final String label;
        final CalendarDay day;

        private Item(int type, String label, CalendarDay day) {
            this.type = type;
            this.label = label;
            this.day = day;
        }

        static Item monthHeader(String label) { return new Item(TYPE_MONTH_HEADER, label, null); }
        static Item weekday(String label)     { return new Item(TYPE_WEEKDAY, label, null); }
        static Item empty()                   { return new Item(TYPE_EMPTY, null, null); }
        static Item day(CalendarDay d)        { return new Item(TYPE_DAY, null, d); }
    }

    private final List<Item> items = new ArrayList<>();
    private OnDayClickListener listener;
    private OnBulkDayToggleListener bulkToggleListener;
    private boolean bulkMode = false;
    private Set<LocalDate> bulkSelected = Collections.emptySet();

    public void setOnDayClickListener(OnDayClickListener l) {
        this.listener = l;
    }

    public void setOnBulkDayToggleListener(OnBulkDayToggleListener l) {
        this.bulkToggleListener = l;
    }

    public void setBulkMode(boolean enabled) {
        this.bulkMode = enabled;
        notifyDataSetChanged();
    }

    public void setBulkSelectedDays(Set<LocalDate> selected) {
        this.bulkSelected = selected != null ? selected : Collections.emptySet();
        notifyDataSetChanged();
    }

    public void setData(List<CalendarMonth> months) {
        items.clear();
        for (CalendarMonth m : months) {
            items.add(Item.monthHeader(m.month.format(MONTH_FMT)));
            for (String wd : WEEKDAY_LABELS) {
                items.add(Item.weekday(wd));
            }
            for (CalendarDay d : m.days) {
                items.add(d == null ? Item.empty() : Item.day(d));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_MONTH_HEADER:
                return new LabelHolder(inflater.inflate(R.layout.item_calendar_month_header, parent, false));
            case TYPE_WEEKDAY:
                return new LabelHolder(inflater.inflate(R.layout.item_calendar_weekday, parent, false));
            case TYPE_EMPTY:
                return new EmptyHolder(inflater.inflate(R.layout.item_calendar_empty, parent, false));
            default:
                return new DayHolder(inflater.inflate(R.layout.item_calendar_day, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = items.get(position);
        if (holder instanceof LabelHolder) {
            ((LabelHolder) holder).text.setText(item.label);
        } else if (holder instanceof DayHolder) {
            ((DayHolder) holder).bind(item.day, listener, bulkMode, bulkSelected, bulkToggleListener);
        }
    }

    // ── ViewHolders ───────────────────────────────────────────────────────────

    static class LabelHolder extends RecyclerView.ViewHolder {
        final TextView text;
        LabelHolder(@NonNull View v) {
            super(v);
            text = (TextView) v;
        }
    }

    static class EmptyHolder extends RecyclerView.ViewHolder {
        EmptyHolder(@NonNull View v) { super(v); }
    }

    static class DayHolder extends RecyclerView.ViewHolder {
        final TextView dayNumber;
        final View overrideIndicator;

        DayHolder(@NonNull View v) {
            super(v);
            dayNumber = v.findViewById(R.id.dayNumber);
            overrideIndicator = v.findViewById(R.id.overrideIndicator);
        }

        void bind(CalendarDay day, OnDayClickListener listener,
                  boolean inBulkMode, Set<LocalDate> selected, OnBulkDayToggleListener bulkListener) {
            dayNumber.setText(String.valueOf(day.date.getDayOfMonth()));
            overrideIndicator.setVisibility(day.isManualOverride ? View.VISIBLE : View.GONE);
            dayNumber.setTypeface(null, day.isToday ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

            int bg = Color.TRANSPARENT;
            if (inBulkMode && selected.contains(day.date)) {
                bg = 0xFFFFE082; // amber — distinct selection highlight
            } else if (day.status == DayStatus.IN_OFFICE) {
                bg = 0xFFB8E6B8;
            } else if (day.status == DayStatus.SICK) {
                bg = 0xFFFFDDB3;
            } else if (day.status == DayStatus.HOLIDAY) {
                bg = 0xFFB3D9FF;
            } else if (day.isWeekend || day.isBankHoliday) {
                bg = 0xFFEEEEEE;
            }
            itemView.setBackgroundColor(bg);

            if (inBulkMode) {
                itemView.setOnClickListener(v -> {
                    if (bulkListener != null && !day.isBankHoliday) {
                        bulkListener.onBulkDayToggle(day);
                    }
                });
            } else {
                itemView.setOnClickListener(v -> {
                    if (listener != null && !day.isWeekend && !day.isBankHoliday) {
                        listener.onDayClick(day);
                    }
                });
            }
        }
    }
}
