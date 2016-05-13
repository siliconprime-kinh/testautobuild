package com.dropininc.message;

import com.dropininc.AppApplication;
import com.dropininc.utils.Logs;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;

import java.util.HashSet;
import java.util.Set;


public class MessageManager implements SubscriptionEventListener {

    private final String TAG = "MessageManager";

    private static MessageManager instance;

    private Pusher mPusher;
    private Set<String> gigsSet = new HashSet<>();

    public static MessageManager getInstance() {
        if (instance == null) {
            instance = new MessageManager();
        }
        return instance;
    }

    private MessageManager() {
        mPusher = AppApplication.getInstance().getPusher();
    }


    public void subscribeToGigChannel(String channel) {
        Logs.log(TAG, "Subscribe to the channel: " + channel);
        try {
            Channel channelPusher = mPusher.subscribe(channel);
            channelPusher.bind("gig", this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        gigsSet.add(channel);
    }

    public void unsubscribeFromGigChannel(String channel) {
        Logs.log(TAG, "Unsubscribe from the channel: " + channel);
        if (channel != null) {
            mPusher.unsubscribe(channel);
            gigsSet.remove(channel);
        }
    }

    public void unsubscribeFromAllGigs() {
        for (String channel : gigsSet) {
            Logs.log(TAG, "Unsubscribe from the channel: " + channel);
            mPusher.unsubscribe(channel);
        }
        gigsSet.clear();
    }

    @Override
    public void onEvent(String channelName, String eventName, String data) {
        Logs.log(TAG, "successCallback mSubscriptionEventListener: " + channelName + "; message is " + data);
    }
}
