package com.example.tfgkotlin.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.dbmanager.DBAdministration
import com.example.tfgkotlin.objects.Team

class DeletePosAdapter(private val positions: MutableList<String>) :
    RecyclerView.Adapter<DeletePosAdapter.PositionViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION
    private lateinit var pos: String

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PositionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.position, parent, false)
        return PositionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PositionViewHolder, position: Int) {
        pos = positions[position]
        holder.name.text = pos

        if (position == selectedPosition) {
            holder.layout.setBackgroundResource(R.drawable.delete_item_selected)
            holder.name.setTextColor(Color.WHITE)
        } else {
            holder.layout.setBackgroundResource(R.drawable.small_red_stroke_corners)
            holder.name.setTextColor(Color.BLACK)
        }

        holder.layout.setOnClickListener {
            notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
        }
    }

    fun delete(callback: (Boolean) -> Unit) {
        if (selectedPosition != RecyclerView.NO_POSITION) {
            DBAdministration.deletePos(pos) { deleted ->
                if (deleted) {
                    notifyItemChanged(selectedPosition)
                    callback(true)
                } else {
                    callback(false)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return positions.size
    }

    class PositionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.posName)
        val layout: LinearLayout = itemView.findViewById(R.id.posLayout)
    }
}