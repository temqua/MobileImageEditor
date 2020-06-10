package education.artem.image_editor.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import education.artem.image_editor.CurrentOperation;
import education.artem.image_editor.OperationName;
import education.artem.image_editor.R;


public class ContrastFragment extends Fragment {
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contrast_parameters, container, false);
        LinearLayout contrastParams = v.findViewById(R.id.contrastParams);
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
                        contrastParams.setVisibility(View.VISIBLE);
                        break;
                    case "Histogram equalize":
                        CurrentOperation.setCurrentOperation(OperationName.EQUALIZE_CONTRAST);
                        contrastParams.setVisibility(View.INVISIBLE);
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
