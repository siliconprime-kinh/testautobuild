package com.dropininc.adapter.view_holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created on 31.03.16.
 */
public abstract class BaseViewHolder<Model> extends RecyclerView.ViewHolder{

    public BaseViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    /**
     * Used to bind concrete model to the view holder
     * @param model
     */
    public abstract void bind(Model model);
}
