package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ListItemSimpleAdapter extends BaseAdapter {
    //region Variables
    private Context context;
    private String[] textValues;
    //endregion

    public ListItemSimpleAdapter(Context context) {
        super();

        this.context = context;
        init();
    }

    private void init() {//  NOP
    }

    public void setTextItems(String[] textValues) {
        this.textValues = textValues;
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
        ListItemSimpleViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.listitemsimpple, null);
            viewHolder = buildViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ListItemSimpleViewHolder) convertView.getTag();
        }
        paintView(convertView, position);
        return convertView;
    }

    public void paintView(View view, int index) {    //  Décoration proprement dite du getView
        ListItemSimpleViewHolder viewHolder = (ListItemSimpleViewHolder) view.getTag();
        viewHolder.textView.setText(textValues[index]);
    }

    private ListItemSimpleViewHolder buildViewHolder(View convertView) {
        ListItemSimpleViewHolder viewHolder = new ListItemSimpleViewHolder();
        viewHolder.textView = convertView.findViewById(R.id.TV_TXT);
        return viewHolder;
    }

}