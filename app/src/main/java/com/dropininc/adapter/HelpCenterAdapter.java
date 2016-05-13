package com.dropininc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.utils.FontUtils;
import com.zendesk.sdk.model.helpcenter.Article;

import java.util.ArrayList;


public class HelpCenterAdapter extends BaseAdapter {

    private ArrayList<Article> mObjects = new ArrayList<>();

    public HelpCenterAdapter(Context mContext) {
        super(mContext);
    }

    public void setData(ArrayList<Article> data) {
        mObjects = data;
        notifyDataSetChanged();
    }

    public ArrayList<Article> getData() {
        return mObjects;
    }


    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public Article getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder(mContext);
        } else {
            holder = (ViewHolder) convertView;
        }
        Article model = mObjects.get(position);
        holder.txtTitle.setText(model.getTitle());
        return holder;
    }

    public class ViewHolder extends LinearLayout {
        public RelativeLayout lay_parent;
        public TextView txtTitle;
        public ImageView iconArrow;

        public ViewHolder(Context context) {
            super(context);
            LayoutInflater li = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            li.inflate(R.layout.view_row_question, this, true);

            lay_parent = (RelativeLayout) findViewById(R.id.lay_parent);
            txtTitle = (TextView) findViewById(R.id.txtTitle);
            iconArrow = (ImageView) findViewById(R.id.iconArrow);

            FontUtils.typefaceTextView(txtTitle, FontType.REGULAR);
        }
    }
}
