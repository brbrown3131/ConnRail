package com.connriverlines.connrail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.connriverlines.connrail.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class SpotList {

    private final ListView lv;
    private final Context context;
    private final ArrayList<SpotData> spotListData; // car list to use


    public SpotList(ListView lv, Context context, ArrayList<SpotData> spotListData) {
        this.lv = lv;
        this.context = context;
        this.spotListData = spotListData;
    }

    private class CustomComparator implements Comparator<SpotData> {
        public int compare(SpotData spot1, SpotData spot2) {
            String s1 = spot1.getTown() + spot1.getIndustry() + spot1.getTrack();
            String s2 = spot2.getTown() + spot2.getIndustry() + spot2.getTrack();
            return s1.compareTo(s2);
        }
    }

    public SpotData getSpotData(int index) {
        return spotListData.get(index);
    }

    public void resetList() {
        //sort the list
        Collections.sort(spotListData, new CustomComparator());

        SpotDataAdapter adapter = new SpotDataAdapter(context, spotListData);
        lv.setAdapter(adapter);
    }

    private static class ViewHolder {
        private TextView tvSpotTown;
        private TextView tvSpotIndustry;
        private TextView tvSpotTrack;
        private TextView tvDays;
    }

    private class SpotDataAdapter extends ArrayAdapter<SpotData> {
        private SpotDataAdapter(Context context, ArrayList<SpotData> spotData) {
            super(context, 0, spotData);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spot_list, parent, false);

                holder = new ViewHolder();
                holder.tvSpotTown = (TextView) convertView.findViewById(R.id.tvSpotTown);
                holder.tvSpotIndustry = (TextView) convertView.findViewById(R.id.tvSpotIndustry);
                holder.tvSpotTrack = (TextView) convertView.findViewById(R.id.tvSpotTrack);
                holder.tvDays = (TextView) convertView.findViewById(R.id.tvSpotDays);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            SpotData sd = getItem(position);

            if (sd != null) {
                holder.tvSpotTown.setText(sd.getTown());
                holder.tvSpotIndustry.setText(sd.getIndustry());
                holder.tvSpotTrack.setText(sd.getTrack());
            }
            holder.tvDays.setText("");

            return convertView;
        }
    }

}
