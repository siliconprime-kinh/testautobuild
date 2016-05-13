package com.dropininc.model;

public class StartStreamModel extends BaseModel {
    public StreamModel stream;

    @Override
    public String toJSON() {
        return gson.toJson(this);
    }
}
