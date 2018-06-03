package com.myanmar.tmn.mygooglemaps.remote

import com.myanmar.tmn.mygooglemaps.model.MyPlaces
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Created by msi on 6/3/2018.
 */
interface IGoogleService {

    @GET
    fun getNearByServices(@Url url :String):Call<MyPlaces>
}