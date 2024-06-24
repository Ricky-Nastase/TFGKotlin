package com.example.tfgkotlin.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.activities.MainActivity
import com.example.tfgkotlin.fragments.PlayerProfile
import com.example.tfgkotlin.objects.Player
import de.hdodenhof.circleimageview.CircleImageView

class PlayerAdapter(private val playersList: List<Player>, private val context: Context) :
    RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.player, parent, false)
        return PlayerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val posPlayer = playersList[position]
        holder.name.text = posPlayer.username
        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(posPlayer.profileImage)
            .into(holder.image)
        viewProfile(holder, posPlayer)
    }

    private fun viewProfile(holder: PlayerViewHolder, player: Player) {
        holder.image.setOnClickListener {
            (context as MainActivity).changeFragmentFade(
                PlayerProfile.newInstance(
                    player.id!!
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return playersList.size
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: CircleImageView = itemView.findViewById(R.id.imgPlayer)
        val name: TextView = itemView.findViewById(R.id.playerName)
    }
}