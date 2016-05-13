package com.dropininc.adapter.view_holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.adapter.click_listener.BaseItemClickListener;
import com.dropininc.model.PaymentModel;

import butterknife.Bind;

/**
 * Created on 04.04.16.
 */
public class PaymentViewHolder extends BaseViewHolder<PaymentModel> {

    @Bind(R.id.cardTypeImage)
    protected ImageView cardTypeImage;
    @Bind(R.id.cardLastNumbers)
    protected TextView cardLastNumbers;
    @Bind(R.id.isCardDefaultImage)
    protected ImageView isCardDefaultImage;

    public PaymentViewHolder(View view, BaseItemClickListener clickListener) {
        super(view);
        if (clickListener != null)
            view.setOnClickListener(v -> clickListener.onItemClick(getAdapterPosition()));
    }

    @Override
    public void bind(PaymentModel paymentModel) {
        switch (paymentModel.brand) {
            case "Visa":
                cardTypeImage.setImageResource(R.drawable.pk_card_visa);
                break;
            case "American Express":
                cardTypeImage.setImageResource(R.drawable.pk_card_amex);
                break;
            case "MasterCard":
                cardTypeImage.setImageResource(R.drawable.pk_card_master);
                break;
            case "Discover":
                cardTypeImage.setImageResource(R.drawable.pk_card_discover);
                break;
            default:
                cardTypeImage.setImageResource(R.drawable.pk_default_card);
                break;
        }

        cardLastNumbers.setText(String.format(" \u2022\u2022\u2022\u2022 %s", paymentModel.last4));
        if (paymentModel.isDefault) {
            isCardDefaultImage.setVisibility(View.VISIBLE);
        } else {
            isCardDefaultImage.setVisibility(View.GONE);
        }
    }
}
