package education.artem.image_editor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import education.artem.image_editor.CurrentOperation;
import education.artem.image_editor.OperationName;
import education.artem.image_editor.R;

public class FilterFragment extends Fragment {

    private void hideAllComponents(View... views) {
        for (View view : views) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.filter_parameters, container, false);
        Spinner algorithmSpinner = v.findViewById(R.id.filter_algorithm_spinner);
        Spinner parameterSpinner = v.findViewById(R.id.filter_parameter_spinner);
        LinearLayout medianParams = v.findViewById(R.id.medianParams);
        ArrayAdapter<CharSequence> filterAlgorithm = ArrayAdapter.createFromResource(getActivity(),
                R.array.filter_algorithm_array, android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> filterParameter = ArrayAdapter.createFromResource(getActivity(),
                R.array.median_filter_array, android.R.layout.simple_spinner_dropdown_item);
        filterParameter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSpinner.setAdapter(filterAlgorithm);
        algorithmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        medianParams.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        CurrentOperation.setCurrentOperationName(OperationName.GAMMA_CORRECTION);
                        hideAllComponents(medianParams);
                        break;
                    case 2:
                        CurrentOperation.setCurrentOperationName(OperationName.BLUR);
                        hideAllComponents(medianParams);
                        break;
                    case 3:
                        CurrentOperation.setCurrentOperationName(OperationName.GAUSSIAN_BLUR);
                        hideAllComponents(medianParams);
                        break;
                    case 4:
                        CurrentOperation.setCurrentOperationName(OperationName.EMBOSS);
                        hideAllComponents(medianParams);
                        break;
                    case 5:
                        hideAllComponents(medianParams);
                        CurrentOperation.setCurrentOperationName(OperationName.IDENTITY);
                        break;
                    case 6:
                        hideAllComponents(medianParams);
                        CurrentOperation.setCurrentOperationName(OperationName.SHARPEN);
                        break;
                    case 7:
                        hideAllComponents(medianParams);
                        CurrentOperation.setCurrentOperationName(OperationName.BILATERAL);
                        break;
                    case 8:
                        hideAllComponents(medianParams);
                        CurrentOperation.setCurrentOperationName(OperationName.UNSHARPEN);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        parameterSpinner.setAdapter(filterParameter);
        parameterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int p = (position < 0) ? 1 : position + 1;
                int matrixSize = (p * 2) + 1;
                CurrentOperation.getOperationParams().put("matrixSize", String.valueOf(matrixSize));
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return v;
    }

}
