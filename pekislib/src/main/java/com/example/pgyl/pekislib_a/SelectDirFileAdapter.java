/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author pgyl
 */
public class SelectDirFileAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private final int[] colors;

    public SelectDirFileAdapter(Context context, String[] values, int[] colors) {
        super(context, R.layout.selectdirfilelistitem, values);
        this.context = context;
        this.values = values;
        this.colors = colors;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.selectdirfilelistitem, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.text1);
        textView.setText(values[position]);
        textView.setTextColor(colors[position]);

        return rowView;
    }
}
