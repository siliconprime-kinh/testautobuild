package com.dropininc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.activity.MainActivity;
import com.dropininc.model.LogNetModel;
import com.dropininc.model.ResumeCheckModel;
import com.dropininc.model.VerifyModel;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.Utils;

import java.util.ArrayList;


public class SettingFragment extends BaseFragment implements View.OnClickListener {
    private String TAG = getClass().getName();
    private FragmentActivity mActivity;
    private View mRootView;

    private RelativeLayout lay_edit_peofile;
    private RelativeLayout lay_gigs_cancel; /*KINH cancel gigs*/
    private RelativeLayout lay_log_network; /*KINH log network*/
    private RelativeLayout lay_log_pusher; /*KINH log pusher*/
    private RelativeLayout lay_log_server; /*KINH log server*/
    private TextView lay_log_network_first; /*KINH log network*/
    private TextView lay_log_pusher_first; /*KINH log pusher*/
    private RelativeLayout lay_language;
    private RelativeLayout lay_become_operator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_setting, container, false);
        ((MainActivity) mActivity).addToolBarNormal(getString(R.string.settings));
        initView();

        if (((MainActivity) mActivity).isCheckResumeProfile()) {
            ProfileFragment pro = new ProfileFragment();
            ((MainActivity) mActivity).pushFragments(pro);
            ((MainActivity) mActivity).addToolBarNormal(getString(R.string.profile));
            ((MainActivity) mActivity).setCheckResumeProfile(false);
        }

        return mRootView;
    }


    private void initView() {
        lay_edit_peofile = (RelativeLayout) mRootView.findViewById(R.id.lay_edit_peofile);
        lay_gigs_cancel = (RelativeLayout) mRootView.findViewById(R.id.lay_gigs_cancel);

        lay_language = (RelativeLayout) mRootView.findViewById(R.id.lay_language);
        lay_become_operator = (RelativeLayout) mRootView.findViewById(R.id.lay_become_operator);

        lay_edit_peofile.setOnClickListener(this);
        lay_gigs_cancel.setOnClickListener(this);

        lay_language.setOnClickListener(this);
        lay_become_operator.setOnClickListener(this);

        ////KINH log///
        lay_log_network = (RelativeLayout) mRootView.findViewById(R.id.lay_log_network);
        lay_log_network_first = (TextView) mRootView.findViewById(R.id.lay_log_network_first);
        lay_log_pusher = (RelativeLayout) mRootView.findViewById(R.id.lay_log_pusher);
        lay_log_pusher_first = (TextView) mRootView.findViewById(R.id.lay_log_pusher_first);
        lay_log_server = (RelativeLayout) mRootView.findViewById(R.id.lay_log_server);
        lay_log_network.setOnClickListener(this);
        lay_log_pusher.setOnClickListener(this);
        lay_log_server.setOnClickListener(this);

        LogNetModel logNetWork = AppApplication.getInstance().getLogNetWork();
        LogNetModel logPusher = AppApplication.getInstance().getLogPusher();
        if(logNetWork != null && logNetWork.msg != null && logNetWork.msg.size() > 0){
            String msg = logNetWork.msg.get(logNetWork.msg.size() - 1);
            if(!TextUtils.isEmpty(msg)){
                lay_log_network_first.setText(msg);
            }
        }
        if(logPusher != null && logPusher.msg != null && logPusher.msg.size() > 0){
            String msg = logPusher.msg.get(logPusher.msg.size() - 1);
            if(!TextUtils.isEmpty(msg)){
                lay_log_pusher_first.setText(msg);
            }
        }

        mRootView.findViewById(R.id.sendLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.sendLogsIntoEmail(mActivity);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lay_edit_peofile:
                ProfileFragment pro = new ProfileFragment();
                ((MainActivity) mActivity).pushFragments(pro);
                ((MainActivity) mActivity).addToolBarNormal(getString(R.string.profile));
                break;
            /*KINH cancel gigs*/
            case R.id.lay_gigs_cancel:
                cancelGigs();
                break;
            case R.id.lay_log_network:
                LogFragment logNetwork = LogFragment.newInstance(LogFragment.NETWORK_MODE);
                ((MainActivity) mActivity).pushFragments(logNetwork);
                ((MainActivity) mActivity).addToolBarNormal("Log NetWork");
                break;
            case R.id.lay_log_pusher:
                LogFragment logPusher = LogFragment.newInstance(LogFragment.PUSHER_MODE);
                ((MainActivity) mActivity).pushFragments(logPusher);
                ((MainActivity) mActivity).addToolBarNormal("Log Pusher");
                break;
            case R.id.lay_log_server:
                LogFragment logServer = LogFragment.newInstance(LogFragment.SERVER_MODE);
                ((MainActivity) mActivity).pushFragments(logServer);
                ((MainActivity) mActivity).addToolBarNormal("Log Server");
                break;
            case R.id.lay_language:
//                LanguageFragment frag = new LanguageFragment();
//                ((MainActivity) mActivity).pushFragments(frag);
                break;
            case R.id.lay_become_operator:
                break;
        }

    }


    /*KINH cancel gigs*/
    private ArrayList<ResumeCheckModel> gigsCancel = null;
    private Handler gigsCancelhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(gigsCancel == null || gigsCancel.size() <= 0){
                Toast.makeText(AppApplication.getInstance(), "Cancel gigs. finish", Toast.LENGTH_SHORT).show();
                return;
            }

            final ResumeCheckModel item = gigsCancel.remove(0);
            if(item != null && !TextUtils.isEmpty(item.id)){
                networkManager.cancelStream(item.id)

                        .subscribe((endStreamModel)-> {
                            gigsCancelhandler.sendEmptyMessage(0);

                        }, throwable -> {
                            AppApplication.getInstance().logErrorServer("cancelStream" , networkManager.parseError(throwable));
                            Toast.makeText(AppApplication.getInstance(), "Cancel gigs >> gigId=" + item.id+".Fail=" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            gigsCancelhandler.sendEmptyMessage(0);

                        });
            }else{
                gigsCancelhandler.sendEmptyMessage(0);
            }
        }
    };

    private void cancelGigs(){
        String json = DSharePreference.getProfile(mActivity);
            if(TextUtils.isEmpty(json)){
            Toast.makeText(AppApplication.getInstance(), "Cancel gigs. finish. total= 0", Toast.LENGTH_SHORT).show();
            return;
        }
        VerifyModel verify = VerifyModel.fromJSON(json);
        if(verify == null || verify.account == null){
            Toast.makeText(AppApplication.getInstance(),"Cancel gigs. finish. total= 0",Toast.LENGTH_SHORT).show();
            return;
        }

        String accountId = verify.account.id;

        networkManager.getAllGigs(accountId,"operatorEnroute,handshaking")
                .subscribe(searchModels -> {
                    gigsCancel = searchModels;
                    if(searchModels == null || searchModels.size() <= 0){
                        Toast.makeText(AppApplication.getInstance(),"Cancel gigs. finish. total= 0",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    gigsCancelhandler.sendEmptyMessage(0);

                },  throwable -> {
                    AppApplication.getInstance().logErrorServer("getGigsForCancel" , networkManager.parseError(throwable));
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AppApplication.getInstance(), "Cancel gigs. finish. error=" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }});
                });

    }


}
