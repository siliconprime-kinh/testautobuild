package com.dropininc.adapter.view_holder;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.adapter.click_listener.BaseItemClickListener;
import com.dropininc.model.MessagesModel;

import butterknife.Bind;

/**
 * Created on 31.03.16.
 */
public class InboxViewHolder extends BaseViewHolder<MessagesModel> {

    @Bind(R.id.txtTitle)
    protected TextView title;
    @Bind(R.id.iconArrow)
    protected ImageView iconArrow;


    public InboxViewHolder(View view, BaseItemClickListener itemClickListener) {
        super(view);
        if (itemClickListener != null)
            view.setOnClickListener(v -> itemClickListener.onItemClick(getAdapterPosition()));
    }

    @Override
    public void bind(MessagesModel messagesModel) {
        title.setText(messagesModel.title);
        if (messagesModel.status.equalsIgnoreCase("received")) {
            title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_gray));
            iconArrow.setBackgroundResource(R.drawable.ic_arrow_left_grey);
        } else {
            title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_green));
            iconArrow.setBackgroundResource(R.drawable.ic_arrow_left_green);
        }
    }
}
