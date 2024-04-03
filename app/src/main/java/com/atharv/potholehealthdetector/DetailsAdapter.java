package com.atharv.potholehealthdetector;

import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.TextView;

        import java.util.ArrayList;

public class DetailsAdapter extends ArrayAdapter<String> {
    private ArrayList<String> statusList;
    private ArrayList<String> valueList;
    private Context context;

    public DetailsAdapter(Context context, ArrayList<String> statusList, ArrayList<String> valueList) {
        super(context, 0, statusList);
        this.context = context;
        this.statusList = statusList;
        this.valueList = valueList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data items for this position
        String status = statusList.get(position);
        String value = valueList.get(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_status, parent, false);
        }

        // Lookup views for data population
        TextView statusTextView = convertView.findViewById(R.id.statusTextView);
        TextView valueTextView = convertView.findViewById(R.id.valueTextView);

        // Populate the data into the template views using the data objects
        statusTextView.setText(status + ": ");
        valueTextView.setText(value);

        // Return the completed view to render on screen
        return convertView;
    }
}

