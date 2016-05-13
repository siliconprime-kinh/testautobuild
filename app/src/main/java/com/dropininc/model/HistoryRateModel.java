package com.dropininc.model;

public class HistoryRateModel extends BaseModel {
    public int value;

    public static HistoryRateModel fromJSON(String json) {
        return gson.fromJson(json, HistoryRateModel.class);
    }
}
