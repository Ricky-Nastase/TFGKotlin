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
import de.hdodenhof.circleimageview.CircleImageView

class DeleteTeamAdapter(private val teamList: MutableList<Team>) :
    RecyclerView.Adapter<DeleteTeamAdapter.DeleteTeamViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION
    private lateinit var posTeam: Team

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeleteTeamViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.delete_item, parent, false)
        return DeleteTeamViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeleteTeamViewHolder, position: Int) {
        posTeam = teamList[position]
        holder.name.text = posTeam.name
        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(posTeam.image)
            .into(holder.image)

        if (position == selectedPosition) {
            holder.image.borderColor = Color.WHITE
            holder.layoutDeleteTeam.setBackgroundResource(R.drawable.delete_item_selected)
            holder.name.setTextColor(Color.WHITE)
        } else {
            holder.image.borderColor =
                ContextCompat.getColor(holder.itemView.context, R.color.granet)
            holder.layoutDeleteTeam.setBackgroundResource(R.drawable.small_red_stroke_corners)
            holder.name.setTextColor(Color.BLACK)
        }

        holder.layoutDeleteTeam.setOnClickListener {
            notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
        }

    }

    fun delete(callback: (Boolean) -> Unit) {
        if (selectedPosition != RecyclerView.NO_POSITION) {
            DBAdministration.deleteTeam(posTeam) { deletedTeam ->
                if (deletedTeam) {
                    notifyItemChanged(selectedPosition)
                    callback(true)
                }else{
                    callback(false)
                }
            }
        }
    }

    fun getTeamSelected(): Team{
        return posTeam
    }

    override fun getItemCount(): Int {
        return teamList.size
    }

    class DeleteTeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: CircleImageView = itemView.findViewById(R.id.imageItemDelete)
        val name: TextView = itemView.findViewById(R.id.nameItemDelete)
        val layoutDeleteTeam: LinearLayout = itemView.findViewById(R.id.layoutDeleteItem)
    }
}