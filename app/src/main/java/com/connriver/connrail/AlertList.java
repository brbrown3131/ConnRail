package com.connriver.connrail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


// displays a list of alerts in a ListView
public class AlertList {

    private ListView lv;
    Context context;
    private ArrayList<AlertData> listData; // car list to use

    // pass in the listview to use and the list of alerts
    public AlertList(ListView lv, Context context, ArrayList<AlertData> listData) {
        this.lv = lv;
        this.context = context;
        this.listData = listData;
    }

    // return the CarData for the selected item
    public AlertData getAlertData(int index) {
        return listData.get(index);
    }

    // redisplay the list
    public void resetList() {
        DataAdapter adapter = new DataAdapter(context, listData);
        lv.setAdapter(adapter);
    }

    static private class ViewHolder {
        private ImageView ivIcon;
        private TextView tvMessage;
    }

    private class DataAdapter extends ArrayAdapter<AlertData> {
        private DataAdapter(Context context, ArrayList<AlertData> data) {
            super(context, 0, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AlertData ad = getItem(position);

            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_alert_list, parent, false);

                holder = new ViewHolder();
                holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
                holder.tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // always show the car initials/number/type
            holder.tvMessage.setText(ad.getMessage());
            showIcon(holder, ad);

            return convertView;
        }

        private void showIcon(ViewHolder holder, AlertData ad) {
            switch (ad.getLevel()) {
                case AlertData.ALERT_INFO:
                    holder.ivIcon.setImageResource(R.drawable.ic_info);
                    break;
                case AlertData.ALERT_WARNING:
                    holder.ivIcon.setImageResource(R.drawable.ic_warning);
                    break;
                case AlertData.ALERT_ERROR:
                    holder.ivIcon.setImageResource(R.drawable.ic_error);
                    break;
            }
        }

    }
}
