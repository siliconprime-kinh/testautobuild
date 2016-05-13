package com.dropininc.network;

import com.dropininc.model.MapDirectionModel;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface IGoogleMapsApi {

    @GET("directions/json?sensor=false&units=metric&mode=driving")
    Observable<MapDirectionModel> getDirections(@Query("origin") String origin,
                                     @Query("destination") String destination);
}
