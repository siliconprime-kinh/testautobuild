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
import com.dropininc.model.MapDirectionModel;
import com.dropininc.model.MessagesModel;
import com.dropininc.model.NewCardModel;
import com.dropininc.model.OperatorModel;
import com.dropininc.model.PaymentStatusModel;
import com.dropininc.model.RateStreamModel;
import com.dropininc.model.RecordModel;
import com.dropininc.model.ResumeCheckModel;
import com.dropininc.model.RetrofitErrorModel;
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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Singleton
public class NetworkManager {

    public static final String TAG = NetworkManager.class.getSimpleName();

    private IServiceApi serviceApi;
    private IGoogleMapsApi googleMapsApi;
    private Converter<ResponseBody, RetrofitErrorModel[]> converter;

    @Inject
    public NetworkManager(IServiceApi serviceApi, IGoogleMapsApi googleMapsApi, Retrofit retrofit) {
        this.serviceApi = serviceApi;
        this.googleMapsApi = googleMapsApi;
        this.converter = retrofit.responseBodyConverter(RetrofitErrorModel[].class, new Annotation[0]);
    }

    public RetrofitErrorModel parseError(Throwable throwable) {
        RetrofitErrorModel error;
        if (!(throwable instanceof HttpException)) {
            // KINH return RetrofitErrorModel.defaultError();
            return RetrofitErrorModel.defaultError(throwable); /*KINH test*/
        }

        Response response = ((HttpException) throwable).response();

        try {
            error = converter.convert(response.errorBody())[0];
        } catch (Exception e) {
//            e.printStackTrace();
            if (response.code() == 500) {
                error = new RetrofitErrorModel("WARNING", "Internal Server Error");
            } else {
                //KINH  error = RetrofitErrorModel.defaultError();
                error = RetrofitErrorModel.defaultError(response); /*KINH TEST*/
            }
        }
        return error;
    }

    // AUTH
    public Observable<TokenModel> logIn(LoginRequest loginRequest) {
        return serviceApi.logIn(loginRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<SignupModel> signUp(AccountRequest accountRequest) {
        return serviceApi.signUp(accountRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<VerifyModel> verifyToken(VerifyTokenRequest tokenRequest) {
        return serviceApi.verifyToken(tokenRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> addDeviceToken(DeviceTokenRequest deviceRequest) {
        return serviceApi.addDeviceToken(deviceRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    // MESSAGES
    public Observable<List<MessagesModel>> getInbox(boolean archive, int start, int limit) {
        return serviceApi.getInbox(archive, start, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<MessagesModel> archiveMessage(String messageId) {
        return serviceApi.archiveMessage(messageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> markMessageAsRead(String notificationId) {
        return serviceApi.markMessageAsRead(notificationId, "received")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<CountModel> getNotificationCount() {
        return serviceApi.getNotificationCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //LOG
    public Observable<Object> log(LogModel logRequest) {
        return serviceApi.log(logRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //PROFILE
    public Observable<AccountSettingModel> getAccountSettings() {
        return serviceApi.getAccountSettings()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<OperatorModel> checkOperatorProfile(String accountId) {
        return serviceApi.checkOperatorProfile(accountId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<AccountModel> getProfile(String accountId) {
        return serviceApi.getProfile(accountId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<AccountModel> updateProfile(String accountId, AccountRequest accountRequest) {
        return serviceApi.updateProfile(accountId, accountRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<Object> deleteProfile(String accountId) {
        return serviceApi.deleteProfile(accountId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> sendFeedback(FeedbackRequest feedbackRequest) {
        return serviceApi.sendFeedback(feedbackRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ArrayList<AccountSettingModel>> updateAccountSetting(int detectRadius) {
        return serviceApi.updateAccountSetting(detectRadius)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> setActiveOperator() {
        return serviceApi.setActiveOperator()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> skipDroperator(String accountId) {
        return serviceApi.skipDroperator(accountId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<GeneralModel> logout(String deviceAddress) {
        return serviceApi.logout(deviceAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<AvatarModel> getLinkUploadAvatar() {
        return serviceApi.getLinkUploadAvatar("profileImage")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<AvatarModel> getLinkUploadScreenshot() {
        return serviceApi.getLinkUploadAvatar("chat")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> updateOperatorsProfile(UpdateOperatorProfileRequest operatorProfileRequest) {
        return serviceApi.updateOperatorsProfile(operatorProfileRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //PAYMENT
    public Observable<PaymentStatusModel> getPaymentOptions() {
        return serviceApi.getPaymentOptions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<NewCardModel> addCreditCard(String token) {
        return serviceApi.addCreditCard(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> removeCreditCard(String paymentOptionId) {
        return serviceApi.removeCreditCard(paymentOptionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> setDefaultCreditCard(String paymentOptionId) {
        return serviceApi.setDefaultCreditCard(paymentOptionId, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ArrayList<HistoryItemModel>> getHistoryEarning(int start, int limit, String sort,
                                                                     String account) {
        return serviceApi.getHistoryEarning(start, limit, sort, account)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ArrayList<HistoryItemModel>> getHistoryPurchase(int start, int limit, String sort,
                                                                      String account) {
        return serviceApi.getHistoryPurchase(start, limit, sort, account)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //MAP
    public Observable<ArrayList<SearchModel>> searchOperator(double lat, double lng) {
        return serviceApi.searchOperator(lat, lng)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ClaimModel> setOperatorDefault(double latitude, double longitude, String metaData) {
        return serviceApi.setOperatorDefault(latitude, longitude, metaData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> location(LocationRequest locationRequest) {
        return serviceApi.location(locationRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ClaimModel> responseViewer(String gigsId, String response) {
        return serviceApi.responseViewer(gigsId, response)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ViewerStartStreamModel> startStream(String gigsId) {
        return serviceApi.startStream(gigsId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<StartStreamModel> handshakeStream(String gigsId) {
        return serviceApi.handshakeStream(gigsId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<EndStreamModel> endStream(String gigsId) {
        return serviceApi.endStream(gigsId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<EndStreamModel> cancelStream(String gigsId) {
        return serviceApi.cancelStream(gigsId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<RateStreamModel> rateStream(String gigsId, RatingRequest ratingRequest) {
        return serviceApi.rateStream(gigsId, ratingRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ConfirmModel> confirmRequest(String gigsId) {
        return serviceApi.confirmRequest(gigsId, new ConfirmRequest("dummyNonce"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<RecordModel> startRecordVideo(String streamId) {
        return serviceApi.startRecordVideo(streamId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> stopRecordVideo(String streamId) {
        return serviceApi.stopRecordVideo(streamId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<AccountModel> checkGisStatus() {
        return serviceApi.checkGisStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    // RESUME
    public Observable<ArrayList<ResumeCheckModel>> getAllGigs(String accountId, String status) {
        return serviceApi.getAllGigs(accountId, status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    //MAPS
    public Observable<MapDirectionModel> getDirections(double originLat, double originLng, double destLat,
                                                       double destLng) {
        return googleMapsApi.getDirections(String.valueOf(originLat) + "," + String.valueOf(originLng),
                String.valueOf(destLat) + "," + String.valueOf(destLng))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ChatSaveModel> saveChatHistory(SaveChatRequest request) {
        return serviceApi.saveChatHistory(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ArrayList<ChatSaveModel>> getAllChat(String gigID) {
        return serviceApi.getAllChat(gigID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Object> logPusher(LogPusher logPusher) {
        return serviceApi.logPusher(logPusher)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
