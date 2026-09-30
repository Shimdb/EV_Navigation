package com.example.evchargeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent

class StationAdapter(private val stationList: List<Station>) :
    RecyclerView.Adapter<StationAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val stationName: TextView = itemView.findViewById(R.id.stationName)
        val stationType: TextView = itemView.findViewById(R.id.stationType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_station, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val station = stationList[position]

        holder.stationName.text = station.name
        holder.stationType.text = station.type

        holder.itemView.setOnClickListener {

            val intent = Intent(holder.itemView.context, StationDetailActivity::class.java)

            intent.putExtra("stationName", station.name)
            intent.putExtra("stationType", station.type)

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return stationList.size
    }
}