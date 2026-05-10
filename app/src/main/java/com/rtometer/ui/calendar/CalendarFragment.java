package com.rtometer.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rtometer.R;
import com.rtometer.data.model.DayStatus;

import java.time.LocalDate;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CalendarFragment extends Fragment {

    private CalendarViewModel viewModel;
    private CalendarAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        adapter = new CalendarAdapter();
        adapter.setOnDayClickListener(day ->
                DayStatusBottomSheetFragment.newInstance(day.date)
                        .show(getChildFragmentManager(), "day_status"));
        adapter.setOnBulkDayToggleListener(day -> viewModel.toggleBulkDay(day.date));

        RecyclerView recycler = view.findViewById(R.id.calendarRecycler);
        GridLayoutManager glm = new GridLayoutManager(requireContext(), 7);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position) == CalendarAdapter.TYPE_MONTH_HEADER ? 7 : 1;
            }
        });
        recycler.setLayoutManager(glm);
        recycler.setAdapter(adapter);

        View bulkPanel = view.findViewById(R.id.bulkPanel);
        FloatingActionButton fabBulk = view.findViewById(R.id.fabBulk);
        RadioGroup statusPicker = view.findViewById(R.id.statusPicker);
        Button btnApply = view.findViewById(R.id.btnBulkApply);
        Button btnCancel = view.findViewById(R.id.btnBulkCancel);

        fabBulk.setOnClickListener(v -> viewModel.enterBulkMode());
        btnApply.setOnClickListener(v -> viewModel.applyBulkStatus());
        btnCancel.setOnClickListener(v -> viewModel.exitBulkMode());

        statusPicker.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioInOffice)         viewModel.setBulkStatus(DayStatus.IN_OFFICE);
            else if (checkedId == R.id.radioNotInOffice) viewModel.setBulkStatus(DayStatus.NOT_IN_OFFICE);
            else if (checkedId == R.id.radioSick)        viewModel.setBulkStatus(DayStatus.SICK);
            else if (checkedId == R.id.radioHoliday)     viewModel.setBulkStatus(DayStatus.HOLIDAY);
            else if (checkedId == R.id.radioClear)       viewModel.setBulkStatus(null);
        });

        viewModel.months.observe(getViewLifecycleOwner(), adapter::setData);

        viewModel.bulkMode.observe(getViewLifecycleOwner(), isBulk -> {
            bulkPanel.setVisibility(isBulk ? View.VISIBLE : View.GONE);
            fabBulk.setVisibility(isBulk ? View.GONE : View.VISIBLE);
            adapter.setBulkMode(isBulk);
            if (!isBulk) statusPicker.check(R.id.radioInOffice);
        });

        viewModel.bulkSelectedDays.observe(getViewLifecycleOwner(), adapter::setBulkSelectedDays);

        // Load the quarter that contains today
        String today = LocalDate.now().toString();
        viewModel.quarterDao.observeByDate(today).observe(getViewLifecycleOwner(), quarter -> {
            if (quarter != null) viewModel.loadQuarter(quarter.id);
        });
    }
}
