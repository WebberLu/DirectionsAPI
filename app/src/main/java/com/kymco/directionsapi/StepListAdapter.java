package com.kymco.directionsapi;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 2016/2/26.
 */
public class StepListAdapter extends BaseAdapter {
    private LayoutInflater myInflater;
    private Context mContext;
    List<HashMap<String, String>> mSteps_hm = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> mLegs_hm = new HashMap<String, String>();

    public StepListAdapter(Context context) {
        this.mContext = context;
    }

    public List<HashMap<String, String>> getSteps() {
        return mSteps_hm;
    }

    public void setSteps(List<HashMap<String, String>> steps) {
        this.mSteps_hm = steps;
    }

    public HashMap<String, String> getLegs() {
        return mLegs_hm;
    }

    public void setLegs(HashMap<String, String> legs) {
        this.mLegs_hm = legs;
    }

    @Override
    public int getCount() {
        return mSteps_hm.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final ViewHolder viewHolder;
        if (view == null) {
            myInflater = LayoutInflater.from(mContext);
            view = myInflater.inflate(R.layout.step_listitem, null);
            viewHolder = new ViewHolder();
            viewHolder._step_tv = (TextView) view.findViewById(R.id.step_tv);
            viewHolder._arrow_iv = (ImageView) view.findViewById(R.id.arrows_iv);
            viewHolder._mile_tv = (TextView) view.findViewById(R.id.mile_tv);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder._step_tv.setText(Html.fromHtml(mSteps_hm.get(i).get("html_instructions")));
        viewHolder._mile_tv.setText(mSteps_hm.get(i).get("distance_text"));
        handleArrowLevel(mSteps_hm.get(i).get("maneuver"),viewHolder._arrow_iv);

        return view;
    }

    private void handleArrowLevel(String s, ImageView iv) {
        if (s.equals("turn-left")) {
            iv.setImageLevel(Constants.MANEUVER_LF);
        } else if (s.equals("turn-right")) {
            iv.setImageLevel(Constants.MANEUVER_RF);
        } else if (s.equals("turn-slight-right") ||s.equals("keep-right")) {
            iv.setImageLevel(Constants.MANEUVER_TSR);
        } else if (s.equals("turn-slight-left") || s.equals("keep-left")) {
            iv.setImageLevel(Constants.MANEUVER_TSL);
        } else if (s.equals("fork-right")) {
            iv.setImageLevel(Constants.MANEUVER_FR);
        } else if (s.equals("fork-left")) {
            iv.setImageLevel(Constants.MANEUVER_FL);
        } else if (s.equals("straight")) {
            iv.setImageLevel(Constants.MANEUVER_S);
        } else if (s.equals("uturn-left")) {
            iv.setImageLevel(Constants.MANEUVER_UL);
        } else if (s.equals("roundabout-right")) {
            iv.setImageLevel(Constants.MANEUVER_RR);
        } else if (/*isContains(s, R.string.roundabout_out)*/false) {
            iv.setImageLevel(Constants.MANEUVER_RO);
        } else if (s.equals("turn-sharp-left")) {
            iv.setImageLevel(Constants.MANEUVER_TSHL);
        } else if (s.equals("turn-sharp-right")) {
            iv.setImageLevel(Constants.MANEUVER_TSHR);
        } else if (s.equals("merge")) {
            iv.setImageLevel(Constants.MANEUVER_M);
        } else if(s.equals("ramp-right")){
            iv.setImageLevel(Constants.MANEUVER_RAMP_RIGHT);
        } else{
            iv.setImageLevel(Constants.MANEUVER_UNKNOW);
        }
    }

    private class ViewHolder{
        TextView _step_tv;
        ImageView _arrow_iv;
        TextView _mile_tv;
    }
}
