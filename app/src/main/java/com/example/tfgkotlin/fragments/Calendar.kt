package com.example.tfgkotlin.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.activities.MainActivity
import com.example.tfgkotlin.adapters.AssistantAdapter
import com.example.tfgkotlin.dbmanager.DBAdministration
import com.example.tfgkotlin.dbmanager.DBCalendar
import com.example.tfgkotlin.objects.Player
import com.example.tfgkotlin.objects.UserType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Calendar : Fragment() {
    private lateinit var calendarView: CalendarView
    private var dateChosen = ""
    private lateinit var dropLayout: RelativeLayout
    private lateinit var assistantsDropDown: LinearLayout
    private lateinit var imgDrop: ImageView
    private lateinit var numAssistants: TextView
    private lateinit var txtObservations: TextView
    private lateinit var tvNoTraining: TextView
    private lateinit var rvAssistants: RecyclerView
    private lateinit var observations: TextView
    private lateinit var btnAssistants: ImageButton
    private lateinit var assistantAdapter: AssistantAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var layoutTaining: LinearLayout
    private var visible = false
    private lateinit var userId: String
    private lateinit var userType: UserType
    private var assistantsList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        setAttributes(view)
        setTodayDefault()
        checkIfExists()
        calendarListener()
        layoutListener()
        return view
    }

    private fun setAttributes(view: View) {
        assistantsDropDown = view.findViewById(R.id.assistantsDropDown)
        calendarView = view.findViewById(R.id.calendarView)
        dropLayout = view.findViewById(R.id.dropLayout)
        imgDrop = view.findViewById(R.id.imgDrop)
        txtObservations = view.findViewById(R.id.txtObservations)
        numAssistants = view.findViewById(R.id.numAssistants)
        rvAssistants = view.findViewById(R.id.rvAssistants)
        observations = view.findViewById(R.id.observations)
        btnAssistants = view.findViewById(R.id.btnAssistants)
        layoutTaining = view.findViewById(R.id.layoutTaining)
        database = FirebaseDatabase.getInstance()
        userType = (activity as MainActivity).getUserType()
        userId = (activity as MainActivity).getUserId()
        tvNoTraining = view.findViewById(R.id.tvNoTraining)
        txtObservations.visibility = View.GONE
    }

    private fun setTodayDefault() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        dateChosen = dateFormat.format(calendar.time)
    }

    private fun checkBeforeToday(): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val today = dateFormat.format(Date())

        val comparator = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val actual = comparator.parse(today)
        val selected = comparator.parse(dateChosen)

        if (selected.before(actual)) {
            checkAssisted()
            return false
        } else {
            return true
        }
    }

    private fun checkAssisted() {
        if (assistantsList.contains(userId)) {
            btnAssistants.setBackgroundResource(R.drawable.rounded_button_add)
            btnAssistants.setImageResource(R.drawable.check)
        } else {
            btnAssistants.setBackgroundResource(R.drawable.rounded_button_remove)
            btnAssistants.setImageResource(R.drawable.cancel)
        }
    }

    private fun checkIfExists() {
        DBCalendar.checkExistsTraining(dateChosen) { exists ->
            if (exists) {
                tvNoTraining.visibility = View.GONE
                layoutTaining.visibility = View.VISIBLE
                btnAssistants.visibility = View.VISIBLE
                observations.visibility = View.VISIBLE
                getAssistants()
            } else {
                layoutTaining.visibility = View.GONE
                btnAssistants.visibility = View.GONE
                observations.visibility = View.GONE
                tvNoTraining.visibility = View.VISIBLE
            }
        }
    }

    private fun calendarListener() {
        calendarView.setOnDateChangeListener { calendarView, i, i1, i2 ->
            dateChosen = i.toString() + "-" + (i1 + 1).toString() + "-" + i2.toString()
            txtObservations.visibility = View.GONE
            checkIfExists()
        }
    }

    private fun clearLayout() {
        dropLayout.visibility = View.GONE
        imgDrop.setImageResource(R.drawable.drop_down)
        visible = false
    }

    private fun layoutListener() {
        assistantsDropDown.setOnClickListener {
            if (visible) {
                clearLayout()
            } else {
                if (assistantsList.isNotEmpty()) {
                    showList()
                    dropLayout.visibility = View.VISIBLE
                    imgDrop.setImageResource(R.drawable.drop_up)
                    visible = true
                }
            }
        }
    }

    private fun setObservations(obs: String) {
        if (obs.isEmpty()) {
            observations.text = "Sin observaciones."
            txtObservations.visibility = View.GONE
        } else {
            txtObservations.visibility = View.VISIBLE
            observations.text = obs
        }
    }

    private fun getAssistants() {
        assistantsList.clear()

        lifecycleScope.launch {
            DBCalendar.getTraining(dateChosen).collect { trainingMap ->
                val observations = trainingMap["observations"] as? String
                setObservations(observations!!)

                val assistants = trainingMap["assistants"] as? MutableList<String>
                assistants?.let {
                    assistantsList.addAll(it)
                }

                setNumAssistants()
                if (checkBeforeToday()) {
                    checkAssist()
                    assistListener()
                    enableButton()
                } else {
                    disenableButton()
                }
                clearLayout()
            }
        }
    }

    private fun showList() {
        lifecycleScope.launch {
            DBCalendar.getAssistants(assistantsList).collect { assistantsNames ->
                val layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                rvAssistants.setLayoutManager(layoutManager)
                assistantAdapter = AssistantAdapter(assistantsNames)
                rvAssistants.setAdapter(assistantAdapter)
            }
        }
    }

    private fun assistListener() {
        btnAssistants.setOnClickListener {
            if (assistantsList.contains(userId)) {
                removePlayer()
            } else {
                addPlayer()
            }
        }
    }

    private fun removePlayer() {
        DBCalendar.removePlayer(dateChosen, userId) { playerRemoved ->
            if (playerRemoved) {
                getAssistants()
                checkAssist()
                setNumAssistants()
            } else {
                Log.d("", "Couldn't remove player.")
            }
        }
    }

    private fun addPlayer() {
        DBCalendar.addPlayer(dateChosen, userId) { playerAdded ->
            if (playerAdded) {
                getAssistants()
                checkAssist()
                setNumAssistants()
            } else {
                Log.d("", "Couldn't add player.")
            }
        }
    }

    private fun checkAssist() {
        if (assistantsList.contains(userId)) {
            btnAssistants.setBackgroundResource(R.drawable.rounded_button_remove)
            btnAssistants.setImageResource(R.drawable.remove)
        } else {
            btnAssistants.setBackgroundResource(R.drawable.rounded_button_add)
            btnAssistants.setImageResource(R.drawable.assist)
        }
    }

    private fun setNumAssistants() {
        if (assistantsList.isEmpty()) {
            numAssistants.text = "0"
        } else {
            numAssistants.text = assistantsList.size.toString()
        }
    }

    private fun disenableButton() {
        btnAssistants.isEnabled = false
    }

    private fun enableButton() {
        btnAssistants.isEnabled = true
    }
}