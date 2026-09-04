package com.four_zreo.soulrepair.paul.arian.fileselector;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.four_zreo.soulrepair.R;

/* JADX INFO: loaded from: classes.dex */
public class CustomList extends ArrayAdapter<String> {
    private final Activity context;
    private final Integer imageId;
    private final String[] web;

    public CustomList(Activity context, String[] web, Integer imageId) {
        super(context, R.layout.list_single, web);
        this.context = context;
        this.web = web;
        this.imageId = imageId;
    }

    @Override // android.widget.ArrayAdapter, android.widget.Adapter
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = this.context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_single, (ViewGroup) null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
        txtTitle.setText(this.web[position]);
        imageView.setImageResource(this.imageId.intValue());
        return rowView;
    }
}
