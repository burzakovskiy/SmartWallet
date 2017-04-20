package com.rbsoftware.pfm.personalfinancemanager.banking;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.rbsoftware.pfm.personalfinancemanager.R;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Roman Burzakovskiy
 */
public class SelectCountry extends Fragment {

    private ListView.OnItemClickListener onClickListener;



    public SelectCountry() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_select_country, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = (ListView) getActivity().findViewById(R.id.select_country_list);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),R.array.bank_integration_countries, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(onClickListener);
    }

    public void setOnClickListener(ListView.OnItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
