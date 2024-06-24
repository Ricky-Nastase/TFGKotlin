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
import com.example.tfgkotlin.dbmanager.DBAccess
import com.example.tfgkotlin.objects.Player
import de.hdodenhof.circleimageview.CircleImageView

class DeletePlayerAdapter(private val teamList: List<Player>) :
    RecyclerView.Adapter<DeletePlayerAdapter.DeletePlayerViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION
    private lateinit var posPlayer: Player

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeletePlayerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.delete_item, parent, false)
        return DeletePlayerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeletePlayerViewHolder, position: Int) {
        posPlayer = teamList[position]
        holder.name.text = posPlayer.username
        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(posPlayer.profileImage)
            .into(holder.image)

        if (position == selectedPosition) {
            holder.image.borderColor = Color.WHITE
            holder.playerLayout.setBackgroundResource(R.drawable.delete_item_selected)
            holder.name.setTextColor(Color.WHITE)
        } else {
            holder.image.borderColor =
                ContextCompat.getColor(holder.itemView.context, R.color.granet)
            holder.playerLayout.setBackgroundResource(R.drawable.small_red_stroke_corners)
            holder.name.setTextColor(Color.BLACK)
        }

        holder.playerLayout.setOnClickListener {
            notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
        }

    }

    fun delete(callback: (Boolean) -> Unit) {
        if (selectedPosition != RecyclerView.NO_POSITION) {
            DBAccess.deleteUser(posPlayer.email!!) { deleted ->
                if (deleted) {
                    notifyItemChanged(selectedPosition)
                    callback(true)
                } else {
                    callback(false)
                }
            }
        }
    }

    fun getTeamSelected(): Player {
        return posPlayer
    }

    override fun getItemCount(): Int {
        return teamList.size
    }

    class DeletePlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: CircleImageView = itemView.findViewById(R.id.imageItemDelete)
        val name: TextView = itemView.findViewById(R.id.nameItemDelete)
        val playerLayout: LinearLayout = itemView.findViewById(R.id.layoutDeleteItem)
    }
}