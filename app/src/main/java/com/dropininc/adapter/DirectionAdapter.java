package com.dropininc.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.MapDirectionModel;
import com.dropininc.utils.FontUtils;

import java.text.DecimalFormat;
import java.util.List;


public class DirectionAdapter extends BaseAdapter {
    private List<MapDirectionModel.Step> mObjects;
    private DecimalFormat mFormatter = new DecimalFormat("#.#");

    public DirectionAdapter(Context mContext, List<MapDirectionModel.Step> mObjects) {
        super(mContext);
        this.mObjects = mObjects;
    }

    public List<MapDirectionModel.Step> getData() {
        return mObjects;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public MapDirectionModel.Step getItem(int position) {
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

        MapDirectionModel.Step model = mObjects.get(position);
        int distanceValue = model.distance.value;
        distanceValue = (int) (distanceValue * 3.28084);
        if (distanceValue < 1000) {
            holder.tv_distance.setText(distanceValue + " Ft");
        } else {
            double mile = distanceValue * 0.000621371;
            holder.tv_distance.setText(mFormatter.format(mile) + " Mi");
        }
        String html = model.instructions.replace("<div style=\"font-size:0.9em\">", "<br/>");
        html = html.replace("</div>", "");
        holder.tv_address.setText(Html.fromHtml(html));

        if (position == 0 || position == mObjects.size() - 1) {
            if (position == 0) {
                holder.img_location.setImageResource(R.drawable.ic_location_green);
            } else {
                holder.img_location.setImageResource(R.drawable.ic_location);
            }
            holder.img_location.setVisibility(View.VISIBLE);
            holder.tv_distance.setVisibility(View.GONE);
        } else {
            holder.img_location.setVisibility(View.GONE);
            holder.tv_distance.setVisibility(View.VISIBLE);
        }

        return holder;
    }

    public class ViewHolder extends LinearLayout {
        public TextView tv_distance, tv_address;
        public ImageView img_location;

        public ViewHolder(Context context) {
            super(context);
            LayoutInflater li = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            li.inflate(R.layout.view_item_direction, this, true);

            tv_distance = (TextView) findViewById(R.id.tv_distance);
            tv_address = (TextView) findViewById(R.id.tv_address);
            img_location = (ImageView) findViewById(R.id.img_location);

            FontUtils.typefaceTextView(tv_distance, FontType.BOLD);
            FontUtils.typefaceTextView(tv_address, FontType.BOLD);

        }
    }
}
