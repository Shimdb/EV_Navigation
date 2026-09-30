package com.example.evchargeapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StationDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_station_detail)

        val stationName = intent.getStringExtra("stationName")
        val stationType = intent.getStringExtra("stationType")

        val nameText = findViewById<TextView>(R.id.detailName)
        val typeText = findViewById<TextView>(R.id.detailType)
        val bookmarkButton = findViewById<Button>(R.id.bookmarkButton)

        nameText.text = stationName
        typeText.text = stationType

        bookmarkButton.setOnClickListener {

            Toast.makeText(
                this,
                "즐겨찾기에 추가되었습니다",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}