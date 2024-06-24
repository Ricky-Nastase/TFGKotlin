package com.example.tfgkotlin.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgkotlin.R
import com.example.tfgkotlin.objects.PlayerCalification

class PlayerCalifAdapter(private var players: MutableList<PlayerCalification>) :
    RecyclerView.Adapter<PlayerCalifAdapter.PlayerCalifViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayerCalifViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.player_calification, parent, false)
        return PlayerCalifViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlayerCalifViewHolder, position: Int) {
        val player = players[position]
        holder.name.text = player.name
        holder.calification.hint = player.calification

        holder.calification.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pos = holder.adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    player.calification = s.toString()
                    players[pos].calification = s.toString()
                }
            }
        })
    }

    override fun getItemCount(): Int {
        return players.size
    }

    class PlayerCalifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.playerCalifName);
        val calification: EditText = itemView.findViewById(R.id.playerCalifNumber);
    }
}