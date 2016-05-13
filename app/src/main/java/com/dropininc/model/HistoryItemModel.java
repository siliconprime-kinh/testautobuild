package com.dropininc.model;

public class HistoryItemModel extends BaseModel {
    public GigModel gig;
    public HistoryPaymentModel payment;
    public HistoryRateModel rating;
    public HistoryAccountModel account;
    public boolean isExpandLocation = false;
    public boolean isExpandGPS = false;

    public static HistoryItemModel fromJSON(String json) {
        return gson.fromJson(json, HistoryItemModel.class);
    }


}
