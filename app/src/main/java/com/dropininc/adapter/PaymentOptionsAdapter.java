package com.dropininc.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.dropininc.R;
import com.dropininc.adapter.click_listener.BaseItemClickListener;
import com.dropininc.adapter.view_holder.BaseViewHolder;
import com.dropininc.adapter.view_holder.PaymentViewHolder;
import com.dropininc.model.PaymentModel;

import java.util.List;

/**
 * Created on 04.04.16.
 */
public class PaymentOptionsAdapter extends BaseRecyclerViewAdapter<PaymentModel> {

    private BaseItemClickListener clickListener;

    public PaymentOptionsAdapter(Context context, List<PaymentModel> data, BaseItemClickListener clickListener) {
        super(context, data);
        this.clickListener = clickListener;
    }

    @Override
    public BaseViewHolder<PaymentModel> onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.view_row_payment, parent, false);
        return new PaymentViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<PaymentModel> holder, int position) {
        PaymentModel model = data.get(position);
        holder.bind(model);
    }

    public void setDefaultCard(int positionToSwap) {
        data.get(0).isDefault = false;
        PaymentModel model = data.get(positionToSwap);
        model.isDefault = true;
        data.remove(positionToSwap);
        data.add(0, model);
        notifyDataSetChanged();
    }
}
