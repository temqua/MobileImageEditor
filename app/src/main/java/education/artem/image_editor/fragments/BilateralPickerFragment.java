package education.artem.image_editor.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Objects;

import education.artem.image_editor.CurrentOperation;
import education.artem.image_editor.MainActivity;
import education.artem.image_editor.R;

public class BilateralPickerFragment extends DialogFragment {

    private double intensitySigma;
    private double distanceSigma;

    public BilateralPickerFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        NumberPicker distancePicker = new NumberPicker(getActivity());
        distancePicker.setMinValue(1);
        distancePicker.setMaxValue(100);
        distancePicker.setWrapSelectorWheel(false);
        distancePicker.setOnValueChangedListener((picker, oldVal, newVal) -> this.distanceSigma = newVal);

        NumberPicker intensityPicker = new NumberPicker(getActivity());
        intensityPicker.setMinValue(1);
        intensityPicker.setMaxValue(100);
        intensityPicker.setWrapSelectorWheel(false);
        intensityPicker.setOnValueChangedListener((picker, oldVal, newVal) -> this.intensitySigma = newVal);

        TextView distanceText = new TextView(getActivity());
        distanceText.setText(R.string.distanceSigmaDialog);
        TextView intensityText = new TextView(getActivity());
        intensityText.setText(R.string.intensitySigmaDialog);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(distanceText);
        layout.addView(distancePicker);
        layout.addView(intensityText);
        layout.addView(intensityPicker);

        builder.setTitle(R.string.bilateralFilter)
                .setMessage(R.string.bilateralFilterDialog)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .setPositiveButton(R.string.ok, ((dialog, which) -> {
                    CurrentOperation.getOperationParams().put("distanceSigma", String.valueOf(this.distanceSigma));
                    CurrentOperation.getOperationParams().put("intensitySigma", String.valueOf(this.intensitySigma));
                    ((MainActivity) Objects.requireNonNull(getActivity())).executeCurrentTask();
                }));

        builder.setView(layout);
        return builder.create();
    }

}
