package com.dropininc.model;

public class HistoryPaymentModel extends BaseModel {
    public String id;
    public String amount;
    public String createdAt;

    public static HistoryPaymentModel fromJSON(String json) {
        return gson.fromJson(json, HistoryPaymentModel.class);
    }
}
