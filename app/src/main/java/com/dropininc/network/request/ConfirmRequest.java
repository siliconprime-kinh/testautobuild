package com.dropininc.network.request;

public class ConfirmRequest {

    public String paymentMethodNonce;

    public ConfirmRequest(String paymentMethodNonce) {
        this.paymentMethodNonce = paymentMethodNonce;
    }

}
