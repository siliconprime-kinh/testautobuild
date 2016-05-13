package com.dropininc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.utils.FontUtils;

import org.json.JSONArray;
import org.json.JSONException;


public class TutorialAdapter extends BaseAdapter {
    Context mContext;
    JSONArray arrayItems;

    public TutorialAdapter(JSONArray arrayItems, Context mContext) {
        super();
        this.mContext = mContext;
        this.arrayItems = arrayItems;
    }


    @Override
    public int getCount() {
        return arrayItems.length();
    }

    @Override
    public Object getItem(int position) {
        try {
            return arrayItems.get(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.view_tutorial,
                    null);
        }
        ImageView imgView = (ImageView) convertView
                .findViewById(R.id.imgBg);
        TextView tutorial_mess = (TextView) convertView
                .findViewById(R.id.tutorial_mess);
        FontUtils.typefaceTextView(tutorial_mess, FontType.REGULAR);
        String text = "";
        int resourceId = 0;
        if (position == 0) {
            resourceId = R.drawable.bg_tutorial_1;
            text = mContext.getString(R.string.text_1);
        } else if (position == 1) {
            resourceId = R.drawable.bg_tutorial_2;
            text = mContext.getString(R.string.text_2);
        } else if (position == 2) {
            resourceId = R.drawable.bg_tutorial_4;
            text = mContext.getString(R.string.text_4);
        } else {
//            resourceId = R.drawable.bg_tutorial_3;
//            text = mContext.getString(R.string.text_3);
        }
        imgView.setImageResource(resourceId);
        tutorial_mess.setText(text);


        return convertView;
    }

    public interface OnItemFeatureClick {
        public void onItemFeatureClick(int position);
    }

}
