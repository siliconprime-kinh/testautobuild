package com.dropininc.network;

import com.dropininc.model.AccountModel;
import com.dropininc.model.AccountSettingModel;
import com.dropininc.model.AvatarModel;
import com.dropininc.model.ChatSaveModel;
import com.dropininc.model.ClaimModel;
import com.dropininc.model.ConfirmModel;
import com.dropininc.model.CountModel;
import com.dropininc.model.EndStreamModel;
import com.dropininc.model.GeneralModel;
import com.dropininc.model.HistoryItemModel;
import com.dropininc.model.LogModel;
import com.dropininc.model.LogPusher;
import com.dropininc.model.MessagesModel;
import com.dropininc.model.NewCardModel;
import com.dropininc.model.OperatorModel;
import com.dropininc.model.PaymentStatusModel;
import com.dropininc.model.RateStreamModel;
import com.dropininc.model.RecordModel;
import com.dropininc.model.ResumeCheckModel;
import com.dropininc.model.SearchModel;
import com.dropininc.model.SignupModel;
import com.dropininc.model.StartStreamModel;
import com.dropininc.model.TokenModel;
import com.dropininc.model.VerifyModel;
import com.dropininc.model.ViewerStartStreamModel;
import com.dropininc.network.request.AccountRequest;
import com.dropininc.network.request.ConfirmRequest;
import com.dropininc.network.request.DeviceTokenRequest;
import com.dropininc.network.request.FeedbackRequest;
import com.dropininc.network.request.LocationRequest;
import com.dropininc.network.request.LoginRequest;
import com.dropininc.network.request.RatingRequest;
import com.dropininc.network.request.SaveChatRequest;
import com.dropininc.network.request.UpdateOperatorProfileRequest;
import com.dropininc.network.request.VerifyTokenRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;


public interface IServiceApi {

    // AUTH
    @POST("login")
    Observable<TokenModel> logIn(@Body LoginRequest loginRequest);

    @POST("accounts")
    Observable<SignupModel> signUp(@Body AccountRequest accountRequest);

    @POST("login/verify")
    Observable<VerifyModel> verifyToken(@Body VerifyTokenRequest tokenRequest);

    @POST("devices")
    Observable<Object> addDeviceToken(@Body DeviceTokenRequest deviceRequest);

    //MESSAGES
    @GET("notifications")
    Observable<List<MessagesModel>> getInbox(@Query("archive") boolean archive,
                                             @Query("start") int start, @Query("limit") int limit);

    @DELETE("notifications/{id}")
    Observable<MessagesModel> archiveMessage(@Path("id") String messageId);

    @PUT("notifications/{id}")
    @FormUrlEncoded
    @Headers("X-HTTP-Method-Override: PATCH")
    Observable<Object> markMessageAsRead(@Path("id") String notificationId, @Field("status") String status);

    @GET("notifications/count?status=unread")
    Observable<CountModel> getNotificationCount();

    //LOG
    @POST("logs")
    Observable<Object> log(@Body LogModel logRequest);

    //PROFILE
    @GET("accountsetting")
    Observable<AccountSettingModel> getAccountSettings();

    @GET("operators/{id}")
    Observable<OperatorModel> checkOperatorProfile(@Path("id") String accountId);

    @GET("accounts/{id}")
    Observable<AccountModel> getProfile(@Path("id") String accountId);

    @Headers("X-HTTP-Method-Override: PATCH")
    @PUT("accounts/{id}")
    Observable<AccountModel> updateProfile(@Path("id") String accountId, @Body AccountRequest accountRequest);

    @DELETE("accounts/{id}")
    Observable<Object> deleteProfile(@Path("id") String accountId);

    @POST("accounts/{id}")
    Observable<Object> sendFeedback(@Body FeedbackRequest feedbackRequest);

    @POST("accountsetting")
    @FormUrlEncoded
    Observable<ArrayList<AccountSettingModel>> updateAccountSetting(@Field("detectRadius") int detectRadius);

    @POST("operators/skip")
    Observable<Object> setActiveOperator();

    @POST("accounts/:id/skipoperator")
    Observable<Object> skipDroperator(@Path("id") String accountId);

    @POST("logout")
    @FormUrlEncoded
    Observable<GeneralModel> logout(@Field("deviceAddress") String deviceAddress);

    @POST("storage")
    @FormUrlEncoded
    Observable<AvatarModel> getLinkUploadAvatar(@Field("type") String type);

    @POST("operators")
    Observable<Object> updateOperatorsProfile(@Body UpdateOperatorProfileRequest operatorProfileRequest);

    //PAYMENT
    @GET("paymentOptions")
    Observable<PaymentStatusModel> getPaymentOptions();

    @POST("paymentOptions")
    @FormUrlEncoded
    Observable<NewCardModel> addCreditCard(@Field("token") String token);

    @DELETE("paymentOptions/{id}")
    Observable<Object> removeCreditCard(@Path("id") String paymentOptionId);

    @PATCH("paymentOptions/{id}")
    @FormUrlEncoded
    Observable<Object> setDefaultCreditCard(@Path("id") String paymentOptionId,
                                            @Field("setAsDefault") int setAsDefault);

    @GET("payouts")
    Observable<ArrayList<HistoryItemModel>> getHistoryEarning(@Query("start") int start, @Query("limit") int limit,
                                                              @Query("sort") String sort,
                                                              @Query("account") String account);

    @GET("purchases")
    Observable<ArrayList<HistoryItemModel>> getHistoryPurchase(@Query("start") int start, @Query("limit") int limit,
                                                               @Query("sort") String sort,
                                                               @Query("account") String account);

    //MAP
    @GET("map/{lat}/{lng}")
    Observable<ArrayList<SearchModel>> searchOperator(@Path("lat") double lat, @Path("lng") double lng);

    @POST("gigs")
    @FormUrlEncoded
    Observable<ClaimModel> setOperatorDefault(@Field("latitude") double latitude,
                                              @Field("longitude") double longitude,
                                              @Field("metaData") String metadata);

    @POST("map")
    Observable<Object> location(@Body LocationRequest locationRequest);

    @POST("gigs/claim/{id}")
    @FormUrlEncoded
    Observable<ClaimModel> responseViewer(@Path("id") String gigsId, @Field("response") String response);

    @POST("gigs/start/{id}")
    Observable<ViewerStartStreamModel> startStream(@Path("id") String gigsId);

    @POST("gigs/handshake/{id}")
    Observable<StartStreamModel> handshakeStream(@Path("id") String gigsId);

    @POST("gigs/stop/{id}")
    Observable<EndStreamModel> endStream(@Path("id") String gigsId);

    @POST("gigs/cancel/{id}")
    Observable<EndStreamModel> cancelStream(@Path("id") String gigsId);

    @POST("gigs/rate/{id}")
    Observable<RateStreamModel> rateStream(@Path("id") String gigsId, @Body RatingRequest ratingRequest);

    @POST("gigs/confirm/{id}")
    Observable<ConfirmModel> confirmRequest(@Path("id") String gigsId, @Body ConfirmRequest confirmRequest);

    @POST("streams/start/{id}")
    Observable<RecordModel> startRecordVideo(@Path("id") String streamId);

    @POST("streams/stop/{id}")
    Observable<Object> stopRecordVideo(@Path("id") String streamId);

    @GET("gigs/status")
    Observable<AccountModel> checkGisStatus();

    // RESUME
    @GET("gigs/{accountId}/getall")
    Observable<ArrayList<ResumeCheckModel>> getAllGigs(@Path("accountId") String accountId, @Query("status") String status);

    @POST("chat/save")
    Observable<ChatSaveModel> saveChatHistory(@Body SaveChatRequest request);

    @GET("chat/{id}")
    Observable<ArrayList<ChatSaveModel>> getAllChat(@Path("id") String gigIds);

    @POST("notification/logs")
    Observable<Object> logPusher(@Body LogPusher logPusher);


}
