package com.example.evnavigation1

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 공공데이터 포털 환경부 전기차 충전소 API Base URL
    private const val BASE_URL = "https://apis.data.go.kr/B552584/EvCharger/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}