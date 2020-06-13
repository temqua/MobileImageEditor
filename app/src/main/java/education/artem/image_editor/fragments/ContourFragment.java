package education.artem.image_editor.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import education.artem.image_editor.CurrentOperation;
import education.artem.image_editor.OperationName;
import education.artem.image_editor.R;

public class ContourFragment extends Fragment {

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contour_parameters, container, false);
        Spinner spinner = v.findViewById(R.id.contoursSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.contours_array, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selected = adapter.getItem(position).toString();
                switch (selected) {
                    case "Prewitt":
                        CurrentOperation.setCurrentOperationName(OperationName.CONTOURS_PREWITT);
                        break;
                    case "Prewitt grayscale":
                        CurrentOperation.setCurrentOperationName(OperationName.CONTOURS_PREWITT_GRAYSCALE);
                        break;
                    case "Sobel":
                        CurrentOperation.setCurrentOperationName(OperationName.CONTOURS_SOBEL);
                        break;
                    case "Sobel grayscale":
                        CurrentOperation.setCurrentOperationName(OperationName.CONTOURS_SOBEL_GRAYSCALE);
                        break;
                    case "Laplacian 3x3":
                        CurrentOperation.setCurrentOperationName(OperationName.CONTOURS_LAPLASIAN_3X3);
                        break;
                    case "Laplacian 3x3 grayscale":
                        CurrentOperation.setCurrentOperationName(OperationName.CONTOURS_LAPLASIAN_3X3_GRAYSCALE);
                        break;
                    case "Laplacian 5x5":
                        CurrentOperation.setCurrentOperationName(OperationName.CONTOURS_LAPLASIAN_5X5);
                        break;
                    case "Laplacian 5x5 grayscale":
                        CurrentOperation.setCurrentOperationName(OperationName.CONTOURS_LAPLASIAN_5X5_GRAYSCALE);
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
