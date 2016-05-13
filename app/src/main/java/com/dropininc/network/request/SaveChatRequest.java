package com.dropininc.network.request;

public class SaveChatRequest {

    public String gig;
    public String from;
    public String message;

    public SaveChatRequest(String gig, String from, String message) {
        this.gig = gig;
        this.from = from;
        this.message = message;
    }
}
