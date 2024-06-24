package com.example.tfgkotlin.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.dbmanager.DBAdministration
import com.example.tfgkotlin.objects.Match
import com.example.tfgkotlin.objects.StatusMatch
import com.example.tfgkotlin.objects.Team
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MatchAdapter(
    private val matchesList: List<Match>,
    private val context: Context
) :
    RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {

    private lateinit var dialog: AlertDialog

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MatchViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.match, parent, false)
        return MatchViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val posMatch = matchesList[position]

        DBAdministration.getTeam(posMatch.team1!!) { team1 ->
            team1?.let {
                Glide.with(holder.itemView.context)
                    .asBitmap()
                    .load(team1.image)
                    .into(holder.imgTeam1)
                holder.nameTeam1.text = team1.name
            }
        }

        DBAdministration.getTeam(posMatch.team2!!) { team2 ->
            team2?.let {
                Glide.with(holder.itemView.context)
                    .asBitmap()
                    .load(team2.image)
                    .into(holder.imgTeam2)
                holder.nameTeam2.text = team2.name
            }
        }

        holder.scoreTeam1.text = posMatch.scoreTeam1
        holder.scoreTeam2.text = posMatch.scoreTeam2
        holder.matchDate.text = posMatch.date

        setMatchesInterface(holder, posMatch)

        setCardListener(holder, posMatch, position)
    }

    private fun setMatchesInterface(holder: MatchViewHolder, posMatch: Match) {
        when (posMatch.status) {
            StatusMatch.SCHEDULED.toString() -> {
                holder.txtStatus.text = context.getString(R.string.planificado)
                holder.txtStatus.background =
                    ContextCompat.getDrawable(context, R.drawable.shape_planned)
                holder.scoreTeam1.visibility = View.GONE
                holder.scoreTeam2.visibility = View.GONE
                holder.txtVS.text = context.getString(R.string.vs)
                val size = 14
                holder.txtVS.textSize = size.toFloat()
            }

            StatusMatch.CANCELLED.toString() -> {
                holder.txtStatus.text =
                    context.getString(R.string.cancelado)
                holder.txtStatus.background =
                    ContextCompat.getDrawable(context, R.drawable.shape_cancelled)
                holder.scoreTeam1.visibility = View.GONE
                holder.scoreTeam2.visibility = View.GONE
                holder.txtVS.text = context.getString(R.string.vs)
                val size = 14
                holder.txtVS.textSize = size.toFloat()
            }

            StatusMatch.FINISHED.toString() -> {
                holder.txtStatus.text =
                    context.getString(R.string.finalizado)
                holder.txtStatus.background =
                    ContextCompat.getDrawable(context, R.drawable.shape_finished)
                holder.scoreTeam1.visibility = View.VISIBLE
                holder.scoreTeam2.visibility = View.VISIBLE
                holder.txtVS.text = context.getString(R.string.bar)
            }

            StatusMatch.ONGOING.toString() -> {
                holder.txtStatus.text =
                    context.getString(R.string.en_proceso)
                holder.txtStatus.background =
                    ContextCompat.getDrawable(context, R.drawable.shape_ongoing)
                holder.scoreTeam1.visibility = View.VISIBLE
                holder.scoreTeam2.visibility = View.VISIBLE
                holder.txtVS.text = context.getString(R.string.bar)
            }
        }
    }

    private fun setCardListener(holder: MatchAdapter.MatchViewHolder, match: Match, position: Int) {
        holder.cardViewMatchDelete.setOnClickListener {
            val view =
                LayoutInflater.from(context).inflate(R.layout.dialog_match_info, null)
            val placeMatch = view.findViewById<TextView>(R.id.placeMatch)
            val startMatch = view.findViewById<TextView>(R.id.startMatch)
            val endMatch = view.findViewById<TextView>(R.id.endMatch)
            val seasonMatch = view.findViewById<TextView>(R.id.seasonMatch)
            val leagueMatch = view.findViewById<TextView>(R.id.leagueMatch)

            val builder = AlertDialog.Builder(context)
            builder.setView(view)
            dialog = builder.create()

            placeMatch.text = match.place
            startMatch.text = match.start
            endMatch.text = match.finish
            seasonMatch.text = match.season
            leagueMatch.text = match.league

            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return matchesList.size
    }

    class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgTeam1: CircleImageView = itemView.findViewById(R.id.imgTeam1)
        val imgTeam2: CircleImageView = itemView.findViewById(R.id.imgTeam2)
        val nameTeam1: TextView = itemView.findViewById(R.id.nameTeam1)
        val nameTeam2: TextView = itemView.findViewById(R.id.nameTeam2)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val scoreTeam1: TextView = itemView.findViewById(R.id.scoreTeam1)
        val scoreTeam2: TextView = itemView.findViewById(R.id.scoreTeam2)
        val matchDate: TextView = itemView.findViewById(R.id.matchDate)
        val cardViewMatchDelete: CardView = itemView.findViewById(R.id.cardViewMatch)
        val txtVS: TextView = itemView.findViewById(R.id.txtVS)
    }
}