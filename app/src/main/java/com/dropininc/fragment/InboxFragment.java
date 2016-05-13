package com.dropininc.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
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
import com.dropininc.dialog.ConfirmDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.MessagesModel;
import com.dropininc.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;


public class InboxFragment extends BaseFragment implements BaseItemClickListener {
    private final int ITEM_COUNT = 10;
    @Bind(R.id.inboxRecyclerView)
    protected RecyclerViewEmptySupport inboxRecyclerView;
    @Bind(R.id.btnArchived)
    protected Button btnArchived;
    @Bind(R.id.emptyView)
    protected TextView emptyView;
    @Bind(R.id.txtRecentMessages)
    protected TextView recentMessagesLabel;
    private MainActivity mainActivity;
    private MessagesAdapter adapter;
    private boolean isArchiveButtonVisible = false;
    private Subscription subscription;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        ButterKnife.bind(this, view);
        setStyle();
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        /*
        if (adapter != null) {
            adapter.hideLoadingItem();
        }
        */
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
        inboxRecyclerView.setLayoutManager(linearLayoutManager);
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //try archive message
                int swipePosition = viewHolder.getAdapterPosition();
                showArchiveMessageDialog(swipePosition);
            }
        };
        ItemTouchHelper touchHelper = new ItemTouchHelper(simpleCallback);
        touchHelper.attachToRecyclerView(inboxRecyclerView);
        inboxRecyclerView.setEmptyView(emptyView);
        inboxRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        inboxRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.d("KINH", "INBOX>>onLoadMore:page=" + page + ".totalItemsCount=" + totalItemsCount);
                if (totalItemsCount != 0 && adapter.getLastItem() != null) {
                    //adapter.showLoadingItem();
                    getInbox(totalItemsCount);
                }
            }
        });

        btnArchived.setVisibility(isArchiveButtonVisible ? View.VISIBLE : View.GONE);

        if (isOnline()) {
            if (adapter == null) {
                showProgressDialog(getString(R.string.processing), null);
                getInbox(0);
            } else {
                inboxRecyclerView.setAdapter(adapter);
            }
        } else {
            showAlertDialog(getString(R.string.network_problem), getString(R.string.please_connect_to_continue), v -> {
                hideAlertDialog();
            });
        }
    }

    private void setStyle() {
        FontUtils.typefaceTextView(recentMessagesLabel, FontType.REGULAR);
        FontUtils.typefaceTextView(btnArchived, FontType.LIGHT);
    }

    @OnClick(R.id.btnArchived)
    protected void onArchiveButtonClicked() {
        ArchiveFragment frag = new ArchiveFragment();
        mainActivity.pushFragments(frag);
    }

    private void showArchiveMessageDialog(int position) {
        final ConfirmDialog mDialog = new ConfirmDialog(mainActivity);
        mDialog.setTitleDialog("");
        mDialog.setCancelableDialog(false);
        mDialog.setMessageDialog(R.string.message_you_want_to_archive_this_message);
        mDialog.setOkTitleButton(R.string.yes);
        mDialog.setCancelTitleButton(R.string.no);
        mDialog.setOkButtonClick(v -> {
            MessagesModel model = adapter.getItem(position);
            if (model != null) {
                archiveMessage(model.id, position);
            }
            mDialog.dismiss();
        });
        mDialog.setCancelButtonClick(v -> {
            adapter.notifyItemChanged(position);
            mDialog.dismiss();
        });
        mDialog.show();
    }

    private void processLoading(List<MessagesModel> messagesModels) {
        if (messagesModels.size() > 0) {
            if (adapter == null) {
                adapter = new MessagesAdapter(getActivity(), messagesModels, this);
                inboxRecyclerView.setAdapter(adapter);
            } else {
                adapter.addItems(messagesModels);
            }
        }
    }
    /*
    private void checkArchive(){
        subscription = networkManager.getInbox(true, 0, ITEM_COUNT)
                .doAfterTerminate(this::hideProgressDialog)
                .subscribe(archiveMessagesModels -> {
                            Log.d("KINH", "INBOX>>getInbox>>.KQ(archiveMessagesModels)=" + (archiveMessagesModels != null ? archiveMessagesModels.size() : "0"));
                            isArchiveButtonVisible = !archiveMessagesModels.isEmpty();
                            btnArchived.setVisibility(isArchiveButtonVisible ? View.VISIBLE : View.GONE);
                        },
                        throwable -> {
                            AppApplication.getInstance().logErrorServer("getInbox", networkManager.parseError(throwable));
                            showAlertDialog(networkManager.parseError(throwable).message);
                        });
    }*/
    private void getInbox(int lastItemIndex) {
        subscription = networkManager.getInbox(false, lastItemIndex, ITEM_COUNT)
                .flatMap(messagesModels -> {
                    //hideInsideLoading();
                    processLoading(messagesModels);
                    return networkManager.getInbox(true, 0, ITEM_COUNT);
                })
                .doAfterTerminate(this::hideProgressDialog)
                .subscribe(archiveMessagesModels -> {
                            isArchiveButtonVisible = !archiveMessagesModels.isEmpty();
                            btnArchived.setVisibility(isArchiveButtonVisible ? View.VISIBLE : View.GONE);
                        },
                        throwable -> {
                            //hideInsideLoading();
                            showAlertDialog(networkManager.parseError(throwable).message);
                        });
    }
    /*
    private void getInbox(int lastItemIndex) {
        Log.d("KINH", "INBOX>>getInbox:lastItemIndex=" + lastItemIndex + ".START=" );

        subscription = networkManager.getInbox(false, lastItemIndex, ITEM_COUNT)
                .subscribe(messagesModels -> {
                            Log.d("KINH", "INBOX>>getInbox>>lastItemIndex=" + lastItemIndex + ".KQ(messagesModels)=" + (messagesModels != null ? messagesModels.size() : "0"));
                            hideInsideLoading();
                            processLoading(messagesModels);
                            checkArchive();
                        },
                        throwable -> {
                            AppApplication.getInstance().logErrorServer("getInbox", networkManager.parseError(throwable));
                            hideInsideLoading();
                            hideProgressDialog();
                            showAlertDialog(networkManager.parseError(throwable).message);
                        });
        Log.d("KINH", "INBOX>>getInbox:lastItemIndex=" + lastItemIndex + ".END=" );
    }*/

    /*
    private void hideInsideLoading(){
        if (adapter != null)
            adapter.hideLoadingItem();
    }
    */

    private void archiveMessage(String messageId, int position) {
        if (!TextUtils.isEmpty(messageId)) {
            networkManager.archiveMessage(messageId)
                    .subscribe(ignore -> {
                                adapter.removeItem(position);
                                if (!btnArchived.isShown()) {
                                    isArchiveButtonVisible = true;
                                    btnArchived.setVisibility(View.VISIBLE);
                                }
                            },
                            throwable -> {
                                AppApplication.getInstance().logErrorServer("archiveMessage", networkManager.parseError(throwable));
                                showAlertDialog(networkManager.parseError(throwable).message);
                                adapter.notifyItemChanged(position);
                            });
        }
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
