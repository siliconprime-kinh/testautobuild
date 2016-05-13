package com.dropininc.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.dropininc.R;
import com.dropininc.adapter.click_listener.BaseItemClickListener;
import com.dropininc.adapter.view_holder.BaseViewHolder;
import com.dropininc.adapter.view_holder.InboxViewHolder;
import com.dropininc.adapter.view_holder.LoadingViewHolder;
import com.dropininc.model.MessagesModel;

import java.util.List;

/**
 * Created on 31.03.16.
 */
public class MessagesAdapter extends BaseRecyclerViewAdapter<MessagesModel> {

    public static final int ITEM_TYPE_NORMAL = 0;
    public static final int ITEM_TYPE_LOADING = 1;

    private BaseItemClickListener clickListener;

    public MessagesAdapter(Context context, List<MessagesModel> data, BaseItemClickListener clickListener) {
        super(context, data);
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) == null ? ITEM_TYPE_LOADING : ITEM_TYPE_NORMAL;
    }

    @Override
    public BaseViewHolder<MessagesModel> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == ITEM_TYPE_NORMAL) {
            view = layoutInflater.inflate(R.layout.view_row_messages, parent, false);
            return new InboxViewHolder(view, clickListener);
        }
        if (viewType == ITEM_TYPE_LOADING) {
            view = layoutInflater.inflate(R.layout.view_row_loading, parent, false);
            return new LoadingViewHolder<>(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<MessagesModel> holder, int position) {
        if (holder instanceof InboxViewHolder) {
            MessagesModel messagesModel = data.get(position);
            holder.bind(messagesModel);
        }
    }

    public MessagesModel getLastItem() {
        return data.get(data.size() - 1);
    }

    public void showLoadingItem() {
        if (getLastItem() != null)
            addItem(null);
    }

    public void hideLoadingItem() {
        if (data == null || data.isEmpty())
            return;
        if (getLastItem() == null)
            removeItem(data.size() - 1);
    }
}
