package com.dropininc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.adapter.DirectionAdapter;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.MapDirectionModel;
import com.dropininc.utils.FontUtils;

import java.text.DecimalFormat;
import java.util.List;

public class DirectionDialog extends Dialog {
    private Context mContext;
    private TextView mTextTitle, mTextValue;
    private Button mButtonDone;
    private ListView mListView;
    private DirectionAdapter mAdapter;

    public DirectionDialog(Context mContext) {
        super(mContext, android.R.style.Theme_Translucent);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.mContext = mContext;

        setContentView(R.layout.dialog_direction);

        mTextTitle = (TextView) findViewById(R.id.tv_title);
        mTextValue = (TextView)findViewById(R.id.tv_value);
        mButtonDone = (Button) findViewById(R.id.bt_done);
        mListView = (ListView) findViewById(R.id.lv_content);

        mButtonDone.setOnClickListener(v -> dismiss());

        FontUtils.typefaceTextView(mTextTitle, FontType.REGULAR);
        FontUtils.typefaceTextView(mTextValue, FontType.REGULAR);
        FontUtils.typefaceButton(mButtonDone, FontType.BOLD);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public void setData(List<MapDirectionModel.Step> mData){
        mAdapter = new DirectionAdapter(mContext, mData);
        mListView.setAdapter(mAdapter);
    }

    public void setDistance(float mDistance){
        DecimalFormat formatter = new DecimalFormat("#.#");
        String textDistance = formatter.format(mDistance);
        String text = mContext.getString(R.string.distance) + ": " + textDistance + " Mi";
        mTextValue.setText(text);
    }
}
