package com.commonsware.cwac.merge;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class MergeSpinnerAdapter extends MergeAdapter {
    @Override // android.widget.BaseAdapter, android.widget.SpinnerAdapter
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        for (ListAdapter piece : getPieces()) {
            int size = piece.getCount();
            if (position < size) {
                return ((SpinnerAdapter) piece).getDropDownView(position, convertView, parent);
            }
            position -= size;
        }
        return null;
    }

    @Override // com.commonsware.cwac.merge.MergeAdapter
    public void addView(View view) {
        throw new RuntimeException("Not supported with MergeSpinnerAdapter");
    }

    @Override // com.commonsware.cwac.merge.MergeAdapter
    public void addView(View view, boolean enabled) {
        throw new RuntimeException("Not supported with MergeSpinnerAdapter");
    }

    @Override // com.commonsware.cwac.merge.MergeAdapter
    public void addViews(List<View> views) {
        throw new RuntimeException("Not supported with MergeSpinnerAdapter");
    }

    @Override // com.commonsware.cwac.merge.MergeAdapter
    public void addViews(List<View> views, boolean enabled) {
        throw new RuntimeException("Not supported with MergeSpinnerAdapter");
    }
}
