package com.dropininc.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dropininc.R;
import com.dropininc.activity.DroperatorFlowActivity;
import com.dropininc.customview.HighlightEditText;
import com.dropininc.dialog.ListDialog;
import com.dropininc.model.OperatorFlowModel;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created on 14.03.16.
 */
public class DroperatorRegisterStep2Fragment extends BaseFragment implements
        HighlightEditText.OnStateChangeListener {

    private static final String OPERATOR_FLOW_BUNDLE = "OPERATOR_FLOW_BUNDLE";

    private final String[] STATES = new String[] { "Alabama", "Alaska", "Arizona", "Arkansas",
            "California", "Colorado", "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii",
            "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Louisiana", "Maine",
            "Maryland", "Massachusetts", "Michigan", "Minnesota", "Mississippi", "Missouri",
            "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico",
            "New York", "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon",
            "Pennsylvania", "Rhode Island", "South Carolina", "South Dakota", "Tennessee",
            "Texas", "Utah", "Vermont", "Virginia", "Washington", "West Virginia",
            "Wisconsin", "Wyoming"
    };

    @Bind(R.id.dropRegisterStreetEditText)
    protected HighlightEditText streetEditText;
    @Bind(R.id.dropRegisterCityEditText)
    protected HighlightEditText cityEditText;
    @Bind(R.id.dropRegisterStateEditText)
    protected HighlightEditText stateNameEditText;
    @Bind(R.id.dropRegisterZipCodeEditText)
    protected HighlightEditText zipCodeNameEditText;
    @Bind(R.id.dropRegisterNextButton)
    protected Button nextButton;

    private ArrayList<String> stateList = new ArrayList<>(Arrays.asList(STATES));
    private OperatorFlowModel mOperatorFlowModel;
    private Activity mActivity;
    private int currentStateIndex = -1;

    public static DroperatorRegisterStep2Fragment getInstance(OperatorFlowModel model) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(OPERATOR_FLOW_BUNDLE, model);
        DroperatorRegisterStep2Fragment fragment = new DroperatorRegisterStep2Fragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

        mOperatorFlowModel = (OperatorFlowModel)getArguments().getSerializable(OPERATOR_FLOW_BUNDLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_droperator_register_step_2, container, false);
        ButterKnife.bind(this, view);

        initStateChangedListener(this);

        return view;
    }

    private void initStateChangedListener(HighlightEditText.OnStateChangeListener listener) {
        streetEditText.setOnStateChangeListener(listener);
        cityEditText.setOnStateChangeListener(listener);
        stateNameEditText.setOnStateChangeListener(listener);
        zipCodeNameEditText.setOnStateChangeListener(listener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        initStateChangedListener(null);
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.dropRegisterStateEditText)
    protected void onStateClicked() {
        ListDialog listDialog = new ListDialog(mActivity, stateList, currentStateIndex) {
            @Override
            public void onClick(int index) {
                currentStateIndex = index;
                stateNameEditText.setText(stateList.get(index));
            }
        };
        listDialog.show();
    }

    @Override
    public void onStateChanged(HighlightEditText.State state) {
        boolean inputIsValid = checkStates(streetEditText, cityEditText, stateNameEditText,
                zipCodeNameEditText);
        nextButton.setEnabled(inputIsValid);
        nextButton.setText(inputIsValid ? R.string.next : R.string.field_completion_hint);
    }

    @OnClick(R.id.dropRegisterNextButton)
    protected void onNextClicked() {
        mOperatorFlowModel.street = streetEditText.getText().toString();
        mOperatorFlowModel.city = cityEditText.getText().toString();
        mOperatorFlowModel.state = stateNameEditText.getText().toString();
        mOperatorFlowModel.zipCode = zipCodeNameEditText.getText().toString();
        ((DroperatorFlowActivity) getActivity()).pushFragment(
                DroperatorRegisterStep3Fragment.getInstance(mOperatorFlowModel));
    }

    private boolean checkStates(HighlightEditText... editTexts) {
        for (HighlightEditText editText : editTexts) {
            if (editText.isRequired()
                    && !editText.getCurrentState().equals(HighlightEditText.State.HIGHLIGHTED)) {
                return false;
            }
        }
        return true;
    }
}
