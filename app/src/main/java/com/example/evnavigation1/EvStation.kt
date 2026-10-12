package com.example.evnavigation1

data class EvStation(
    val statId: String,       // 충전소 ID
    val statNm: String,       // 충전소 이름
    val addr: String,         // 주소
    val lat: Double,          // 위도
    val lng: Double,          // 경도
    val useTime: String,      // 이용 가능 시간
    val chgerType: String,    // 👈 여기 끝에 쉼표(,) 추가!
)