package com.dropininc.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.activity.DroperatorFlowActivity;
import com.dropininc.customview.HighlightEditText;
import com.dropininc.dialog.ProgressDialog;
import com.dropininc.model.AccountModel;
import com.dropininc.model.OperatorFlowModel;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created on 14.03.16.
 */
public class DroperatorRegisterStep1Fragment extends BaseFragment implements
        HighlightEditText.OnStateChangeListener, DatePickerDialog.OnDateSetListener {

    @Bind(R.id.dropRegisterFirstNameEditText)
    protected HighlightEditText firstNameEditText;
    @Bind(R.id.dropRegisterLastNameEditText)
    protected HighlightEditText lastNameEditText;
    @Bind(R.id.dropRegisterSocialSecurityEditText)
    protected HighlightEditText socialSecurityNameEditText;
    @Bind(R.id.dropRegisterBirthdayEditText)
    protected HighlightEditText birthdayNameEditText;
    @Bind(R.id.dropRegisterNextButton)
    protected Button nextButton;

    private Calendar calendar;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
    private DatePickerDialog datePickerDialog;

    private ProgressDialog mProgressDialog;
    private Activity mActivity;
    private OperatorFlowModel operatorFlowModel = new OperatorFlowModel();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 21);
        datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        Calendar maxDateCalendar = Calendar.getInstance();
        maxDateCalendar.set(Calendar.YEAR, maxDateCalendar.get(Calendar.YEAR) - 18);
        datePickerDialog.setMaxDate(maxDateCalendar);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_droperator_register_step_1, container, false);
        ButterKnife.bind(this, view);

        initView();

        return view;
    }

    private void initView() {
        initStateChangedListener(this);
        firstNameEditText.post(this::getProfileFromServer);
    }

    private void initStateChangedListener(HighlightEditText.OnStateChangeListener listener) {
        firstNameEditText.setOnStateChangeListener(listener);
        lastNameEditText.setOnStateChangeListener(listener);
        socialSecurityNameEditText.setOnStateChangeListener(listener);
        birthdayNameEditText.setOnStateChangeListener(listener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        initStateChangedListener(null);
        ButterKnife.unbind(this);
    }

    @Override
    public void onStateChanged(HighlightEditText.State state) {
        boolean inputIsValid = checkStates(firstNameEditText, lastNameEditText, socialSecurityNameEditText,
                birthdayNameEditText);
        nextButton.setEnabled(inputIsValid);
        nextButton.setText(inputIsValid ? R.string.next : R.string.field_completion_hint);
    }

    @OnClick(R.id.dropRegisterBirthdayEditText)
    protected void onBirthdayClicked() {
        datePickerDialog.show(getActivity().getFragmentManager(), "datePicker");
    }

    @OnClick(R.id.dropRegisterNextButton)
    protected void onNextClicked() {
        operatorFlowModel.firstName = firstNameEditText.getText().toString();
        operatorFlowModel.lastName = lastNameEditText.getText().toString();
        operatorFlowModel.socialSecurityNumber = socialSecurityNameEditText.getText().toString();

        ((DroperatorFlowActivity) getActivity()).pushFragment(
                DroperatorRegisterStep2Fragment.getInstance(operatorFlowModel));
    }

    private boolean checkStates(HighlightEditText... editTexts) {
        for (HighlightEditText editText : editTexts) {
            if (editText.isRequired() && !editText.getCurrentState().equals(HighlightEditText.State.HIGHLIGHTED)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
        operatorFlowModel.year = year;
        operatorFlowModel.month = monthOfYear;
        operatorFlowModel.day = dayOfMonth;
        birthdayNameEditText.setText(simpleDateFormat.format(calendar.getTime()));
    }

    private void dismissDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void getProfileFromServer() {
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.show();

        networkManager.getProfile(AppApplication.getInstance().getAccountId())
                .doAfterTerminate(this::dismissDialog)
                .subscribe(this::processResponseProfile, throwable -> {
                    AppApplication.getInstance().logErrorServer("getProfile", networkManager.parseError(throwable));
                    showAlertDialog("", networkManager.parseError(throwable).message, v -> {
                        hideAlertDialog();
                        onBackPressed();
                    });
                });
    }

    private void processResponseProfile(AccountModel model) {
        if (model == null) return;
        firstNameEditText.setText(model.firstName);
        lastNameEditText.setText(model.lastName);
    }
}
