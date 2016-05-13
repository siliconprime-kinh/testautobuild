package com.dropininc.model;

import java.util.ArrayList;

/**
 * Created by sondinh on 10/14/2015.
 */
public class PaymentStatusModel extends BaseModel {
    public String code;
    public ArrayList<PaymentModel> payments;

    public static PaymentStatusModel fromJSON(String json){
        return gson.fromJson(json, PaymentStatusModel.class);
    }
}
