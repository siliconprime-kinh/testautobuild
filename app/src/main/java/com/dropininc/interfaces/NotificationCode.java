package com.dropininc.interfaces;


public interface NotificationCode {
    int STREAM_START = 2;
    int STREAM_FINISH = 3;
    int REQUEST_OPERATOR = 4;
    int ACCEPT_DENY_REQUEST = 5;
    int DOCUMENT_SIGN = 6;
    int OPERATOR_NOT_FOUND = 7;
    int NEW_LOCATION = 8;
    int NEW_OPERATOR = 9;
    int SWITCH_MODE = 10;
    int EN_ROUTE = 11;
    int VIEWER_REJECTED = 12;
    int CANCELED = 13;
    int STREAM_EXPIRE = 14;
    int DOCUMENT_SIGN_CANCEL = 18;
    int TESTING_PING = -2016;
}
