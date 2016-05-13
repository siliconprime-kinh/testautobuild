package com.dropininc.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.dropininc.adapter.view_holder.BaseViewHolder;

import java.util.List;

/**
 * Created on 31.03.16.
 */
public abstract class BaseRecyclerViewAdapter<Model> extends RecyclerView.Adapter<BaseViewHolder<Model>> {

    protected Context context;
    protected List<Model> data;
    protected LayoutInflater layoutInflater;

    public BaseRecyclerViewAdapter(Context context, List<Model> data) {
        this.context = context;
        this.data = data;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Nullable
    public Model getItem(int position) {
        if (position >= 0 && position < data.size()) {
            return data.get(position);
        }
        return null;
    }


    public void addItem(Model item) {
        if (data.add(item)) {
            notifyItemInserted(data.size() - 1);
        }
    }

    public void addItem(Model item, int position) {
        if (position >= 0 && position < data.size()) {
            data.add(position, item);
            notifyItemInserted(position);
        }
    }

    public void addItems(List<Model> items) {
        if (items != null && !items.isEmpty()) {
            if (data.addAll(items)) {
                notifyItemRangeInserted(data.size() - items.size(), data.size() - 1);
            }
        }
    }

    public void addItems(List<Model> items, int position) {
        if (position >= 0 && position < data.size() && items != null && !items.isEmpty()) {
            if (data.addAll(position, items)) {
                notifyItemRangeInserted(position, data.size() - 1);
            }
        }
    }

    /**
     * Remove provided item from the adapter
     * WARNING!!! This method may be slow, calc difficulty - O(n)
     *
     * @param item Item to remove
     */
    public void removeItem(Model item) {
        int position = data.indexOf(item);
        removeItem(position);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < data.size()) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
