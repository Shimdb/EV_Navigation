package com.example.evnavigation1

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.POST

const val TMAP_APP_KEY = "sDAEBi80t588hNSM41CIX2KePKM5SBViabdydGZG"

interface ApiService {
    @GET("stations")
    suspend fun getStations(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): List<BackendStationDto>

    @POST("auth/login")
    suspend fun login(): Response<ResponseBody>

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
data class BackendStationDto(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("available") val available: Boolean
)
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


