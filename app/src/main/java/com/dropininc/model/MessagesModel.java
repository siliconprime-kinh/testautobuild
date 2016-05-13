package com.dropininc.model;

public class MessagesModel extends BaseModel {
    public SenderModel sender;
    public RecipientModel recipient;
    public MessagesDataModel data;
    public String id;
    public String status;
    public boolean archive;
    public String type;
    public String createdAt;
    public String updatedAt;
    public int code;
    public String title;
    public String message;
    public String publicId;
    public boolean isUserNotification;

    public static MessagesModel fromJSON(String json) {
        return gson.fromJson(json, MessagesModel.class);
    }
}
