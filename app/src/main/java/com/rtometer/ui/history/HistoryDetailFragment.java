package com.rtometer.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rtometer.R;
import com.rtometer.ui.calendar.CalendarAdapter;
import com.rtometer.ui.calendar.CalendarViewModel;
import com.rtometer.ui.calendar.DayStatusBottomSheetFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HistoryDetailFragment extends Fragment {

    static final String ARG_QUARTER_ID = "quarterId";
    static final String ARG_QUARTER_LABEL = "quarterLabel";

    public static HistoryDetailFragment newInstance(long quarterId, String quarterLabel) {
        HistoryDetailFragment f = new HistoryDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_QUARTER_ID, quarterId);
        args.putString(ARG_QUARTER_LABEL, quarterLabel);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        long quarterId = args.getLong(ARG_QUARTER_ID);
        String quarterLabel = args.getString(ARG_QUARTER_LABEL, "");

        CalendarViewModel vm = new ViewModelProvider(this).get(CalendarViewModel.class);

        ((TextView) view.findViewById(R.id.historyDetailTitle)).setText(quarterLabel);
        view.findViewById(R.id.historyDetailBack).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        CalendarAdapter adapter = new CalendarAdapter();
        adapter.setOnDayClickListener(day ->
                DayStatusBottomSheetFragment.newInstance(day.date)
                        .show(getChildFragmentManager(), "day_status"));

        RecyclerView recycler = view.findViewById(R.id.historyDetailRecycler);
        GridLayoutManager glm = new GridLayoutManager(requireContext(), 7);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getItemViewType(position) == CalendarAdapter.TYPE_MONTH_HEADER ? 7 : 1;
            }
        });
        recycler.setLayoutManager(glm);
        recycler.setAdapter(adapter);

        vm.months.observe(getViewLifecycleOwner(), adapter::setData);

        vm.loadQuarter(quarterId);
    }
}
