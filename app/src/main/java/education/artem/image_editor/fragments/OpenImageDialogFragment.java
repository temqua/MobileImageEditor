package education.artem.image_editor.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;

import java.util.Objects;

import education.artem.image_editor.MainActivity;

public class OpenImageDialogFragment extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Ошибка!")
                .setMessage("Откройте изображение!")
                .setPositiveButton("Открыть", (dialog, id) -> {
                    // Закрываем окно
                    dialog.cancel();
                    ((MainActivity)Objects.requireNonNull(getActivity())).performFileSearch();
                });
        return builder.create();
    }
}