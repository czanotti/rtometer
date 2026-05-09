package com.rtometer.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rtometer.R;

public class Step6PreloadFragment extends Fragment implements OnboardingActivity.FragmentStep {

    private RadioGroup rgPreload;
    private LinearLayout layoutCount;
    private EditText etCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step6_preload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rgPreload = view.findViewById(R.id.rgPreload);
        layoutCount = view.findViewById(R.id.layoutCount);
        etCount = view.findViewById(R.id.etCount);
        etCount.setText("1");

        rgPreload.setOnCheckedChangeListener((g, id) ->
                layoutCount.setVisibility(id == R.id.rbYesPreload ? View.VISIBLE : View.GONE));
    }

    @Override
    public boolean isValid() {
        if (rgPreload.getCheckedRadioButtonId() == R.id.rbYesPreload) {
            try {
                return Integer.parseInt(etCount.getText().toString().trim()) > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void saveToViewModel(OnboardingViewModel vm) {
        if (rgPreload.getCheckedRadioButtonId() == R.id.rbYesPreload) {
            try {
                vm.setPreloadCount(Integer.parseInt(etCount.getText().toString().trim()));
                return;
            } catch (NumberFormatException ignored) {}
        }
        vm.setPreloadCount(0);
    }
}
