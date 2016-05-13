package com.dropininc.model;

public class ResumeCheckModel extends BaseModel {

    public Profile operator;
    public Profile customer;

    public Rating operatorRating;
    public Rating customerRating;

    public int duration;
    public String operatorPrice;
    public String customerPrice;
    public String id;
    public double latitude;
    public double longitude;
    public String status;
    public String chatChannel;
    public MetaData metaData;
    public String stream;

    public class MetaData {

        public String address;
        public OperatorData operatorLastData;
        public OperatorData operatorData;
        public Stream stream;

        public class OperatorData {
            public double preciseLatitude;
            public double preciseLongitude;
            public String claimed;
        }

        public class Stream {
            public String operatorStreamToken;
            public String customerStreamToken;
            public String key;
            public String sessionId;
        }
    }

    public class Profile {

        public String firstName;
        public String lastName;
        public String id;
        public String type;
        public AvatarModel profileImage;
        public double operatorRating;

    }

    public class Rating {
        public String id;
    }

    public static ResumeCheckModel fromJSON(String json) {
        return gson.fromJson(json, ResumeCheckModel.class);
    }
}


