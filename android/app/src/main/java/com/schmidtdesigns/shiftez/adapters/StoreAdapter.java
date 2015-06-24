package com.schmidtdesigns.shiftez.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.schmidtdesigns.shiftez.R;
import com.schmidtdesigns.shiftez.models.Store;

import java.util.List;

/**
 * Created by braden on 15-06-24.
 */
public class StoreAdapter extends ArrayAdapter<Store> {

    private int mResource;
    private List<Store> stores;
    private Context mContext;
    private int mDropdownResource;

    public StoreAdapter(Context context, int resource, int dropdownResource, List<Store> stores) {
        super(context, resource, stores);
        this.mResource = resource;
        this.stores = stores;
        this.mContext = context;
        this.mDropdownResource = dropdownResource;
    }

    @Override
    public int getCount() {
        return stores.size();
    }

    @Override
    public Store getItem(int position) {
        return stores.get(position);
    }

    @Override
    public long getItemId(int position) {
        //TODO
        return super.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        }

        TextView storeName = (TextView) convertView.findViewById(R.id.spinner_item_text);

        storeName.setText(stores.get(position).getName());

        return storeName;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mDropdownResource, parent, false);
        }

        TextView storeName = (TextView) convertView.findViewById(R.id.spinner_dropdown_item_text);
        storeName.setText(stores.get(position).getName());

        return storeName;
    }


}
