package education.artem.contrastchangeandroid.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Objects;

import education.artem.contrastchangeandroid.CurrentOperation;
import education.artem.contrastchangeandroid.MainActivity;
import education.artem.contrastchangeandroid.OperationName;
import education.artem.contrastchangeandroid.R;

public class FilterFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.filter_parameters, container, false);
        Spinner spinner = v.findViewById(R.id.filter_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.filter_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selected = adapter.getItem(position).toString();
                switch (selected) {
                    case "3x3":
                        CurrentOperation.setCurrentOperation(OperationName.FILTER_3x3);
                        break;
                    case "5x5":
                        CurrentOperation.setCurrentOperation(OperationName.FILTER_5x5);
                        break;
                    case "7x7":
                        CurrentOperation.setCurrentOperation(OperationName.FILTER_7x7);
                        break;
                    case "9x9":
                        CurrentOperation.setCurrentOperation(OperationName.FILTER_9x9);
                        break;
                    case "11x11":
                        CurrentOperation.setCurrentOperation(OperationName.FILTER_11x11);
                        break;
                }
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
        return v;
    }

}
