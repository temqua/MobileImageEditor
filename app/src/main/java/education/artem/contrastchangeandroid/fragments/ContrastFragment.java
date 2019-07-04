package education.artem.contrastchangeandroid.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import education.artem.contrastchangeandroid.CurrentOperation;
import education.artem.contrastchangeandroid.OperationName;
import education.artem.contrastchangeandroid.R;


public class ContrastFragment extends Fragment {
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contrast_parameters, container, false);
        Spinner spinner = v.findViewById(R.id.contrast_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.contrast_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selected = adapter.getItem(position).toString();
                switch (selected) {
                    case "Linear":
                        CurrentOperation.setCurrentOperation(OperationName.LINEAR_CONTRAST);
                        break;
                    case "Histogram equalize":
                        CurrentOperation.setCurrentOperation(OperationName.EQUALIZE_CONTRAST);
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
