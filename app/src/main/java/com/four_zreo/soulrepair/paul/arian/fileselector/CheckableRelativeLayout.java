package com.four_zreo.soulrepair.paul.arian.fileselector;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
    private List<Checkable> checkableViews;
    private boolean isChecked;

    public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialise(attrs);
    }

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(attrs);
    }

    public CheckableRelativeLayout(Context context, int checkableId) {
        super(context);
        initialise(null);
    }

    @Override // android.widget.Checkable
    public boolean isChecked() {
        return this.isChecked;
    }

    @Override // android.widget.Checkable
    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        for (Checkable c : this.checkableViews) {
            c.setChecked(isChecked);
        }
    }

    @Override // android.widget.Checkable
    public void toggle() {
        this.isChecked = !this.isChecked;
        for (Checkable c : this.checkableViews) {
            c.toggle();
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            findCheckableChildren(getChildAt(i));
        }
    }

    private void initialise(AttributeSet attrs) {
        this.isChecked = false;
        this.checkableViews = new ArrayList(5);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void findCheckableChildren(View view) {
        if (view instanceof Checkable) {
            this.checkableViews.add((Checkable) view);
        }
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; i++) {
                findCheckableChildren(vg.getChildAt(i));
            }
        }
    }
}
