package com.dropininc.model;

public class RecordModel extends BaseModel {
    public String name;
    public String archiveId;
    public String account;
    public String duration;
    public String size;
    public String status;
    public String stream;
    public String archive;
    public String createdAt;
    public String updatedAt;
    public String id;

    public static RecordModel fromJSON(String json) {
        return gson.fromJson(json, RecordModel.class);
    }
}
