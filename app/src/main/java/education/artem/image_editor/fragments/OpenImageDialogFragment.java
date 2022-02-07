package education.artem.image_editor.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

import education.artem.image_editor.MainActivity;
import education.artem.image_editor.R;

public class OpenImageDialogFragment extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.error)
                .setMessage(R.string.openImageDialog)
                .setIcon(R.drawable.ic_baseline_error_24)
                .setPositiveButton(R.string.open, (dialog, id) -> {
                    dialog.dismiss();
                    ((MainActivity) Objects.requireNonNull(getActivity())).performFileSearch();
                });
        return builder.create();
    }
}
