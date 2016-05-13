package com.dropininc.model;

import com.google.gson.Gson;

import java.io.Serializable;

public abstract class BaseModel implements Serializable{

    protected final static Gson gson = new Gson();

	public BaseModel() {
	}

    public String toJSON() {
        return gson.toJson(this);
    }
}
