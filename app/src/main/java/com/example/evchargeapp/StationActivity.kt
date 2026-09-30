package com.example.evchargeapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val stationList = listOf(
            Station("대전 충전소", "급속 충전"),
            Station("서울역 충전소", "완속 충전"),
            Station("부산 충전소", "사용 가능")
        )

        recyclerView.adapter = StationAdapter(stationList)
    }
}