package com.connriver.connrail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ConsistList {

    private ListView lv;
    Context context;
    private ArrayList<ConsistData> consistListData; // car list to use

    public ConsistList(ListView lv, Context context, ArrayList<ConsistData> consistListData) {
        this.lv = lv;
        this.context = context;
        this.consistListData = consistListData;
    }

    private class CustomComparator implements Comparator<ConsistData> {
        public int compare(ConsistData con1, ConsistData con2) {
            String s1 = con1.getName();
            String s2 = con2.getName();
            return s1.compareTo(s2);
        }
    }

    public ConsistData getConsistData(int index) {
        return consistListData.get(index);
    }

    public void resetList() {
        //sort the list
        Collections.sort(consistListData, new CustomComparator());

        ConsistDataAdapter adapter = new ConsistDataAdapter(context, consistListData);
        lv.setAdapter(adapter);

    }

    static private class ViewHolder {
        private TextView tvName;
        private TextView tvDesc;
    }

    private class ConsistDataAdapter extends ArrayAdapter<ConsistData> {
        private ConsistDataAdapter(Context context, ArrayList<ConsistData> consistData) {
            super(context, 0, consistData);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ConsistData cd = getItem(position);

            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_consist_list, parent, false);

                holder = new ViewHolder();
                holder.tvName = (TextView) convertView.findViewById(R.id.tvConsistName);
                holder.tvDesc = (TextView) convertView.findViewById(R.id.tvConsistDescription);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvName.setText(cd.getName());
            holder.tvDesc.setText(cd.getDescription());

            return convertView;
        }
    }
}
