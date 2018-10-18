package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListItemColorAdapter extends BaseAdapter {
    //region Variables
    private Context context;
    private String[] textValues;
    private ArrayList<String[]> colorValues;
    //endregion

    public ListItemColorAdapter(Context context) {
        super();

        this.context = context;
        init();
    }

    private void init() {
        ;    //  NOP
    }

    public void setTextItems(String[] textValues) {
        this.textValues = textValues;
    }

    public void setColorItems(ArrayList<String[]> colorValues) {
        this.colorValues = colorValues;
    }

    @Override
    public int getCount() {
        if (textValues == null) {
            return 0;
        } else {
            return textValues.length;
        }
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ListItemColorViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.listitemcolor, null);
            viewHolder = buildViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ListItemColorViewHolder) convertView.getTag();
        }
        paintView(convertView, position);
        return convertView;
    }

    public void paintView(View view, int index) {    //  Décoration proprement dite du getView
        ListItemColorViewHolder viewHolder = (ListItemColorViewHolder) view.getTag();
        viewHolder.colorWheelView.disableMarker();
        viewHolder.colorWheelView.setColors(colorValues.get(index));
        viewHolder.textView.setText(textValues[index]);
    }

    private ListItemColorViewHolder buildViewHolder(View convertView) {
        ListItemColorViewHolder viewHolder = new ListItemColorViewHolder();
        viewHolder.colorWheelView = (ColorWheelView) convertView.findViewById(R.id.COLORS_VIEW);
        viewHolder.textView = (TextView) convertView.findViewById(R.id.TV_TXT);
        return viewHolder;
    }

}
