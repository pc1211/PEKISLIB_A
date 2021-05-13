package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class TextListViewAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> textListViewLines;

    public TextListViewAdapter(Context context) {
        super();

        this.context = context;
        init();
    }

    private void init() {
        textListViewLines = null;
    }

    public void close() {
        removeAllItems();
        textListViewLines = null;
        context = null;
    }

    public void setItems(ArrayList<String> textListViewLines) {
        this.textListViewLines = textListViewLines;
    }

    public void removeAllItems() {
        if (textListViewLines != null) textListViewLines.clear();
    }

    @Override
    public int getCount() {
        return (textListViewLines != null) ? textListViewLines.size() : 0;
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
    public View getView(final int position, View rowView, ViewGroup parent) {   //  ViewHolder pattern
        TextListViewViewHolder viewHolder;

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.textlistviewitem, null);
            viewHolder = buildViewHolder(rowView);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (TextListViewViewHolder) rowView.getTag();
        }
        //  setupViewHolder(viewHolder, position);
        paintView(viewHolder, position);
        return rowView;
    }

    public void paintView(TextListViewViewHolder viewHolder, int position) {    //  Décoration proprement dite du getView
        viewHolder.textView.setText(textListViewLines.get(position));
    }

    private TextListViewViewHolder buildViewHolder(View rowView) {
        TextListViewViewHolder viewHolder = new TextListViewViewHolder();

        viewHolder.textView = rowView.findViewById(R.id.TV_TEXT_VIEW);
        return viewHolder;
    }

    private void setupViewHolder(TextListViewViewHolder viewHolder, int position) {
        //  RIEN
    }
}
