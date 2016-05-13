package com.dropininc.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.activity.MainActivity;
import com.dropininc.adapter.MessagesAdapter;
import com.dropininc.adapter.click_listener.BaseItemClickListener;
import com.dropininc.customview.DividerItemDecoration;
import com.dropininc.customview.EndlessRecyclerViewScrollListener;
import com.dropininc.customview.RecyclerViewEmptySupport;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.MessagesModel;
import com.dropininc.utils.FontUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ArchiveFragment extends BaseFragment implements BaseItemClickListener {
    private final int ITEM_COUNT = 10;
    @Bind(R.id.archiveRecyclerView)
    protected RecyclerViewEmptySupport archiveRecyclerView;
    @Bind(R.id.btnBack)
    protected Button btnArchived;
    @Bind(R.id.emptyView)
    protected TextView emptyView;
    @Bind(R.id.txtArchivedMessages)
    protected TextView archivedMessagesLabel;
    private MainActivity mainActivity;
    private MessagesAdapter adapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archiv_messagese, container, false);
        ButterKnife.bind(this, view);
        setStyle();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        archiveRecyclerView.setLayoutManager(linearLayoutManager);
        archiveRecyclerView.setEmptyView(emptyView);
        archiveRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        archiveRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (totalItemsCount != 0 && adapter.getLastItem() != null) {
                    adapter.showLoadingItem();
                    getArchived(totalItemsCount);
                }
            }
        });

        if (isOnline()) {
            if (adapter == null) {
                showProgressDialog(getString(R.string.processing), null);
                getArchived(0);
            } else {
                archiveRecyclerView.setAdapter(adapter);
            }
        } else {
            showAlertDialog(getString(R.string.network_problem), getString(R.string.please_connect_to_continue), v -> {
                hideAlertDialog();
            });
        }
    }

    private void setStyle() {
        FontUtils.typefaceTextView(archivedMessagesLabel, FontType.REGULAR);
        FontUtils.typefaceTextView(btnArchived, FontType.LIGHT);
        FontUtils.typefaceTextView(emptyView, FontType.REGULAR);
    }

    @OnClick(R.id.btnBack)
    protected void onBackButtonPressed() {
        mainActivity.popFragments();
    }

    private void processLoading(List<MessagesModel> messagesModels) {
        if (messagesModels.size() > 0) {
            if (adapter == null) {
                adapter = new MessagesAdapter(getActivity(), messagesModels, this);
                archiveRecyclerView.setAdapter(adapter);
            } else {
                adapter.addItems(messagesModels);
            }
        }
    }

    private void getArchived(int lastItemIndex) {
        networkManager.getInbox(true, lastItemIndex, ITEM_COUNT)
                .doAfterTerminate(this::hideProgressDialog)
                .subscribe(messagesModels -> {
                    hideInsideLoading();
                    processLoading(messagesModels);
                }, throwable -> {
                    AppApplication.getInstance().logErrorServer("getArchived", networkManager.parseError(throwable));
                    hideInsideLoading();
                    showAlertDialog(networkManager.parseError(throwable).message);
                });
    }

    private void hideInsideLoading(){
        if (adapter != null)
            adapter.hideLoadingItem();
    }

    @Override
    public void onItemClick(int position) {
        MessagesModel messagesModel = adapter.getItem(position);
        MessagesDetailsFragment frag = new MessagesDetailsFragment();
        Bundle b = new Bundle();
        b.putString("MODEL", messagesModel.toJSON());
        frag.setArguments(b);

        // mark as read
        messagesModel.status = "received";
        adapter.notifyItemChanged(position);

        mainActivity.pushFragments(frag);
    }

}
