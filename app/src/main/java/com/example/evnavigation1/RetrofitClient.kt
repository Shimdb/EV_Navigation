package com.example.evnavigation1

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TMapApiService {
    @GET("tmap/pois")
    suspend fun searchPoi(
        @Header("appKey") appKey: String = "sDAEBi80t588hNSM41CIX2KePKM5SBViabdydGZG",
        @Query("version") version: Int = 1,
        @Query("searchKeyword") keyword: String,
        @Query("resCoordType") resCoordType: String = "WGS84GEO", // 위경도 좌표계로 직접 수신
        @Query("reqCoordType") reqCoordType: String = "WGS84GEO",
        @Query("count") count: Int = 20
    ): TMapPoiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://apis.openapi.sk.com/"

    val instance: TMapApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TMapApiService::class.java)
    }
}