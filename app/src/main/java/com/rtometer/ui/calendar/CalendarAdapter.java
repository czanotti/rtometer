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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public void setOnDayClickListener(OnDayClickListener l) {
        this.listener = l;
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
            ((DayHolder) holder).bind(item.day, listener);
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

        void bind(CalendarDay day, OnDayClickListener listener) {
            dayNumber.setText(String.valueOf(day.date.getDayOfMonth()));
            overrideIndicator.setVisibility(day.isManualOverride ? View.VISIBLE : View.GONE);

            // Background tint by status / day type
            int bg = Color.TRANSPARENT;
            if (day.status == DayStatus.IN_OFFICE) {
                bg = 0xFFB8E6B8; // green
            } else if (day.status == DayStatus.SICK) {
                bg = 0xFFFFDDB3; // orange
            } else if (day.status == DayStatus.HOLIDAY) {
                bg = 0xFFB3D9FF; // blue
            } else if (day.isWeekend || day.isBankHoliday) {
                bg = 0xFFEEEEEE; // gray
            }
            itemView.setBackgroundColor(bg);

            if (day.isToday) {
                dayNumber.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                dayNumber.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null && !day.isWeekend && !day.isBankHoliday) {
                    listener.onDayClick(day);
                }
            });
        }
    }
}
