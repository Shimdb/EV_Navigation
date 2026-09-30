package com.example.evchargeapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StationAdapter

    private val stationList = listOf(
        Station("대전 충전소", "급속 충전"),
        Station("서울역 충전소", "완속 충전"),
        Station("부산 충전소", "사용 가능"),
        Station("인천 충전소", "급속 충전")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = StationAdapter(stationList)

        recyclerView.adapter = adapter

        val searchEdit = findViewById<EditText>(R.id.searchEdit)

        searchEdit.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

                val filteredList = stationList.filter {

                    it.name.contains(s.toString())
                }

                adapter = StationAdapter(filteredList)

                recyclerView.adapter = adapter
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}
        })
    }
}