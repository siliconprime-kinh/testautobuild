package com.dropininc.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dropininc.R;
import com.dropininc.customview.CircleImageView;
import com.dropininc.dialog.ImageZoomDialog;
import com.dropininc.interfaces.FontType;
import com.dropininc.model.ChatModel;
import com.dropininc.utils.FontUtils;
import com.dropininc.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;


public class ChatAdapter extends ArrayAdapter<ChatModel> {

    private Context mContext;

    private ImageLoader mImageLoader;
    private DisplayImageOptions mOptions;
    private String myUserId = "";
    private String avatar = "";
    private boolean isTransparent = false;

    public interface NotifyDataSelect {
        void onNotify(double total);
    }

    public ChatAdapter(Context mContext) {
        super(mContext, 0);
        this.mContext = mContext;
        mImageLoader = ImageLoader.getInstance();
        mOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(false).cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY).cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
                .build();
    }

    public void setMyUserId(String myUserId) {
        this.myUserId = myUserId;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        notifyDataSetChanged();
    }

    public void setTransparent(boolean isTransparent) {
        this.isTransparent = isTransparent;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder(mContext);
        } else {
            holder = (ViewHolder) convertView;
        }
        ChatModel model = getItem(position);

        if (model.userId.equals(myUserId)) {// my messages sent
            holder.linear_receive.setVisibility(View.GONE);
            holder.linear_sender.setVisibility(View.VISIBLE);

            holder.tv_time_sender.setText(Utils.getStringTimeFormat((long) model.date));

            if (model.isPhoto) {
                holder.tv_message_sender.setVisibility(View.GONE);
                holder.img_photo_sender.setVisibility(View.VISIBLE);
                mImageLoader.displayImage(model.messageContent, holder.img_photo_sender, mOptions);
                holder.img_photo_sender.setTag(model);
            } else {
                holder.tv_message_sender.setVisibility(View.VISIBLE);
                holder.img_photo_sender.setVisibility(View.GONE);

                holder.tv_message_sender.setText(model.messageContent);
            }

        } else {
            holder.linear_receive.setVisibility(View.VISIBLE);
            holder.linear_sender.setVisibility(View.GONE);

            holder.tv_time_receive.setText(Utils.getStringTimeFormat((long) model.date));

            if (TextUtils.isEmpty(avatar)) {
                holder.img_avatar.setImageResource(R.drawable.ic_avatar);
            } else {
                mImageLoader.displayImage(avatar, holder.img_avatar, mOptions);
            }

            if (model.isPhoto) {
                holder.tv_message_receive.setVisibility(View.GONE);
                holder.img_photo_receive.setVisibility(View.VISIBLE);
                mImageLoader.displayImage(model.messageContent, holder.img_photo_receive, mOptions);
                holder.img_photo_receive.setTag(model);
            } else {
                holder.tv_message_receive.setVisibility(View.VISIBLE);
                holder.img_photo_receive.setVisibility(View.GONE);
                holder.tv_message_receive.setText(model.messageContent);
            }

        }
        holder.img_photo_receive.setOnClickListener(mOnClickListener);
        holder.img_photo_sender.setOnClickListener(mOnClickListener);

        if (isTransparent) {
            holder.tv_time_receive.setTextColor(mContext.getResources().getColor(R.color.text_gray_hint));
            holder.tv_time_sender.setTextColor(mContext.getResources().getColor(R.color.text_gray_hint));
        } else {
            holder.tv_time_receive.setTextColor(mContext.getResources().getColor(R.color.text_gray_custom));
            holder.tv_time_sender.setTextColor(mContext.getResources().getColor(R.color.text_gray_custom));
        }

        return holder;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ChatModel model = (ChatModel) v.getTag();
            ImageZoomDialog dialog = new ImageZoomDialog(mContext);
            dialog.setImageResource(model.messageContent);
            dialog.show();
        }
    };

    public class ViewHolder extends LinearLayout {
        public LinearLayout linear_receive;
        public CircleImageView img_avatar;
        public TextView tv_message_receive;
        public TextView tv_time_receive;
        public ImageView img_photo_receive;

        public LinearLayout linear_sender;
        public TextView tv_message_sender;
        public TextView tv_time_sender;
        public ImageView img_photo_sender;

        public ViewHolder(Context context) {
            super(context);
            LayoutInflater li = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            li.inflate(R.layout.view_row_chat, this, true);

            linear_receive = (LinearLayout) findViewById(R.id.linear_receive);
            img_avatar = (CircleImageView) findViewById(R.id.img_avatar);
            tv_message_receive = (TextView) findViewById(R.id.tv_message_receive);
            tv_time_receive = (TextView) findViewById(R.id.tv_time_receive);
            img_photo_receive = (ImageView) findViewById(R.id.img_photo_receive);

            linear_sender = (LinearLayout) findViewById(R.id.linear_sender);
            tv_message_sender = (TextView) findViewById(R.id.tv_message_sender);
            tv_time_sender = (TextView) findViewById(R.id.tv_time_sender);
            img_photo_sender = (ImageView) findViewById(R.id.img_photo_sender);

            FontUtils.typefaceTextView(tv_message_receive, FontType.REGULAR);
            FontUtils.typefaceTextView(tv_time_receive, FontType.LIGHT);
            FontUtils.typefaceTextView(tv_message_sender, FontType.REGULAR);
            FontUtils.typefaceTextView(tv_time_sender, FontType.LIGHT);
        }
    }
}
