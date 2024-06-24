package com.example.tfgkotlin.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgkotlin.R
import com.example.tfgkotlin.objects.PlayerCalification

class ProfileCalifAdapter(private var califications: MutableList<PlayerCalification>) :
    RecyclerView.Adapter<ProfileCalifAdapter.ProfileCalifViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProfileCalifViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_califications, parent, false)
        return ProfileCalifViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfileCalifViewHolder, position: Int) {
        val calif = califications[position]
        holder.name.text = calif.name
        val formattedCalif = String.format("%.1f", calif.calification!!.toDouble())
        holder.calification.text = formattedCalif
    }

    override fun getItemCount(): Int {
        return califications.size
    }

    class ProfileCalifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.califNameProfile);
        val calification: TextView = itemView.findViewById(R.id.califNumberProfile);
    }
}