package com.dropininc.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.activity.ContactUsActivity;
import com.dropininc.activity.MainActivity;
import com.dropininc.adapter.HelpCenterAdapter;
import com.dropininc.interfaces.FontType;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.zendesk.sdk.model.helpcenter.Article;
import com.zendesk.sdk.model.helpcenter.HelpCenterSearch;
import com.zendesk.sdk.model.helpcenter.SearchArticle;
import com.zendesk.sdk.network.HelpCenterProvider;
import com.zendesk.sdk.network.impl.ZendeskHelpCenterProvider;
import com.zendesk.sdk.support.ViewArticleActivity;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;

import java.util.ArrayList;
import java.util.List;


public class HelpCenterFragment extends BaseFragment {

    private String TAG = getClass().getName();
    private FragmentActivity mActivity;
    private View mRootView;
    private ListView list;
    private Button btnContactUs, buttonCancel, buttonDone;
    private EditText atvSearch;
    private ImageView imgClear;
    private TextView txtDes;

    private HelpCenterProvider provider;
    private HelpCenterAdapter adapter;

    private ArrayList<Article> mainArticles = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        ((MainActivity) mActivity).addToolBarNormal(getString(R.string.help_center));

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void getListQuestion() {
        showProgressDialog("", dialog -> {

        });
        provider.getArticles(202845077L, new ZendeskCallback<List<Article>>() {
            @Override
            public void onSuccess(List<Article> articles) {
                Logs.log(TAG, "articles " + articles.size());
                if (articles != null && articles.size() > 0) {
                    showAlertNoResult(false);
                    mainArticles = (ArrayList) articles;
                    adapter.setData(mainArticles);
                    list.setAdapter(adapter);
                } else {
                    showAlertNoResult(true);
                }
                hideProgressDialog();
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                Logs.log(TAG, "error: " + errorResponse.getReason());
                hideProgressDialog();
                showAlertNoResult(true);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_help_center, container, false);
        provider = new ZendeskHelpCenterProvider();
        adapter = new HelpCenterAdapter(mActivity);

        initView();

        getListQuestion();
        return mRootView;
    }

    private void hideKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    private void initView() {
        atvSearch = (EditText) mRootView.findViewById(R.id.atvSearch);
        list = (ListView) mRootView.findViewById(R.id.list);
        btnContactUs = (Button) mRootView.findViewById(R.id.btnContactUs);
        buttonCancel = (Button) mRootView.findViewById(R.id.bt_cancel);
        buttonDone = (Button) mRootView.findViewById(R.id.bt_save);
        imgClear = (ImageView) mRootView.findViewById(R.id.imgClear);
        txtDes = (TextView) mRootView.findViewById(R.id.txtDes);

        imgClear.setOnClickListener(v -> {
            atvSearch.setText("");
            if (mainArticles != null && mainArticles.size() > 0) {
                adapter.setData(mainArticles);
                list.setAdapter(adapter);
                showAlertNoResult(false);
            } else {
                list.setAdapter(null);
                showAlertNoResult(false);
            }
        });

        btnContactUs.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ContactUsActivity.class);
            startActivity(intent);
        });

        list.setOnItemClickListener((parent, view, position, id) -> ViewArticleActivity.startActivity(mContext, adapter.getItem(position)));

        atvSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    buttonDone.setEnabled(true);
                    imgClear.setVisibility(View.VISIBLE);
                } else {
                    buttonDone.setEnabled(false);
                    imgClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        atvSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //do something
                searchAction();
            }
            return false;
        });

        buttonDone.setOnClickListener(v -> searchAction());

        buttonCancel.setOnClickListener(v -> {
//                atvSearch.setText("");
//                if (mainArticles != null && mainArticles.size() > 0) {
//                    adapter.setData(mainArticles);
//                    list.setAdapter(adapter);
//                    showAlertNoResult(false);
//                } else {
//                    list.setAdapter(null);
//                    showAlertNoResult(false);
//                }
            hideKeyboard();
            onBackPressed();
        });

        FontUtils.typefaceTextView(txtDes, FontType.REGULAR);
        FontUtils.typefaceEditText(atvSearch, FontType.REGULAR);
        FontUtils.typefaceButton(btnContactUs, FontType.LIGHT);
        FontUtils.typefaceButton(buttonCancel, FontType.REGULAR);
        FontUtils.typefaceButton(buttonDone, FontType.REGULAR);
        TextView txtTitle = (TextView) mRootView.findViewById(R.id.txtTitle);
        FontUtils.typefaceTextView(txtTitle, FontType.REGULAR);

        TextView txtFAQ = (TextView) mRootView.findViewById(R.id.txtFAQ);
        FontUtils.typefaceTextView(txtFAQ, FontType.REGULAR);

    }

    public void onBackPressed() {
        ((MainActivity) mContext).onBackPressed();
    }

    private void searchAction() {
        HelpCenterSearch.Builder builder = new HelpCenterSearch.Builder();
        Logs.log(TAG, "SEARCH: " + atvSearch.getText().toString());
        builder.withQuery(atvSearch.getText().toString());
        HelpCenterSearch helpCenterSearch = builder.build();
        showProgressDialog("", dialog -> {

        });
        provider.searchArticles(helpCenterSearch, new ZendeskCallback<List<SearchArticle>>() {
            @Override
            public void onSuccess(List<SearchArticle> searchArticles) {
                Logs.log(TAG, "SearchArticle " + searchArticles.size());
                if (searchArticles.size() > 0) {
                    showAlertNoResult(false);
                    ArrayList<Article> mainArticles = new ArrayList<>();
                    for (int i = 0; i < searchArticles.size(); i++) {
                        mainArticles.add(searchArticles.get(i).getArticle());
                    }
                    adapter.setData(mainArticles);
                    list.setAdapter(adapter);
                } else {
                    showAlertNoResult(true);
                    adapter = new HelpCenterAdapter(mActivity);
                    list.setAdapter(adapter);
                }
                hideProgressDialog();
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                Logs.log(TAG, "SearchArticle onError " + errorResponse.getReason());
                hideProgressDialog();
                showAlertNoResult(true);
            }
        });
    }

    private void showAlertNoResult(boolean isShow) {
        if (isShow) {
            txtDes.setVisibility(View.VISIBLE);
        } else {
            txtDes.setVisibility(View.GONE);
        }
    }
}
