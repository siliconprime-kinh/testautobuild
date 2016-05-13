package com.dropininc.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.adapter.HistoryAdapter;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.HistoryItemModel;
import com.dropininc.model.VerifyModel;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.FontUtils;
import com.google.gson.Gson;

import java.util.ArrayList;


public class HistoryFragment extends BaseFragment implements View.OnClickListener {
    private String TAG = getClass().getName();
    private FragmentActivity mActivity;
    private View mRootView;
    private ListView list;
    private Button btnMore;
    private Button btnPurchase;
    private Button btnEarning;
    private TextView txtDes;

    private HistoryAdapter adapter;
    ProgressBar pb_loading;
    ProgressBar pb_loading_mode;

    int curenttab = 0;
    int currentPurchaseIndex = 0;
    int currentEarningIndex = 0;
    int pageItem = 10;

    ArrayList<HistoryItemModel> purchaseList = new ArrayList<HistoryItemModel>();
    ArrayList<HistoryItemModel> earningList = new ArrayList<HistoryItemModel>();
    boolean isHasMorePurchase = true;
    boolean isHasMoreEarning = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_history, container, false);

        initView();

        if (isOnline()) {
            purchaseList.clear();
            earningList.clear();
            getHistoryPurchase(false);
            getHistoryEarning(false);
        } else {
            showAlertDialog(getString(R.string.network_problem), getString(R.string.please_connect_to_continue), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideAlertDialog();
                }
            });
        }

        return mRootView;
    }

    private void initView() {
        adapter = new HistoryAdapter(mContext);
        list = (ListView) mRootView.findViewById(R.id.list);

        txtDes = (TextView) mRootView.findViewById(R.id.txtDes);
        btnMore = (Button) mRootView.findViewById(R.id.btnMore);
        pb_loading = (ProgressBar) mRootView.findViewById(R.id.pb_loading);
        pb_loading_mode = (ProgressBar) mRootView.findViewById(R.id.pb_loading_mode);
        btnEarning = (Button) mRootView.findViewById(R.id.btnEarning);
        if (AppApplication.getInstance().isOperator()) btnEarning.setVisibility(View.VISIBLE);
        btnPurchase = (Button) mRootView.findViewById(R.id.btnPurchase);

        FontUtils.typefaceButton(btnMore, FontType.LIGHT);
        FontUtils.typefaceButton(btnPurchase, FontType.LIGHT);
        FontUtils.typefaceButton(btnEarning, FontType.LIGHT);

        btnPurchase.setSelected(true);
        btnEarning.setSelected(false);
        btnPurchase.setOnClickListener(this);
        btnEarning.setOnClickListener(this);
        btnMore.setOnClickListener(this);

        list.setAdapter(adapter);
    }

    private void loadData() {
        if (curenttab == 0) {
            if (purchaseList.size() > 0) {
                txtDes.setVisibility(View.GONE);
            } else {
                txtDes.setVisibility(View.VISIBLE);
            }
            controlLoadMoreButton(isHasMorePurchase);
            adapter.setData(purchaseList, curenttab);
        } else {
            if (earningList.size() > 0) {
                txtDes.setVisibility(View.GONE);
            } else {
                txtDes.setVisibility(View.VISIBLE);
            }
            adapter.setData(earningList, curenttab);
            controlLoadMoreButton(isHasMoreEarning);
        }
    }

    private void controlLoadMoreButton(boolean isShow) {
        if (isShow) {
            btnMore.setVisibility(View.VISIBLE);
        } else {
            btnMore.setVisibility(View.GONE);
        }
    }

    private void getHistoryPurchase(boolean isLoadMore) {
        if (isLoadMore) {
            pb_loading_mode.setVisibility(View.VISIBLE);
        } else {
            pb_loading.setVisibility(View.VISIBLE);
        }
        if (purchaseList.size() > 0) {
            currentPurchaseIndex = currentPurchaseIndex + pageItem;
        }
        String json = DSharePreference.getProfile(mActivity);
        VerifyModel model = new Gson().fromJson(json, VerifyModel.class);

        networkManager.getHistoryPurchase(currentPurchaseIndex, pageItem, "createdAt%20desc", model.account.id)
                .doAfterTerminate(() -> {
                    pb_loading.setVisibility(View.GONE);
                    pb_loading_mode.setVisibility(View.GONE);
                })
                .subscribe(historyItemModels -> {
                    if (historyItemModels.size() > 0) {
                        for (int i = 0; i < historyItemModels.size(); i++) {
                            if (historyItemModels.get(i).payment.amount != null) {
                                purchaseList.add(historyItemModels.get(i));
                            }
                        }
                        isHasMorePurchase = historyItemModels.size() >= pageItem;
                    } else {
                        isHasMorePurchase = false;
                    }
                    if (curenttab == 0) {
                        loadData();
                    }
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("getHistoryPurchase", networkManager.parseError(throwable));
                    loadData();
                    showAlertDialog(networkManager.parseError(throwable).message);
                });
    }

    private void getHistoryEarning(boolean isLoadMore) {
        if (isLoadMore) {
            pb_loading_mode.setVisibility(View.VISIBLE);
        } else {
            pb_loading.setVisibility(View.VISIBLE);
        }
        if (earningList.size() > 0) {
            currentEarningIndex = currentEarningIndex + pageItem;
        }
        String json = DSharePreference.getProfile(mActivity);
        VerifyModel model = new Gson().fromJson(json, VerifyModel.class);

        networkManager.getHistoryEarning(currentEarningIndex, pageItem, "createdAt%20desc", model.account.id)
                .doAfterTerminate(() -> {
                    pb_loading.setVisibility(View.GONE);
                    pb_loading_mode.setVisibility(View.GONE);
                })
                .subscribe(historyItemModels -> {
                    if (historyItemModels.size() > 0) {
                        for (int i = 0; i < historyItemModels.size(); i++) {
                            if (historyItemModels.get(i).payment.amount != null) {
                                earningList.add(historyItemModels.get(i));
                            }
                        }

                        isHasMoreEarning = historyItemModels.size() >= pageItem;
                    } else {
                        isHasMoreEarning = false;
                    }
                    if (curenttab != 0) {
                        loadData();
                    }
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("getHistoryEarning", networkManager.parseError(throwable));
                    loadData();
                    showAlertDialog(networkManager.parseError(throwable).message);
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnEarning:
                curenttab = 1;
                txtDes.setVisibility(View.GONE);
                btnPurchase.setSelected(false);
                btnEarning.setSelected(true);
                if (earningList.size() == 0) {
                    getHistoryEarning(false);
                }else{
                    loadData();
                }
                break;
            case R.id.btnPurchase:
                curenttab = 0;
                txtDes.setVisibility(View.GONE);
                btnPurchase.setSelected(true);
                btnEarning.setSelected(false);
                if (purchaseList.size() == 0) {
                    getHistoryPurchase(false);
                }else{
                    loadData();
                }
                break;
            case R.id.btnMore:
                if (isOnline()) {
                    if (curenttab == 0) {
                        getHistoryPurchase(true);
                    } else {
                        getHistoryEarning(true);
                    }
                }
                break;
        }
    }
}
