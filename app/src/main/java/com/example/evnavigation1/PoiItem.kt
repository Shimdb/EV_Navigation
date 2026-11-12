package com.example.evnavigation1

data class TMapPoiResponse(
    val searchPoiInfo: SearchPoiInfo?
)

data class SearchPoiInfo(
    val totalCount: String?,
    val count: String?,
    val page: String?,
    val pois: Pois?
)

data class Pois(
    val poi: List<PoiItem>?
)

data class PoiItem(
    val id: String = "",
    val name: String = "",
    val frontLat: String = "",
    val frontLon: String = "",
    val upperAddrName: String = "",
    val middleAddrName: String = "",
    val lowerAddrName: String = "",
    val detailAddrName: String = "",
    val roadName: String = "",
    val firstBuildNo: String = ""
)