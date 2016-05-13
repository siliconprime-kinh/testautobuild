package com.dropininc.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dropininc.AppApplication;
import com.dropininc.R;
import com.dropininc.activity.MainActivity;
import com.dropininc.activity.SignupTutorialActivity;
import com.dropininc.adapter.BaseAdapter;
import com.dropininc.dialog.ChatDialog;
import com.dropininc.dialog.ProgressDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.GeneralModel;
import com.dropininc.model.HistoryItemModel;
import com.dropininc.model.LogNetModel;
import com.dropininc.network.request.LocationRequest;
import com.dropininc.sharepreference.DSharePreference;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Logs;
import com.dropininc.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;
import java.util.Arrays;


public class LogFragment extends BaseFragment {

    private String TAG = getClass().getName();
    private FragmentActivity mActivity;
    private View mRootView;

    private ProgressDialog mProgressDialog;
    public static final int PUSHER_MODE = 1;
    public static final int NETWORK_MODE = 0;
    public static final int SERVER_MODE = 2;
    //private final int mode;
    private ArrayList<String> data = new ArrayList<String>();

    public static LogFragment newInstance(int mode) {
        LogFragment f = new LogFragment();
        Bundle args = new Bundle();
        args.putInt("mode", mode);
        f.setArguments(args);

        return f;
    }

    public int getModeLog() {
        int mode =  getArguments().getInt("mode", NETWORK_MODE);
        return mode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_log, container, false);
        String title = "Log";
        int mode = getArguments().getInt("mode", NETWORK_MODE);
        if(mode == NETWORK_MODE){
            title = "Log Network";
        }
        if(mode == PUSHER_MODE){
            title = "Log Pusher";
        }

        if(mode == SERVER_MODE){
            title = "Log Server";
        }
        ((MainActivity) mActivity).addToolBarNormal(title);
        initView();

        return mRootView;
    }

    ListView lv;
    MyAdapter adapter;

    Handler hander = new Handler();
    private void initView() {
        lv = (ListView) mRootView.findViewById(R.id.lv_log);
        adapter = new MyAdapter(mContext);
        lv.setAdapter(adapter);

        Runnable r = new Runnable(){
            public void run(){
                ArrayList<String> data = new ArrayList<String>();
                /*
                for(int i = 0; i < 200; i++){
                    data.add("Log......" + i + ".Mode=" + getModeLog());
                }*/
                ArrayList<String> ret = null;
                int mode =  getModeLog();
                LogNetModel logs = null;
                if(mode == NETWORK_MODE){
                    logs = AppApplication.getInstance().getLogNetWork();
                }
                if(mode == PUSHER_MODE){
                    logs = AppApplication.getInstance().getLogPusher();
                }
                if(mode == SERVER_MODE){
                    logs = AppApplication.getInstance().getLogErrorServer();
                }

                if(logs != null && logs.msg != null){
                    data = (ArrayList<String>)logs.msg.clone();
                }
                updateUI(data);
            }
        };
        hander.post(r);


    }

    void updateUI(ArrayList<String> dt){
        if (dt == null)return;
        synchronized (data){
            adapter.notifyDataSetInvalidated();
            data.clear();
            data =  new ArrayList<String>(dt);
            adapter.notifyDataSetChanged();

        }

    }

    class MyAdapter extends BaseAdapter {

        public MyAdapter(Context mContext) {
            super(mContext);

        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(mContext);
            } else {
                holder = (ViewHolder) convertView;
            }

            String msg = data.get(position);
            holder.txtName.setText(msg);
            return holder;
        }

        public class ViewHolder extends LinearLayout {
            public TextView txtName;
            public ViewHolder(Context context) {
                super(context);
                LayoutInflater li = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                li.inflate(R.layout.view_row_log, this, true);

                txtName = (TextView) findViewById(R.id.txtName);

            }
        }
    }
}
