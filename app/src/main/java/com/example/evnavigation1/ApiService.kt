package com.example.evnavigation1

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header

const val TMAP_APP_KEY = "sDAEBi80t588hNSM41CIX2KePKM5SBViabdydGZG"

interface ApiService {
    @GET("getChargerInfo")
    suspend fun getEvChargers(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 20,
        @Query("zcode") zcode: String = "30",
        @Query("_type") dataType: String = "json"
    ): EvStationResponse

    @GET("https://apis.openapi.sk.com/tmap/pois")
    suspend fun searchPoi(
        @Header("appKey") appKey: String = TMAP_APP_KEY,
        @Query("searchKeyword") keyword: String,
        @Query("count") count: Int = 10
    ): PoiSearchResponse
}


// API 응답 데이터 구조 (JSON Parsing)
data class EvStationResponse(
    val items: EvItems?
)

data class EvItems(
    val item: List<EvApiItem>?
)

data class EvApiItem(
    val statId: String,
    val statNm: String,
    val addr: String,
    val lat: String,  // API에서는 String으로 전달됨
    val lng: String,
    val useTime: String,
    val chgerType: String

)
data class PoiSearchResponse(
    val searchPoiInfo: SearchPoiInfo?
)

data class SearchPoiInfo(
    val pois: Pois?
)

data class Pois(
    val poi: List<PoiItem>?
)

data class PoiItem(
    val id: String,
    val name: String,
    val lowerAddrName: String,
    val detailAddrName: String,
    val frontLat: String,
    val frontLon: String
)
