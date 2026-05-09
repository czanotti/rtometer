package com.rtometer.ui.office;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rtometer.R;
import com.rtometer.data.db.Office;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OfficeAdapter extends RecyclerView.Adapter<OfficeAdapter.ViewHolder> {

    interface Listener {
        void onEdit(Office office);
        void onDelete(Office office);
    }

    private List<Office> offices = Collections.emptyList();
    private Listener listener;

    public void setOffices(List<Office> offices) {
        this.offices = offices;
        notifyDataSetChanged();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_office, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(offices.get(position));
    }

    @Override
    public int getItemCount() {
        return offices.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final TextView name;
        final TextView details;
        final ImageButton editBtn;
        final ImageButton deleteBtn;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.officeName);
            details = itemView.findViewById(R.id.officeDetails);
            editBtn = itemView.findViewById(R.id.officeEditBtn);
            deleteBtn = itemView.findViewById(R.id.officeDeleteBtn);
        }

        void bind(Office office) {
            name.setText(office.isPrimary
                    ? itemView.getContext().getString(R.string.office_primary_label, office.name)
                    : office.name);
            details.setText(String.format(Locale.getDefault(),
                    "%.5f, %.5f · %dm", office.latitude, office.longitude, office.radiusMeters));
            editBtn.setOnClickListener(v -> { if (listener != null) listener.onEdit(office); });
            deleteBtn.setOnClickListener(v -> { if (listener != null) listener.onDelete(office); });
        }
    }
}
