package com.dropininc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.utils.FontUtils;

import java.util.ArrayList;


public class ListDialogAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<String> mObjects;
    int currentIndex;

    public ListDialogAdapter(Context mContext, ArrayList<String> mObjects, int currentIndex) {
        super(mContext);
        layoutInflater = LayoutInflater.from(mContext);
        this.mObjects = mObjects;
        this.currentIndex = currentIndex;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public String getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.view_row_list_dialog, null);
            holder = new ViewHolder();
            holder.mLayAction = (LinearLayout) convertView.findViewById(R.id.lay_action);
            holder.mTextViewName = (TextView) convertView.findViewById(R.id.tv_name);

            FontUtils.typefaceTextView(holder.mTextViewName, FontType.REGULAR);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String item = mObjects.get(position);
        holder.mTextViewName.setText(item);

        if (currentIndex != -1) {
            if (currentIndex == position) {
                holder.mLayAction.setSelected(true);
            } else {
                holder.mLayAction.setSelected(false);
            }
        }

        return convertView;
    }

    private class ViewHolder {
        public LinearLayout mLayAction;
        public TextView mTextViewName;
    }
}
