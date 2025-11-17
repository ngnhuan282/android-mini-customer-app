package com.example.btqt02;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CustomerAdapter extends ArrayAdapter<Customer> {
    public CustomerAdapter(Context context, ArrayList<Customer> customers) {
        super(context, 0, customers);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.layout_item, parent, false);
        }

        Customer customer = getItem(position);

        TextView txtPhone = convertView.findViewById(R.id.txtPhone);
        TextView txtPoint = convertView.findViewById(R.id.txtPoint);
        TextView txtCreatedAt = convertView.findViewById(R.id.txtCreatedAt);
        TextView txtUpdatedAt = convertView.findViewById(R.id.txtUpdatedAt);
        TextView txtNote = convertView.findViewById(R.id.txtNote);

        txtPhone.setText(customer.getPhone().toString());
        txtPoint.setText(String.valueOf(customer.getPoint()));
        txtCreatedAt.setText(customer.getCreatedAt());
        txtUpdatedAt.setText(customer.getUpdatedAt());
        txtNote.setText(customer.getNote().toString());

        return convertView;
    }
}
