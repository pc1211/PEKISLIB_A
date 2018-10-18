package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListItemNormalAdapter extends ArrayAdapter<String> {
    //region Variables
    private final Context context;
    private final String[] values;
    //endregion

    public ListItemNormalAdapter(Context context, String[] values) {
        super(context, R.layout.listitemnormal, values);

        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.listitemnormal, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.text1);
        textView.setText(values[position]);
        return rowView;
    }
}
