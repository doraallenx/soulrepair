package com.commonsware.cwac.merge;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.SectionIndexer;
import com.commonsware.cwac.sacklist.SackOfViewsAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class MergeAdapter extends BaseAdapter implements SectionIndexer {
    protected PieceStateRoster pieces = new PieceStateRoster(null);

    public void addAdapter(ListAdapter adapter) {
        this.pieces.add(adapter);
        adapter.registerDataSetObserver(new CascadeDataSetObserver(this, null));
    }

    public void addView(View view) {
        addView(view, false);
    }

    public void addView(View view, boolean enabled) {
        ArrayList<View> list = new ArrayList<>(1);
        list.add(view);
        addViews(list, enabled);
    }

    public void addViews(List<View> views) {
        addViews(views, false);
    }

    public void addViews(List<View> views, boolean enabled) {
        if (enabled) {
            addAdapter(new EnabledSackAdapter(views));
        } else {
            addAdapter(new SackOfViewsAdapter(views));
        }
    }

    @Override // android.widget.Adapter
    public Object getItem(int position) {
        for (ListAdapter piece : getPieces()) {
            int size = piece.getCount();
            if (position < size) {
                return piece.getItem(position);
            }
            position -= size;
        }
        return null;
    }

    public ListAdapter getAdapter(int position) {
        for (ListAdapter piece : getPieces()) {
            int size = piece.getCount();
            if (position < size) {
                return piece;
            }
            position -= size;
        }
        return null;
    }

    @Override // android.widget.Adapter
    public int getCount() {
        int total = 0;
        for (ListAdapter piece : getPieces()) {
            total += piece.getCount();
        }
        return total;
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public int getViewTypeCount() {
        int total = 0;
        for (PieceState piece : this.pieces.getRawPieces()) {
            total += piece.adapter.getViewTypeCount();
        }
        return Math.max(total, 1);
    }

    @Override // android.widget.BaseAdapter, android.widget.Adapter
    public int getItemViewType(int position) {
        int typeOffset = 0;
        for (PieceState piece : this.pieces.getRawPieces()) {
            if (piece.isActive) {
                int size = piece.adapter.getCount();
                if (position < size) {
                    int result = typeOffset + piece.adapter.getItemViewType(position);
                    return result;
                }
                position -= size;
            }
            typeOffset += piece.adapter.getViewTypeCount();
        }
        return -1;
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override // android.widget.BaseAdapter, android.widget.ListAdapter
    public boolean isEnabled(int position) {
        for (ListAdapter piece : getPieces()) {
            int size = piece.getCount();
            if (position < size) {
                return piece.isEnabled(position);
            }
            position -= size;
        }
        return false;
    }

    @Override // android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        for (ListAdapter piece : getPieces()) {
            int size = piece.getCount();
            if (position < size) {
                return piece.getView(position, convertView, parent);
            }
            position -= size;
        }
        return null;
    }

    @Override // android.widget.Adapter
    public long getItemId(int position) {
        for (ListAdapter piece : getPieces()) {
            int size = piece.getCount();
            if (position < size) {
                return piece.getItemId(position);
            }
            position -= size;
        }
        return -1L;
    }

    @Override // android.widget.SectionIndexer
    public int getPositionForSection(int section) {
        int position = 0;
        for (ListAdapter piece : getPieces()) {
            if (piece instanceof SectionIndexer) {
                Object[] sections = ((SectionIndexer) piece).getSections();
                int numSections = 0;
                if (sections != null) {
                    numSections = sections.length;
                }
                if (section < numSections) {
                    return ((SectionIndexer) piece).getPositionForSection(section) + position;
                }
                if (sections != null) {
                    section -= numSections;
                }
            }
            position += piece.getCount();
        }
        return 0;
    }

    @Override // android.widget.SectionIndexer
    public int getSectionForPosition(int position) {
        Object[] sections;
        int section = 0;
        for (ListAdapter piece : getPieces()) {
            int size = piece.getCount();
            if (position < size) {
                if (piece instanceof SectionIndexer) {
                    return ((SectionIndexer) piece).getSectionForPosition(position) + section;
                }
                return 0;
            }
            if ((piece instanceof SectionIndexer) && (sections = ((SectionIndexer) piece).getSections()) != null) {
                section += sections.length;
            }
            position -= size;
        }
        return 0;
    }

    @Override // android.widget.SectionIndexer
    public Object[] getSections() {
        Object[] curSections;
        ArrayList<Object> sections = new ArrayList<>();
        for (ListAdapter piece : getPieces()) {
            if ((piece instanceof SectionIndexer) && (curSections = ((SectionIndexer) piece).getSections()) != null) {
                Collections.addAll(sections, curSections);
            }
        }
        if (sections.size() == 0) {
            return new String[0];
        }
        return sections.toArray(new Object[0]);
    }

    public void setActive(ListAdapter adapter, boolean isActive) {
        this.pieces.setActive(adapter, isActive);
        notifyDataSetChanged();
    }

    public void setActive(View v, boolean isActive) {
        this.pieces.setActive(v, isActive);
        notifyDataSetChanged();
    }

    protected List<ListAdapter> getPieces() {
        return this.pieces.getPieces();
    }

    private static class PieceState {
        ListAdapter adapter;
        boolean isActive;

        PieceState(ListAdapter adapter, boolean isActive) {
            this.isActive = true;
            this.adapter = adapter;
            this.isActive = isActive;
        }
    }

    private static class PieceStateRoster {
        protected ArrayList<ListAdapter> active;
        protected ArrayList<PieceState> pieces;

        private PieceStateRoster() {
            this.pieces = new ArrayList<>();
            this.active = null;
        }

        /* synthetic */ PieceStateRoster(PieceStateRoster pieceStateRoster) {
            this();
        }

        void add(ListAdapter adapter) {
            this.pieces.add(new PieceState(adapter, true));
        }

        void setActive(ListAdapter adapter, boolean isActive) {
            for (PieceState state : this.pieces) {
                if (state.adapter == adapter) {
                    state.isActive = isActive;
                    this.active = null;
                    return;
                }
            }
        }

        void setActive(View v, boolean isActive) {
            for (PieceState state : this.pieces) {
                if ((state.adapter instanceof SackOfViewsAdapter) && ((SackOfViewsAdapter) state.adapter).hasView(v)) {
                    state.isActive = isActive;
                    this.active = null;
                    return;
                }
            }
        }

        List<PieceState> getRawPieces() {
            return this.pieces;
        }

        List<ListAdapter> getPieces() {
            if (this.active == null) {
                this.active = new ArrayList<>();
                for (PieceState state : this.pieces) {
                    if (state.isActive) {
                        this.active.add(state.adapter);
                    }
                }
            }
            return this.active;
        }
    }

    private static class EnabledSackAdapter extends SackOfViewsAdapter {
        public EnabledSackAdapter(List<View> views) {
            super(views);
        }

        public boolean areAllItemsEnabled() {
            return true;
        }

        public boolean isEnabled(int position) {
            return true;
        }
    }

    private class CascadeDataSetObserver extends DataSetObserver {
        private CascadeDataSetObserver() {
        }

        /* synthetic */ CascadeDataSetObserver(MergeAdapter mergeAdapter, CascadeDataSetObserver cascadeDataSetObserver) {
            this();
        }

        @Override // android.database.DataSetObserver
        public void onChanged() {
            MergeAdapter.this.notifyDataSetChanged();
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            MergeAdapter.this.notifyDataSetInvalidated();
        }
    }
}
