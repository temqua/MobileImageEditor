package education.artem.contrastchangeandroid.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import education.artem.contrastchangeandroid.MainActivity;
import education.artem.contrastchangeandroid.R;

public class FilterFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.filter_parameters, container, false);
    }

}
