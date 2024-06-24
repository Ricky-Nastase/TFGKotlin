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

class ViewCalificationAdapter (
    private val players: List<Player>,
    private val califications: List<Calification>
) :
    RecyclerView.Adapter<ViewCalificationAdapter.ViewCalificationViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewCalificationAdapter.ViewCalificationViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.calification, parent, false)
        return ViewCalificationViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: ViewCalificationViewHolder,
        position: Int
    ) {
        val calification = califications[position]

        holder.califName.text = calification.name
        val visible = calification.visible
        holder.dropPlayersCalif.visibility = if (visible) View.VISIBLE else View.GONE

        if (visible) {
            holder.dropImgCalif.setImageResource(R.drawable.drop_up)
        } else {
            holder.dropImgCalif.setImageResource(R.drawable.drop_down)
        }

        val playersCalif = mutableListOf<PlayerCalification>()
        for (player in players) {
            playersCalif.add(
                PlayerCalification(
                    player.username, "0"
                )
            )
        }

        val playersAdapter = PlayerCalifAdapter(playersCalif)

        holder.rvCalifPlayers.layoutManager =
            LinearLayoutManager(holder.itemView.context)
        holder.rvCalifPlayers.adapter = playersAdapter

        holder.layoutSetCalif.setOnClickListener {
            calification.visible = !calification.visible
            notifyItemChanged(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return califications.size
    }

    class ViewCalificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dropPlayersCalif: RelativeLayout = itemView.findViewById(R.id.dropPlayersCalif)
        val califName: TextView = itemView.findViewById(R.id.califName)
        val dropImgCalif: ImageView = itemView.findViewById(R.id.dropImgCalif)
        val rvCalifPlayers: RecyclerView = itemView.findViewById(R.id.rvCalifPlayers)
        val layoutSetCalif: ConstraintLayout = itemView.findViewById(R.id.layoutSetCalif)
    }
}