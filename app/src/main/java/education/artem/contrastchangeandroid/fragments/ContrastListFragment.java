package education.artem.contrastchangeandroid.fragments;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import education.artem.contrastchangeandroid.ProjectLocale;

public class ContrastListFragment extends ListFragment {

    ResourceBundle bundle = ResourceBundle.getBundle("ResourceBundle", ProjectLocale.getProjectLocale());

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<String> data = new ArrayList<String>();
        data.add(bundle.getString("key.linear"));
        data.add(bundle.getString("key.equalize"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, data);
        setListAdapter(adapter);
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

    }

}
