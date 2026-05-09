package com.rtometer.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rtometer.R;

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

        viewModel.months.observe(getViewLifecycleOwner(), adapter::setData);

        // Load the quarter that contains today
        String today = LocalDate.now().toString();
        viewModel.quarterDao.observeByDate(today).observe(getViewLifecycleOwner(), quarter -> {
            if (quarter != null) viewModel.loadQuarter(quarter.id);
        });
    }
}
