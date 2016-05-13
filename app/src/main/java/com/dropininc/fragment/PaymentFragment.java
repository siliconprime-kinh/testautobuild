package com.dropininc.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.Constants;
import com.dropininc.R;
import com.dropininc.activity.AddCardActivity;
import com.dropininc.activity.MainActivity;
import com.dropininc.adapter.PaymentOptionsAdapter;
import com.dropininc.adapter.click_listener.BaseItemClickListener;
import com.dropininc.customview.RecyclerViewEmptySupport;
import com.dropininc.dialog.AlertDialog;
import com.dropininc.dialog.ConfirmDialog;
import com.dropininc.interfaces.PaymentCode;
import com.dropininc.interfaces.RequestCode;
import com.dropininc.model.PaymentModel;
import com.dropininc.utils.Logs;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;


public class PaymentFragment extends BaseFragment implements BaseItemClickListener {
    @Bind(R.id.paymentRecyclerView)
    protected RecyclerViewEmptySupport paymentRecyclerView;
    @Bind(R.id.emptyView)
    protected TextView emptyView;
    private PaymentOptionsAdapter paymentOptionsAdapter;

    private Action1<Throwable> errorAction = throwable -> {
        AppApplication.getInstance().logErrorServer("PaymentFragment", networkManager.parseError(throwable));
        showErrorDialog(networkManager.parseError(throwable).message, "", false);
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_payment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        paymentRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        paymentRecyclerView.setEmptyView(emptyView);
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //try to remove a card
                int swipePosition = viewHolder.getAdapterPosition();
                if (!((MainActivity) mContext).isEnroute()) {
                    showRemoveCardDialog(swipePosition);
                } else {
                    paymentOptionsAdapter.notifyItemChanged(swipePosition);
                }
            }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(simpleCallback);
        touchHelper.attachToRecyclerView(paymentRecyclerView);
        getPaymentOptions();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == RequestCode.ADD_NEW_CARD_REQUEST) {
            String cardNumber = data.getStringExtra(AddCardActivity.CARD_NUMBER_EXTRA);
            int cardExpMonth = data.getIntExtra(AddCardActivity.CARD_MONTH_EXTRA, 0);
            int cardExpYear = data.getIntExtra(AddCardActivity.CARD_YEAR_EXTRA, 0);
            String cardCVC = data.getStringExtra(AddCardActivity.CARD_CVV_EXTRA);
            String cardType = data.getStringExtra(AddCardActivity.CARD_TYPE);

            addNewCard(cardNumber, cardExpMonth, cardExpYear, cardCVC, cardType);
        }
    }

    private void showPayments(List<PaymentModel> payments) {
        paymentOptionsAdapter = new PaymentOptionsAdapter(getActivity(), payments, this);
        paymentRecyclerView.setAdapter(paymentOptionsAdapter);
    }

    private void addNewCard(String cardNumber, int cardExpMonth, int cardExpYear, String cardCVC, String cardType) {
        showProgressDialog(getString(R.string.processing), null);

        Card card = new Card(cardNumber, cardExpMonth, cardExpYear, cardCVC);
        try {
            Stripe stripe = new Stripe(Constants.STRIPE_PUBLISHABLE_KEY);
            stripe.createToken(card, new TokenCallback() {
                @Override
                public void onError(Exception error) {
                    error.printStackTrace();
                    showAlertDialog(getString(R.string.card_adding_error_message));
                    hideProgressDialog();
                }

                @Override
                public void onSuccess(Token token) {
                    Logs.log(TAG, "New card token " + token);
                    performAddCard(new PaymentModel(null, cardType, token.getCard().getLast4(), false), token.getId());
                }
            });
        } catch (AuthenticationException e) {
            hideProgressDialog();
            e.printStackTrace();
        }
    }

    private void showErrorDialog(String title, String message, final boolean isNeedExit) {
        final AlertDialog mDialog = new AlertDialog(getActivity());
        mDialog.setTitleDialog(title);
        mDialog.setMessageDialog(message);
        mDialog.setButtonClick(view -> {
            mDialog.dismiss();
            if (isNeedExit) onBackPressed();
        });
        mDialog.show();
    }

    private void showSetDefaultCardDialog(final String id, int position) {
        final ConfirmDialog mDialog = new ConfirmDialog(getActivity());
        mDialog.setTitleDialog("");
        mDialog.setMessageDialog(R.string.message_make_this_your_default_payment);
        mDialog.setOkTitleButton(R.string.yes);
        mDialog.setCancelTitleButton(R.string.no);
        mDialog.setOkButtonClick(v -> {
            performDefaultCardSelection(id, position);
            mDialog.dismiss();
        });
        mDialog.setCancelButtonClick(v -> mDialog.dismiss());
        mDialog.show();
    }

    private void showRemoveCardDialog(int position) {
        final ConfirmDialog mDialog = new ConfirmDialog(getActivity());
        mDialog.setTitleDialog("");
        mDialog.setCancelableDialog(false);
        mDialog.setMessageDialog(R.string.message_you_want_to_remove_this_card);
        mDialog.setOkTitleButton(R.string.yes);
        mDialog.setCancelTitleButton(R.string.no);
        mDialog.setOkButtonClick(v -> {
            PaymentModel model = paymentOptionsAdapter.getItem(position);
            if (model != null) {
                performRemoveCard(model.id, position, model.isDefault);
            } else {
                paymentOptionsAdapter.notifyItemChanged(position);
            }
            mDialog.dismiss();
        });
        mDialog.setCancelButtonClick(v -> {
            paymentOptionsAdapter.notifyItemChanged(position);
            mDialog.dismiss();
        });
        mDialog.show();
    }

    @OnClick(R.id.addCreditCardButton)
    protected void showAddNewCardActivity() {
        Intent intent = new Intent(getActivity(), AddCardActivity.class);
        getActivity().startActivityForResult(intent, RequestCode.ADD_NEW_CARD_REQUEST);
    }

    // Make requests to the server
    private void getPaymentOptions() {
        showProgressDialog(getString(R.string.processing), null);

        networkManager.getPaymentOptions()
                .doAfterTerminate(this::hideProgressDialog)
                .filter(paymentStatusModel -> paymentStatusModel != null
                        && paymentStatusModel.code.equalsIgnoreCase(PaymentCode.CARD_OK))
                .map(paymentStatusModel -> paymentStatusModel.payments)
                .subscribe(this::showPayments, errorAction);
    }

    private void performAddCard(PaymentModel paymentModel, String id) {
        showProgressDialog(getString(R.string.processing), null);
        networkManager.addCreditCard(id)
                .doAfterTerminate(this::hideProgressDialog)
                .subscribe(newCardModel -> {
                    paymentModel.id = newCardModel.cardId;
                    if (paymentOptionsAdapter == null) {
                        paymentModel.isDefault = true;
                        paymentOptionsAdapter = new PaymentOptionsAdapter(getActivity(), new ArrayList<>(Collections.singletonList(paymentModel)), this);
                        paymentRecyclerView.setAdapter(paymentOptionsAdapter);
                        onBackPressed();
                    } else {
                        paymentOptionsAdapter.addItem(paymentModel);
                    }
                }, errorAction);
    }

    private void performRemoveCard(String cardId, int position, boolean isDefault) {
        showProgressDialog(getString(R.string.processing), null);
        if (isDefault) {
            networkManager.removeCreditCard(cardId)
                    .flatMap(ignore -> {
                        paymentOptionsAdapter.removeItem(position);
                        return networkManager.getPaymentOptions();
                    })
                    .filter(paymentStatusModel -> paymentStatusModel != null
                            && paymentStatusModel.code.equalsIgnoreCase(PaymentCode.CARD_OK))
                    .map(paymentStatusModel -> paymentStatusModel.payments)
                    .doAfterTerminate(this::hideProgressDialog)
                    .subscribe(this::showPayments, errorAction);
        } else {
            networkManager.removeCreditCard(cardId)
                    .doAfterTerminate(this::hideProgressDialog)
                    .subscribe(newDefaultId -> paymentOptionsAdapter.removeItem(position), errorAction);
        }
    }

    private void performDefaultCardSelection(String cardId, int position) {
        showProgressDialog(getString(R.string.processing), null);
        networkManager.setDefaultCreditCard(cardId)
                .doAfterTerminate(this::hideProgressDialog)
                .subscribe(ignore -> paymentOptionsAdapter.setDefaultCard(position), errorAction);
    }

    @Override
    public void onItemClick(int position) {
        PaymentModel model = paymentOptionsAdapter.getItem(position);
        if (model != null && !model.isDefault) {
            showSetDefaultCardDialog(model.id, position);
        }
    }
}
