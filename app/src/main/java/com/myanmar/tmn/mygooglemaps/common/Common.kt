package com.myanmar.tmn.mygooglemaps.common

import com.myanmar.tmn.mygooglemaps.remote.IGoogleService
import com.myanmar.tmn.mygooglemaps.remote.RetrofitClient

/**
 * Created by msi on 6/3/2018.
 */
object Common {
    private val GOOGLE_API_KEY = "https://maps.googleapis.com/"

    val iGoogleService:IGoogleService
    get() = RetrofitClient.getClient(GOOGLE_API_KEY).create(IGoogleService::class.java)
}