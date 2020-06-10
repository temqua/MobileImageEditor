package education.artem.image_editor.fragments;

import android.os.Bundle;
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
        View gammaValue = v.findViewById(R.id.gammaValue);
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
                String selected = filterAlgorithm.getItem(position).toString();
                switch (selected) {
                    case "Median filter":
                        medianParams.setVisibility(View.VISIBLE);
                        gammaValue.setVisibility(View.INVISIBLE);
                        break;
                    case "Gamma correction":
                        medianParams.setVisibility(View.INVISIBLE);
                        gammaValue.setVisibility(View.VISIBLE);
                        CurrentOperation.setCurrentOperation(OperationName.GAMMA_CORRECTION);
                        break;
                    case "Blur":
                        hideAllComponents(medianParams, gammaValue);
                        CurrentOperation.setCurrentOperation(OperationName.BLUR);
                        break;
                    case "Gaussian blur":
                        hideAllComponents(medianParams, gammaValue);
                        CurrentOperation.setCurrentOperation(OperationName.GAUSSIAN_BLUR);
                        break;
                    case "Emboss":
                        hideAllComponents(medianParams, gammaValue);
                        CurrentOperation.setCurrentOperation(OperationName.EMBOSS);
                        break;
                    case "Identity":
                        hideAllComponents(medianParams, gammaValue);
                        CurrentOperation.setCurrentOperation(OperationName.IDENTITY);
                        break;
                    case "Sharpen":
                        hideAllComponents(medianParams, gammaValue);
                        CurrentOperation.setCurrentOperation(OperationName.SHARPEN);
                        break;
                    case "Bilateral":
                        hideAllComponents(medianParams, gammaValue);
                        CurrentOperation.setCurrentOperation(OperationName.BILATERAL);
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
                String selected = filterParameter.getItem(position).toString();
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

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return v;
    }

}
