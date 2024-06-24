package com.example.tfgkotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgkotlin.R
import com.example.tfgkotlin.objects.Calification
import com.example.tfgkotlin.objects.Player
import com.example.tfgkotlin.objects.PlayerCalification

class SetCalificationAdapter(
    private val califications: List<Calification>
) :
    RecyclerView.Adapter<SetCalificationAdapter.SetCalificationViewHolder>() {

    private lateinit var playersAdapter: PlayerCalifAdapter

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SetCalificationViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.calification, parent, false)
        return SetCalificationViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: SetCalificationViewHolder,
        position: Int
    ) {
        val calification = califications[position]

        holder.califName.text = calification.name
        holder.dropPlayersCalif.visibility = View.VISIBLE
        holder.dropImgCalif.visibility = View.GONE

        playersAdapter = PlayerCalifAdapter(calification.players!!)

        holder.rvCalifPlayers.layoutManager =
            LinearLayoutManager(holder.itemView.context)
        holder.rvCalifPlayers.adapter = playersAdapter
    }

    override fun getItemCount(): Int {
        return califications.size
    }

    fun getCalifications(): List<Calification> {
        return califications
    }

    class SetCalificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dropPlayersCalif: RelativeLayout = itemView.findViewById(R.id.dropPlayersCalif)
        val califName: TextView = itemView.findViewById(R.id.califName)
        val dropImgCalif: ImageView = itemView.findViewById(R.id.dropImgCalif)
        val rvCalifPlayers: RecyclerView = itemView.findViewById(R.id.rvCalifPlayers)
    }
}