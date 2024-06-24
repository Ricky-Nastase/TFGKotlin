package com.example.tfgkotlin.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.objects.AllPlayerCalifications

class TotalCalificationAdapter(
    private var califications: MutableList<AllPlayerCalifications>,
    private val action: String
) :
    RecyclerView.Adapter<TotalCalificationAdapter.TotalsViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TotalsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.total_califications, parent, false)
        return TotalsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TotalsViewHolder, position: Int) {
        val calification = califications[position]

        holder.imgTotal.visibility = View.GONE
        if (action == "PROFILE") {
            holder.number.visibility = View.GONE
            holder.imgTotal.visibility = View.GONE
        } else {
            var number = position + 4
            holder.imgTotal.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .asBitmap()
                .load(calification.profileImage)
                .into(holder.img)
            holder.number.text = number.toString()
        }

        holder.name.text = calification.calificationName
        val formattedCalif = String.format("%.1f", calification.calification!!.toDouble())
        holder.calification.text = formattedCalif
    }

    override fun getItemCount(): Int {
        return califications.size
    }

    class TotalsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.totalName)
        val img: ImageView = itemView.findViewById(R.id.imgTotal)
        val calification: TextView = itemView.findViewById(R.id.totalCalif)
        val number: TextView = itemView.findViewById(R.id.numberTotal)
        val imgTotal: ImageView = itemView.findViewById(R.id.imgTotal)
    }
}