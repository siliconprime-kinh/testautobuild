package com.dropininc.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.dialog.ChatDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.HistoryItemModel;
import com.dropininc.utils.FontUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class HistoryAdapter extends BaseAdapter {

    private final String dateFormat = "MM/dd/yyyy  hh:mm a";

    private ArrayList<HistoryItemModel> mObjects = new ArrayList<>();
    private int type = 0;

    private DisplayImageOptions mOptions;

    public HistoryAdapter(Context mContext) {
        super(mContext);

        mOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_avatar)
                .showImageOnFail(R.drawable.ic_avatar)
                .resetViewBeforeLoading(true).cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
                .build();
    }

    public void setData(ArrayList<HistoryItemModel> data, int type) {
        mObjects = data;
        this.type = type;
        notifyDataSetChanged();
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<HistoryItemModel> getData() {
        return mObjects;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public HistoryItemModel getItem(int position) {
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
            holder = new ViewHolder(mContext);
        } else {
            holder = (ViewHolder) convertView;
        }

        HistoryItemModel model = mObjects.get(position);
        Log.e("asd", "date = " + model.payment.createdAt);

        // type = 0 is purchase list
        if (type == 0) {
            holder.imgRate.setVisibility(View.VISIBLE);
            holder.img_avatar.setImageResource(R.drawable.ic_avatar);
            holder.img_chat_history.setVisibility(View.VISIBLE);

            int rate = model.rating.value;
            if (rate == 0) {
                holder.imgRate.setImageResource(R.drawable.ic_his_rate_group_0);
            } else if (rate == 1) {
                holder.imgRate.setImageResource(R.drawable.ic_his_rate_group_1);
            } else if (rate == 2) {
                holder.imgRate.setImageResource(R.drawable.ic_his_rate_group_2);
            } else if (rate == 3) {
                holder.imgRate.setImageResource(R.drawable.ic_his_rate_group_3);
            } else if (rate == 4) {
                holder.imgRate.setImageResource(R.drawable.ic_his_rate_group_4);
            } else if (rate == 5) {
                holder.imgRate.setImageResource(R.drawable.ic_his_rate_group_5);
            }
            if (!TextUtils.isEmpty(model.account.profileImage)) {
                mImageLoader.displayImage(model.account.profileImage, holder.img_avatar, mOptions);
            }
        } else {
            holder.imgRate.setVisibility(View.GONE);
            holder.img_avatar.setImageResource(R.drawable.ic_launcher);
            holder.img_chat_history.setVisibility(View.GONE);
        }

        holder.txtDate.setText(formatStringDate(model.payment.createdAt, dateFormat));
        holder.txtName.setText(model.account.firstName + " " + model.account.lastName);
        holder.txtPrice.setText("$" + model.payment.amount);
        holder.img_chat_history.setTag(model);
        holder.txtLocation.setTag(model);
        holder.txtGPS.setTag(model);

        holder.txtLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.img_history_location_gray, 0, 0, 0);
        holder.txtGPS.setCompoundDrawablesWithIntrinsicBounds(R.drawable.img_history_gps_gray, 0, 0, 0);
        holder.txtLocation.setTextColor(mContext.getResources().getColor(R.color.text_gray_custom));
        holder.txtGPS.setTextColor(mContext.getResources().getColor(R.color.text_gray_custom));
        if (model.isExpandLocation) {
            holder.txtDes.setVisibility(View.VISIBLE);
            holder.txtLocation.setTextColor(mContext.getResources().getColor(R.color.text_green));
            holder.txtLocation.setCompoundDrawablesWithIntrinsicBounds(R.drawable.img_history_location, 0, 0, 0);
            if (model.gig.metaData != null && !TextUtils.isEmpty(model.gig.metaData.address)) {
                holder.txtDes.setText(model.gig.metaData.address);
            } else {
                holder.txtDes.setText("");
            }
        } else if (model.isExpandGPS) {
            holder.txtDes.setVisibility(View.VISIBLE);
            holder.txtGPS.setTextColor(mContext.getResources().getColor(R.color.text_green));
            holder.txtGPS.setCompoundDrawablesWithIntrinsicBounds(R.drawable.img_history_gps, 0, 0, 0);
            String text = "Lat: " + String.format("%.6f", model.gig.latitude) + " | " + "Long: " + String.format("%.6f", model.gig.longitude);
            holder.txtDes.setText(text.trim());
        } else {
            holder.txtDes.setVisibility(View.GONE);
        }

        holder.img_chat_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryItemModel model = (HistoryItemModel) v.getTag();
                final ChatDialog chatDialog = new ChatDialog(mContext, android.R.style.Theme_Translucent);
                String name = model.account.firstName + " " + model.account.lastName;
                chatDialog.setBackgroundTransparent(false);
                chatDialog.show();
                chatDialog.setIsViewHistory(model.gig.chatChannel, name, model.gig.id);
            }
        });

        holder.txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryItemModel model = (HistoryItemModel) v.getTag();
                boolean isExpandLocation = model.isExpandLocation;
                model.isExpandLocation = !isExpandLocation;
                model.isExpandGPS = false;
                notifyDataSetChanged();
            }
        });
        holder.txtGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryItemModel model = (HistoryItemModel) v.getTag();
                boolean isExpandGPS = model.isExpandGPS;
                model.isExpandGPS = !isExpandGPS;
                model.isExpandLocation = false;
                notifyDataSetChanged();
            }
        });


        return holder;
    }

    public class ViewHolder extends LinearLayout {
        public TextView txtName, txtDate, txtPrice, txtLocation, txtGPS, txtDes;
        public ImageView img_avatar, imgRate, img_chat_history;

        public ViewHolder(Context context) {
            super(context);
            LayoutInflater li = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            li.inflate(R.layout.view_row_history_new, this, true);

            txtName = (TextView) findViewById(R.id.txtName);
            txtDate = (TextView) findViewById(R.id.txtDate);
            txtPrice = (TextView) findViewById(R.id.txtPrice);
            img_chat_history = (ImageView) findViewById(R.id.img_chat_history);

            txtLocation = (TextView) findViewById(R.id.txtLocation);
            txtGPS = (TextView) findViewById(R.id.txtGPS);
            txtDes = (TextView) findViewById(R.id.txtDes);
            img_avatar = (ImageView) findViewById(R.id.img_avatar);
            imgRate = (ImageView) findViewById(R.id.imgRate);

            FontUtils.typefaceTextView(txtName, FontType.REGULAR);
            FontUtils.typefaceTextView(txtDate, FontType.LIGHT);
            FontUtils.typefaceTextView(txtPrice, FontType.REGULAR);
            FontUtils.typefaceTextView(txtDes, FontType.LIGHT);
            FontUtils.typefaceTextView(txtLocation, FontType.LIGHT);
            FontUtils.typefaceTextView(txtGPS, FontType.LIGHT);
        }
    }

    public static String formatStringDate(String time, String format) {
        if (TextUtils.isEmpty(time)) return "";

        String finalDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date myDate = null;
        try {
            myDate = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat(format);
        timeFormat.setTimeZone(TimeZone.getDefault());
        finalDate = timeFormat.format(myDate);

        return finalDate;
    }
}
