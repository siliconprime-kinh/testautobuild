package com.dropininc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dropininc.R;
import com.dropininc.utils.Logs;

import me.brendanweinstein.CardType;
import me.brendanweinstein.views.FieldHolder;

public class AddCardActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "AddCardActivity";

    public static final String CARD_NUMBER_EXTRA    = "CARD_NUMBER_EXTRA";
    public static final String CARD_CVV_EXTRA       = "CARD_CVV_EXTRA";
    public static final String CARD_MONTH_EXTRA     = "CARD_MONTH_EXTRA";
    public static final String CARD_YEAR_EXTRA      = "CARD_YEAR_EXTRA";
    public static final String CARD_TYPE            = "CARD_TYPE_EXTRA";

    private FieldHolder mFieldHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        initView();
    }

    private void initView() {
        Button buttonAddCard = (Button) findViewById(R.id.buttonAddCard);
        buttonAddCard.setOnClickListener(this);
        mFieldHolder = (FieldHolder) findViewById(R.id.field_holder);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.add_credit_card);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonAddCard:
                addNewCard();
                break;
        }
    }

    private void addNewCard() {
        String cardNumber = mFieldHolder.getCardNumHolder().getCardField().getText().toString();
        int cardExpMonth = 0;
        try {
            cardExpMonth = Integer.valueOf(mFieldHolder.getExpirationEditText().getMonth());
        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
        }
        int cardExpYear = 0;
        try {
            cardExpYear = Integer.valueOf(mFieldHolder.getExpirationEditText().getYear());
        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
        }
        String cardCVC = mFieldHolder.getCVVEditText().getText().toString();
        CardType cardType = mFieldHolder.getCardIcon().getCardType();
        String cardTypeName = CardType.MASTERCARD == cardType ? "MasterCard" : cardType.getName();

        Logs.log(TAG, "cardNumber = " + cardNumber + " cardExpMonth = " + cardExpMonth + " cardExpYear = "
                + cardExpYear + " cardCVC = " + cardCVC);

        if (mFieldHolder.isFieldsValid()) {
            Intent intent = new Intent()
                    .putExtra(CARD_TYPE, cardTypeName)
                    .putExtra(CARD_NUMBER_EXTRA, cardNumber)
                    .putExtra(CARD_MONTH_EXTRA, cardExpMonth)
                    .putExtra(CARD_YEAR_EXTRA, cardExpYear)
                    .putExtra(CARD_CVV_EXTRA, cardCVC);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this, R.string.add_card_enter_valid_value, Toast.LENGTH_SHORT).show();
        }
    }
}
