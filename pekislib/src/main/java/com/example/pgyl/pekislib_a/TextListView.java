package com.example.pgyl.pekislib_a;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import java.util.ArrayList;

public class TextListView extends ListView {
    Context context;
    TextListViewAdapter textListViewAdapter;

    public TextListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
    }

    public void init() {
        textListViewAdapter = new TextListViewAdapter(context);
        setAdapter(textListViewAdapter);
        setFastScrollEnabled(true);
        setFastScrollAlwaysVisible(true);
    }

    public void close() {
        textListViewAdapter.close();
        textListViewAdapter = null;
    }

    public void setItems(ArrayList<String> textListViewLines) {
        textListViewAdapter.setItems(textListViewLines);
        textListViewAdapter.notifyDataSetChanged();
    }

    public void removeAllItems() {
        textListViewAdapter.removeAllItems();
        textListViewAdapter.notifyDataSetChanged();
    }

}
