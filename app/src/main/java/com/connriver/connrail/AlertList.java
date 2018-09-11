package com.connriver.connrail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


// displays a list of alerts in a ListView
class AlertList {

    private final ListView lv;
    private final Context context;

    // pass in the listview to use and the list of alerts
    AlertList(ListView lv, Context context) {
        this.lv = lv;
        this.context = context;
    }

    // redisplay the list
    public void resetList() {
        DataAdapter adapter = new DataAdapter(context);
        lv.setAdapter(adapter);
    }

    static private class ViewHolder {
        private ImageView ivIcon;
        private TextView tvMessage;
    }

    private class DataAdapter extends ArrayAdapter<AlertData> {
        private DataAdapter(Context context) {
            super(context, 0, MainActivity.alerts);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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

            AlertData ad = getItem(position);

            // always show the car initials/number/type
            if (ad != null) {
                holder.tvMessage.setText(ad.getMessage());
                showIcon(holder, ad);
            }


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
