package com.example.newas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.newas.DistressCall;
import com.example.newas.R;

import java.util.List;

public class DistressCallAdapter extends ArrayAdapter<DistressCall> {

    private Context context;
    private List<DistressCall> distressCallList;

    public DistressCallAdapter(Context context, List<DistressCall> distressCallList) {
        super(context, 0, distressCallList);
        this.context = context;
        this.distressCallList = distressCallList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_distress_call, parent, false);
        }

        DistressCall distressCall = distressCallList.get(position);

        TextView callerTextView = convertView.findViewById(R.id.caller);
        TextView destinationTextView = convertView.findViewById(R.id.destination);

        callerTextView.setText(distressCall.getCallerFirstName() + " " + distressCall.getCallerLastName());
        destinationTextView.setText(distressCall.getDestination());

        return convertView;
    }
}
