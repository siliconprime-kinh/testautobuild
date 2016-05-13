package com.dropininc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dropininc.R;
import com.dropininc.adapter.ListDialogAdapter;

import java.util.ArrayList;


public abstract class ListDialog extends Dialog implements AdapterView.OnItemClickListener {

    private ArrayList<String> mItems;
    private ListView listView;
    int currentIndex;

    public ListDialog(Context context, ArrayList<String> mItems, int currentIndex) {
        super(context);
        this.mItems = mItems;
        this.currentIndex = currentIndex;
    }

    public abstract void onClick(int index);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.view_list_dialog);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        ListDialogAdapter mAdapter = new ListDialogAdapter(getContext(), mItems, currentIndex);
        listView.setAdapter(mAdapter);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int marginWindow = (int) getContext().getResources().getDimension(R.dimen.dialog_margin_window);
        int heightRow = marginWindow * 5;
        int heightDisplay = displayMetrics.heightPixels - marginWindow * 20;
        int numberRowCanDisplay = heightDisplay / heightRow;

        if (mItems.size() > numberRowCanDisplay) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = getWindow();

            int margin_width = (int) getContext().getResources().getDimension(R.dimen.dialog_margin_window);
            int margin_height = (int) getContext().getResources().getDimension(R.dimen.dialog_margin_window);
            lp.copyFrom(window.getAttributes());
            lp.width = displayMetrics.widthPixels - margin_width * 6;
            lp.height = displayMetrics.heightPixels - margin_height * 20;
//        lp.dimAmount = 0.85f;
            window.setAttributes(lp);
        }

        Log.d("TAG", "current Index" + currentIndex);
        if (currentIndex != -1) {
            timerDelayRunForScroll(currentIndex, 100);
        }
    }

    public void timerDelayRunForScroll(final int index, long time) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                try {
                    listView.smoothScrollToPosition(index);
                } catch (Exception e) {
                }
            }
        }, time);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        onClick(position);
        dismiss();
    }
}
