package com.example.tfgkotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.objects.Player
import de.hdodenhof.circleimageview.CircleImageView

class AssistantAdapter (private val assistantsList: MutableList<Player>) :
    RecyclerView.Adapter<AssistantAdapter.AssistantViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AssistantViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.assistant, parent, false)
        return AssistantViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AssistantViewHolder, position: Int) {
        val posAssistant = assistantsList[position]
        holder.name.text = posAssistant.username
    }

    override fun getItemCount(): Int {
        return assistantsList.size
    }

    class AssistantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.assistantName);
    }
}