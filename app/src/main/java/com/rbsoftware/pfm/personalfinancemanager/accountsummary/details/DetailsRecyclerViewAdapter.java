package com.rbsoftware.pfm.personalfinancemanager.accountsummary.details;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.rbsoftware.pfm.personalfinancemanager.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Sectioned recycler view adapter
 * Created by Roman Burzakovskiy on 7/13/2016.
 */
public class DetailsRecyclerViewAdapter extends SectionedRecyclerViewAdapter<DetailsRecyclerViewAdapter.ViewHolder> {
    private LinkedHashMap<String, List<String[]>> data;
    private List<String> keyList;

    public DetailsRecyclerViewAdapter(LinkedHashMap<String, List<String[]>> data) {
        this.data = data;
        keyList = new ArrayList<>();
        for (String key : data.keySet()) {
            keyList.add(key);
        }
    }

    @Override
    public int getSectionCount() {
        return data.size();
    }

    @Override
    public int getItemCount(int section) {
        String key = keyList.get(section);
        return data.get(key).size();
    }

    @Override
    public void onBindHeaderViewHolder(ViewHolder holder, int section) {
        if (holder.header != null) {
            holder.header.setText(keyList.get(section));
        }

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int section, int relativePosition, int absolutePosition) {
        if (holder.category != null && holder.value != null) {
            List<String[]> dataList = data.get(keyList.get(section));
            holder.category.setText(dataList.get(relativePosition)[0]);
            holder.value.setText(dataList.get(relativePosition)[1]);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layout;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                layout = R.layout.details_list_item_header;
                break;
            case VIEW_TYPE_ITEM:
                layout = R.layout.details_list_item_main;
                break;
            default:
                layout = R.layout.details_list_item_main;
                break;
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView header;
        private TextView category;
        private TextView value;

        public ViewHolder(View itemView) {
            super(itemView);
            header = (TextView) itemView.findViewById(R.id.details_list_header_textview);
            category = (TextView) itemView.findViewById(R.id.details_list_main_category_textview);
            value = (TextView) itemView.findViewById(R.id.details_list_main_value_textview);
        }
    }
}
