package com.connriver.connrail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.connriver.connrail.MainActivity.NONE;

// displays a list of car data in a ListView
class CarList {

    private final ListView lv;
    private final Context context;
    private final ArrayList<CarData> carListData; // car list to use
    private boolean bShowCurr = true;
    private boolean bShowDest = true;

    // pass in the listview to use and the list of cars
    public CarList(ListView lv, Context context, ArrayList<CarData> carListData) {
        this.lv = lv;
        this.lv.setFastScrollEnabled(true);
        this.context = context;
        this.carListData = carListData;
    }

    private class CustomComparator implements Comparator<CarData> {
        public int compare(CarData car1, CarData car2) {
            String s1 = car1.getInitials() + car1.getNumber();
            String s2 = car2.getInitials() + car2.getNumber();
            return s1.compareTo(s2);
        }
    }

    public void setShowCurr(boolean bx) {
        bShowCurr = bx;
    }

    public void setShowDest(boolean bx) {
        bShowDest = bx;
    }

    // return the CarData for the selected item
    public CarData getCarData(int index) {
        return carListData.get(index);
    }

    // redisplay the list - call when a car is added or removed from the list
    public void resetList() {

        //sort the list
        Collections.sort(carListData, new CustomComparator());

        CarDataAdapter adapter = new CarDataAdapter(context, carListData);
        lv.setAdapter(adapter);
    }

    static private class ViewHolder {
        private TextView tvCarInitials;
        private TextView tvCarNumber;
        private TextView tvCarType;

        private LinearLayout llNoCurr;
        private LinearLayout llCurr;
        private ImageView ivCurr;
        private TextView tvTownCurr;
        private TextView tvIndustryCurr;
        private TextView tvTrackCurr;

        private LinearLayout llErrDest;
        private LinearLayout llHold;
        private LinearLayout llDest;
        private TextView tvTownDest;
        private TextView tvIndustryDest;
        private TextView tvTrackDest;
    }

    private class CarDataAdapter extends ArrayAdapter<CarData> {
        private CarDataAdapter(Context context, ArrayList<CarData> carData) {
            super(context, 0, carData);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_car_list, parent, false);

                holder = new ViewHolder();
                holder.tvCarInitials = (TextView) convertView.findViewById(R.id.tvCarInitials);
                holder.tvCarNumber = (TextView) convertView.findViewById(R.id.tvCarNumber);
                holder.tvCarType = (TextView) convertView.findViewById(R.id.tvCarType);

                holder.llNoCurr = (LinearLayout) convertView.findViewById(R.id.llNoCurrent);
                holder.llCurr = (LinearLayout) convertView.findViewById(R.id.llCurrent);
                holder.ivCurr = (ImageView)  convertView.findViewById(R.id.ivCurrent);
                holder.tvTownCurr = (TextView) convertView.findViewById(R.id.tvSpotTownCurr);
                holder.tvIndustryCurr = (TextView) convertView.findViewById(R.id.tvSpotIndustryCurr);
                holder.tvTrackCurr = (TextView) convertView.findViewById(R.id.tvSpotTrackCurr);

                holder.llErrDest = (LinearLayout) convertView.findViewById(R.id.llErrDest);
                holder.llHold = (LinearLayout) convertView.findViewById(R.id.llHold);
                holder.llDest = (LinearLayout) convertView.findViewById(R.id.llDestination);
                holder.tvTownDest = (TextView) convertView.findViewById(R.id.tvSpotTownDest);
                holder.tvIndustryDest = (TextView) convertView.findViewById(R.id.tvSpotIndustryDest);
                holder.tvTrackDest = (TextView) convertView.findViewById(R.id.tvSpotTrackDest);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            CarData cd = getItem(position);
            if (cd == null) {
                return convertView;
            }

            // always show the car initials/number/type
            holder.tvCarInitials.setText(cd.getInitials());
            holder.tvCarNumber.setText(cd.getNumber());
            holder.tvCarType.setText(cd.getType());

            if (bShowCurr) {
                showCurr(holder, cd);
            } else {
                holder.llCurr.setVisibility(View.GONE);
            }

            if (bShowDest) {
                showDest(holder, cd);
            } else {
                holder.llDest.setVisibility(View.GONE);
            }

            // show invalid spots in all lists
            if (cd.invalidSpots()) {
                holder.llErrDest.setVisibility(View.VISIBLE);
                holder.llDest.setVisibility(View.GONE);
            }


            return convertView;
        }

        private void showCurr(ViewHolder holder, CarData cd) {
            // always default to normal current display
            holder.llNoCurr.setVisibility(View.GONE);
            holder.llCurr.setVisibility(View.VISIBLE);

            if (cd.getInStorage()) {
                holder.ivCurr.setImageResource(R.drawable.ic_archive);
                holder.tvTownCurr.setText(R.string.status_is_stored);
                holder.tvIndustryCurr.setText("");
                holder.tvTrackCurr.setText("");
                return;
            }

            int iConsist = cd.getConsist();
            if (iConsist != NONE) {
                holder.ivCurr.setImageResource(R.drawable.ic_train);
                ConsistData csd = Utils.getConsistFromID(iConsist);
                if (csd == null) {
                    return;
                }
                holder.tvTownCurr.setText(csd.getName());
                holder.tvIndustryCurr.setText("");
                holder.tvTrackCurr.setText("");
                return;
            }

            holder.ivCurr.setImageResource(R.drawable.ic_crosshairs_gps);
            int iCurr = cd.getCurrentLoc();
            if (iCurr == NONE) {
                holder.llNoCurr.setVisibility(View.VISIBLE);
                holder.llCurr.setVisibility(View.GONE);
            } else {
                SpotData sd = Utils.getSpotFromID(iCurr);
                if (sd == null) {
                    holder.llNoCurr.setVisibility(View.VISIBLE);
                    holder.llCurr.setVisibility(View.GONE);
                } else {
                    holder.tvTownCurr.setText(sd.getTown());
                    holder.tvIndustryCurr.setText(sd.getIndustry());
                    holder.tvTrackCurr.setText(sd.getTrack());
                }
            }
        }

        private void showDest(ViewHolder holder, CarData cd) {
            if (cd.getInStorage()) {
                holder.llDest.setVisibility(View.GONE);
                return;
            }

            if (MainActivity.getSessionNumber() < cd.getHoldUntilDay()) {
                holder.llHold.setVisibility(View.VISIBLE);
                holder.llDest.setVisibility(View.GONE);
                return;
            }

            CarSpotData spot = cd.getNextSpot();
            if (spot == null) {
                return;
            }
            SpotData sd = Utils.getSpotFromID(spot.getID());
            if (sd != null) {
                holder.tvTownDest.setText(sd.getTown());
                holder.tvIndustryDest.setText(sd.getIndustry());
                holder.tvTrackDest.setText(sd.getTrack());
            }
        }
    }
}
