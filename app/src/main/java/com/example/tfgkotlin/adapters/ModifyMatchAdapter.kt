package com.example.tfgkotlin.adapters

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.dbmanager.DBAdministration
import com.example.tfgkotlin.objects.ActionMatch
import com.example.tfgkotlin.objects.Match
import com.example.tfgkotlin.objects.StatusMatch
import com.google.android.material.snackbar.Snackbar
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar

class ModifyMatchAdapter(
    private var matchesList: MutableList<Match>,
    private val context: Context,
    private var ctw: ContextThemeWrapper,
    private var action: String
) :
    RecyclerView.Adapter<ModifyMatchAdapter.ModifyMatchViewHolder>() {

    private lateinit var dialog: AlertDialog
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ModifyMatchViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.match, parent, false)
        return ModifyMatchViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ModifyMatchViewHolder, position: Int) {
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

    private fun setMatchesInterface(holder: ModifyMatchViewHolder, posMatch: Match) {
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

    private fun setCardListener(holder: ModifyMatchViewHolder, match: Match, position: Int) {
        when (action) {
            ActionMatch.DELETE.toString() -> setDelete(holder, match, position)
            ActionMatch.CANCEL.toString() -> setCancel(holder, match, position)
            ActionMatch.FINISH.toString() -> setFinish(holder, match, position)
        }
    }

    private fun setDelete(holder: ModifyMatchViewHolder, match: Match, position: Int) {
        holder.cardViewMatch.setOnClickListener {
            val view =
                LayoutInflater.from(context).inflate(R.layout.dialog_modify_match, null)
            val cancel = view.findViewById<Button>(R.id.cancelBtnMatch)
            val accept = view.findViewById<Button>(R.id.acceptBtnMatch)
            accept.text = context.getString(R.string.eliminar_partido)
            val txt = view.findViewById<TextView>(R.id.txtMatch)
            txt.text = context.getString(R.string.desea_eliminar_este_partido)

            cancel.setOnClickListener { dialog.dismiss() }
            val builder = AlertDialog.Builder(context)
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)

            accept.setOnClickListener {
                DBAdministration.deleteMatch(match.id) { deleted ->
                    if (deleted) {
                        matchesList.removeAt(position)
                        notifyDataSetChanged()
                        dialog.dismiss()
                    }
                }
            }

            dialog.show()
        }
    }

    private fun setCancel(holder: ModifyMatchViewHolder, match: Match, position: Int) {
        if (match.status != StatusMatch.CANCELLED.toString()) {
            holder.cardViewMatch.setOnClickListener {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.dialog_modify_match, null)
                val cancel = view.findViewById<Button>(R.id.cancelBtnMatch)
                val accept = view.findViewById<Button>(R.id.acceptBtnMatch)
                accept.text = context.getString(R.string.aceptar)
                val txt = view.findViewById<TextView>(R.id.txtMatch)
                txt.text = context.getString(R.string.desea_cancelar_este_partido)

                cancel.setOnClickListener { dialog.dismiss() }
                val builder = AlertDialog.Builder(context)
                builder.setView(view)
                dialog = builder.create()
                dialog.setCanceledOnTouchOutside(false)

                accept.setOnClickListener {
                    DBAdministration.cancelMatch(match.id) { cancelled ->
                        if (cancelled) {
                            match.status = StatusMatch.CANCELLED.toString()
                            notifyDataSetChanged()
                            matchesList = matchesList.sortedByDescending {
                                it.date?.let { date ->
                                    SimpleDateFormat("d/M/yyyy").parse(date)
                                }
                            }.toMutableList()
                            setMatchesInterface(holder, match)
                            dialog.dismiss()
                        }
                    }
                }

                dialog.show()
            }
        }
    }

    private fun setFinish(holder: ModifyMatchViewHolder, match: Match, position: Int) {
        if (match.status != StatusMatch.CANCELLED.toString()) {
            holder.cardViewMatch.setOnClickListener {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.dialog_finish_match, null)
                val cancel = view.findViewById<Button>(R.id.cancelFinishMatch)
                val accept = view.findViewById<Button>(R.id.acceptFinishMatch)
                val score1 = view.findViewById<TextView>(R.id.finishScore1)
                val score2 = view.findViewById<TextView>(R.id.finishScore2)
                val date = view.findViewById<TextView>(R.id.finishMatchDate)
                date.text = match.date

                date.setOnClickListener {
                    val cal = Calendar.getInstance()
                    val year = cal[Calendar.YEAR]
                    val month = cal[Calendar.MONTH]
                    val day = cal[Calendar.DAY_OF_MONTH]
                    val dialog = DatePickerDialog(
                        context,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        year, month, day
                    )
                    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.datePicker.maxDate = System.currentTimeMillis()
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.show()
                }
                dateSetListener =
                    DatePickerDialog.OnDateSetListener() { datePicker, year, month, day ->
                        var month = month
                        month = month + 1
                        var txtDate = "$day/$month/$year"
                        date.text = txtDate
                    }

                cancel.setOnClickListener { dialog.dismiss() }
                val builder = AlertDialog.Builder(context)
                builder.setView(view)
                dialog = builder.create()
                dialog.setCanceledOnTouchOutside(false)

                accept.setOnClickListener {
                    val txtScore1 = score1.text.toString().trim()
                    val txtScore2 = score2.text.toString().trim()
                    if (txtScore1.isEmpty() || txtScore2.isEmpty()) {
                        Snackbar.make(
                            ctw,
                            view,
                            "Establece los resultados del partido",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        match.date = date.text.toString()
                        match.status = StatusMatch.FINISHED.toString()
                        match.scoreTeam1 = txtScore1
                        match.scoreTeam2 = txtScore2

                        DBAdministration.finishMatch(
                            match
                        ) { finished ->
                            notifyDataSetChanged()
                            matchesList = matchesList.sortedByDescending {
                                it.date?.let { date ->
                                    SimpleDateFormat("d/M/yyyy").parse(date)
                                }
                            }.toMutableList()
                            setMatchesInterface(holder, match)
                            dialog.dismiss()
                        }
                    }
                }

                dialog.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return matchesList.size
    }

    class ModifyMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgTeam1: CircleImageView = itemView.findViewById(R.id.imgTeam1)
        val imgTeam2: CircleImageView = itemView.findViewById(R.id.imgTeam2)
        val nameTeam1: TextView = itemView.findViewById(R.id.nameTeam1)
        val nameTeam2: TextView = itemView.findViewById(R.id.nameTeam2)
        val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        val scoreTeam1: TextView = itemView.findViewById(R.id.scoreTeam1)
        val scoreTeam2: TextView = itemView.findViewById(R.id.scoreTeam2)
        val matchDate: TextView = itemView.findViewById(R.id.matchDate)
        val cardViewMatch: CardView = itemView.findViewById(R.id.cardViewMatch)
        val txtVS: TextView = itemView.findViewById(R.id.txtVS)
    }
}