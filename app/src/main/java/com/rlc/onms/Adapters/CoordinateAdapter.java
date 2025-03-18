package com.rlc.onms.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.rlc.onms.Entities.Coordinate;
import com.rlc.onms.R;

import java.util.List;

public class CoordinateAdapter extends ArrayAdapter<Coordinate> {
    public CoordinateAdapter(Context context, List<Coordinate> coordinates) {
        super(context, 0, coordinates);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Coordinate coordinate = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_coordinate, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.text1);
        textView.setText(coordinate.toString());

        RadioButton radioButton = convertView.findViewById(R.id.radio_button);
        ListView listView = (ListView) parent;


        final View finalConvertView = convertView;
        final int finalPosition = position;

        radioButton.setOnClickListener(v -> listView.performItemClick(finalConvertView, finalPosition, getItemId(finalPosition)));

        radioButton.setChecked(listView.getCheckedItemPosition() == position);

        return convertView;
    }
}




