package com.dropininc.network.request;


public class VerifyTokenRequest {

    public String tokenId;
    public String code;

    public VerifyTokenRequest(String tokenId, String code) {
        this.tokenId = tokenId;
        this.code = code;
    }

}
