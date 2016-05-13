package com.dropininc.model;

public class MessagesDataModel extends BaseModel {
    public int code;
    public String id;
    public String messageId;
    public String firstName;
    public String lastName;
    public String title;
    public String message;
    public String accountId;

    public static MessagesDataModel fromJSON(String json) {
        return gson.fromJson(json, MessagesDataModel.class);
    }
}
