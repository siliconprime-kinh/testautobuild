package com.dropininc.model;

public class ConfigModel extends BaseModel {

    public class PubnubKey {
        public String publishKey;
        public String subscribeKey;
    }

    public class OpentokKey {
        public String key;
        public String secret;
    }

    public class StripeKey {
        public String testPublishKey;
        public String livePublishKey;
    }

    public PubnubKey pubnub;
    public OpentokKey opentok;
    public StripeKey stripe;

    public static ConfigModel fromJSON(String json) {
        return gson.fromJson(json, ConfigModel.class);
    }
}


