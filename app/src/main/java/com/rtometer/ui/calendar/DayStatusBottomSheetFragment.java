package com.rtometer.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.rtometer.R;
import com.rtometer.data.model.DayStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DayStatusBottomSheetFragment extends BottomSheetDialogFragment {

    static final String ARG_DATE = "date";
    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("EEE, MMM d yyyy", Locale.getDefault());

    public static DayStatusBottomSheetFragment newInstance(LocalDate date) {
        DayStatusBottomSheetFragment f = new DayStatusBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date.toString());
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_day_status_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LocalDate date = LocalDate.parse(requireArguments().getString(ARG_DATE));
        CalendarViewModel vm = new ViewModelProvider(requireParentFragment())
                .get(CalendarViewModel.class);

        ((TextView) view.findViewById(R.id.dateLabel)).setText(date.format(DISPLAY_FMT));

        view.findViewById(R.id.btnInOffice).setOnClickListener(v -> pick(vm, date, DayStatus.IN_OFFICE));
        view.findViewById(R.id.btnNotInOffice).setOnClickListener(v -> pick(vm, date, DayStatus.NOT_IN_OFFICE));
        view.findViewById(R.id.btnSick).setOnClickListener(v -> pick(vm, date, DayStatus.SICK));
        view.findViewById(R.id.btnHoliday).setOnClickListener(v -> pick(vm, date, DayStatus.HOLIDAY));
        view.findViewById(R.id.btnBankHoliday).setOnClickListener(v -> pick(vm, date, DayStatus.BANK_HOLIDAY));
        view.findViewById(R.id.btnClear).setOnClickListener(v -> {
            vm.clearDayStatus(date);
            dismiss();
        });
    }

    private void pick(CalendarViewModel vm, LocalDate date, DayStatus status) {
        vm.updateDayStatus(date, status);
        dismiss();
    }
}
