package com.example.tfgkotlin.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.activities.Login
import com.example.tfgkotlin.activities.MainActivity
import com.example.tfgkotlin.adapters.DeletePlayerAdapter
import com.example.tfgkotlin.adapters.DeletePosAdapter
import com.example.tfgkotlin.adapters.DeleteTeamAdapter
import com.example.tfgkotlin.dbmanager.DBAdministration
import com.example.tfgkotlin.dbmanager.DBCalification
import com.example.tfgkotlin.model.OpenGallery
import com.example.tfgkotlin.objects.ActionMatch
import com.example.tfgkotlin.objects.StatusMatch
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class Administration : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var adminLogout: Button
    private lateinit var adminMenu: ImageView
    private lateinit var adminProfile: ImageView
    private lateinit var adminUsername: TextView
    private lateinit var userId: String
    private lateinit var navigationView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawer_layout_admin: DrawerLayout
    private lateinit var sp: SharedPreferences

    private lateinit var imgAddTeam: ImageView
    private var imgAddTeamUri: Uri? = null

    private lateinit var cardAddPlayer: CardView
    private lateinit var cardDeletePlayer: CardView
    private lateinit var cardAddTraining: CardView
    private lateinit var cardDeleteTraining: CardView
    private lateinit var cardAddMatch: CardView
    private lateinit var cardDeleteMatch: CardView
    private lateinit var cardAddTeam: CardView
    private lateinit var cardDeleteTeam: CardView
    private lateinit var cardAddPosition: CardView
    private lateinit var cardDeletePosition: CardView
    private lateinit var cardCancelMatch: CardView
    private lateinit var cardAddCalif: CardView
    private lateinit var cardModifyCalif: CardView
    private lateinit var cardFinishMatch: CardView
    private lateinit var cardEditTeam: CardView
    private lateinit var cardSeeCodes: CardView

    private lateinit var deleteTeamAdapter: DeleteTeamAdapter
    private lateinit var deletePlayerAdapter: DeletePlayerAdapter
    private lateinit var imgMatchTeam1: ImageView
    private lateinit var imgMatchTeam2: ImageView
    private lateinit var team1Name: TextView
    private lateinit var team2Name: TextView
    private lateinit var addMatchDate: TextView
    private lateinit var adapterSeasons: ArrayAdapter<String>
    private var seasonSelected = ""

    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var matchDate = ""

    private lateinit var rootView: View
    private lateinit var navAdminEdit: ImageView
    private lateinit var navImg: ImageView
    private lateinit var navEmail: TextView
    private lateinit var navName: TextView
    private var profileImage: String? = null
    private lateinit var ctw: ContextThemeWrapper

    private lateinit var dialog: AlertDialog
    private var dateChosen = ""
    private lateinit var calendarAddTraining: CalendarView
    private lateinit var calendarDelete: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_administration, container, false)
        rootView = view
        setAttributes(view)
        setInfo()
        logout()
        listenerMenu()
        setSeasons()

        //trainings
        addTraining()
        deleteTraining()
        //matches
        addMatch()
        deleteMatch()
        cancelMatch()
        finishMatch()
        //more
        addTeam()
        deleteTeam()
        editTeam()
        //players
        deletePlayer()
        addPlayer()
        //positions
        addPos()
        deletePos()
        //califications
        addCalification()
        modifyCalification()
        //codes
        seeCodes()

        return view
    }

    private fun setAttributes(view: View) {
        ctw = ContextThemeWrapper(requireContext(), R.style.CustomSnackbarTheme)
        //CARDVIEWS
        cardAddTraining = view.findViewById(R.id.cardAddTraining)
        cardDeleteTraining = view.findViewById(R.id.cardDeleteTraining)
        cardAddMatch = view.findViewById(R.id.cardAddMatch)
        cardDeleteMatch = view.findViewById(R.id.cardDeleteMatch)
        cardAddTeam = view.findViewById(R.id.cardAddTeam)
        cardDeleteTeam = view.findViewById(R.id.cardDeleteTeam)
        cardAddPlayer = view.findViewById(R.id.cardAddPlayer)
        cardDeletePlayer = view.findViewById(R.id.cardDeletePlayer)
        cardAddPosition = view.findViewById(R.id.cardAddPosition)
        cardDeletePosition = view.findViewById(R.id.cardDeletePosition)
        cardCancelMatch = view.findViewById(R.id.cardCancelMatch)
        cardModifyCalif = view.findViewById(R.id.cardModifyCalif)
        cardAddCalif = view.findViewById(R.id.cardAddCalif)
        cardFinishMatch = view.findViewById(R.id.cardFinishMatch)
        cardEditTeam = view.findViewById(R.id.cardEditTeam)
        cardSeeCodes = view.findViewById(R.id.cardSeeCodes)

        adminMenu = view.findViewById(R.id.adminMenu)
        adminProfile = view.findViewById(R.id.adminProfile);
        adminUsername = view.findViewById(R.id.adminUsername)
        userId = (activity as MainActivity).getUserId()

        drawer_layout_admin = view.findViewById(R.id.drawer_layout_admin)
        navigationView = view.findViewById(R.id.navigation_view)
        drawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            drawer_layout_admin,
            R.string.open_nav,
            R.string.close_nav
        )
        drawer_layout_admin.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun setSeasons() {
        adapterSeasons = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line)
        lifecycleScope.launch {
            DBAdministration.getSeasons().collect { seasonList ->
                if (seasonList.isEmpty()) {
                    adapterSeasons.clear()
                    adapterSeasons.add(getString(R.string.sin_temporada))
                    seasonSelected = getString(R.string.sin_temporada)
                } else {
                    adapterSeasons.clear()
                    adapterSeasons.addAll(seasonList)
                    adapterSeasons.notifyDataSetChanged()
                }
            }
        }
    }

    private fun showDatesMatch() {
        addMatchDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal[Calendar.YEAR]
            val month = cal[Calendar.MONTH]
            val day = cal[Calendar.DAY_OF_MONTH]
            val dialog = DatePickerDialog(
                requireContext(),
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                dateSetListener,
                year, month, day
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

        dateSetListener =
            DatePickerDialog.OnDateSetListener() { datePicker, year, month, day ->
                var month = month
                month = month + 1
                matchDate = "$day/$month/$year"
                addMatchDate.text = matchDate
            }
    }

    private fun addPlayer() {
        cardAddPlayer.setOnClickListener {
            (activity as MainActivity).changeFragment(AddNewPlayer())
        }
    }

    private fun addCalification() {
        cardAddCalif.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_add_calification, null)
            val cancel = view.findViewById<Button>(R.id.cancelAddCalif)
            val trainingDate = view.findViewById<TextView>(R.id.trainingDate)
            val txtCalifDialog = view.findViewById<TextView>(R.id.txtCalifDialog)
            var txtDate: String
            cancel.setOnClickListener { dialog.dismiss() }
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)


            trainingDate.setOnClickListener {
                val cal = Calendar.getInstance()
                val year = cal[Calendar.YEAR]
                val month = cal[Calendar.MONTH]
                val day = cal[Calendar.DAY_OF_MONTH]
                val dialog = DatePickerDialog(
                    requireContext(),
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
                    txtDate = "$year-$month-$day"
                    trainingDate.text = txtDate

                    DBCalification.checkTrainingExists(txtDate) { exists, id ->
                        if (!exists) {
                            txtCalifDialog.text = "NO HUBO ENTRENAMIENTO ESTE DÍA"
                            txtCalifDialog.visibility = View.VISIBLE
                        } else {
                            txtCalifDialog.visibility = View.GONE
                            DBCalification.checkTrainingCalificated(id!!) { calificated ->
                                if (calificated) {
                                    txtCalifDialog.text = "EL ENTRENAMIENTO YA HA SIDO CALIFICADO"
                                    txtCalifDialog.visibility = View.VISIBLE
                                } else {
                                    dialog.dismiss()
                                    (activity as MainActivity).changeFragment(
                                        SetCalifications.newInstance(
                                            id, "SET"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

            dialog.show()
        }
    }

    private fun modifyCalification() {
        cardModifyCalif.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_add_calification, null)
            val cancel = view.findViewById<Button>(R.id.cancelAddCalif)
            val trainingDate = view.findViewById<TextView>(R.id.trainingDate)
            val txtCalifDialog = view.findViewById<TextView>(R.id.txtCalifDialog)
            var txtDate: String
            cancel.setOnClickListener { dialog.dismiss() }
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)

            trainingDate.setOnClickListener {
                val cal = Calendar.getInstance()
                val year = cal[Calendar.YEAR]
                val month = cal[Calendar.MONTH]
                val day = cal[Calendar.DAY_OF_MONTH]
                val dialog = DatePickerDialog(
                    requireContext(),
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
                    txtDate = "$year-$month-$day"
                    trainingDate.text = txtDate

                    DBCalification.checkTrainingExists(txtDate) { exists, id ->
                        if (!exists) {
                            txtCalifDialog.text = "NO HUBO ENTRENAMIENTO ESTE DÍA"
                            txtCalifDialog.visibility = View.VISIBLE
                        } else {
                            txtCalifDialog.visibility = View.GONE
                            DBCalification.checkTrainingCalificated(id!!) { calificated ->
                                if (calificated) {
                                    dialog.dismiss()
                                    (activity as MainActivity).changeFragment(
                                        SetCalifications.newInstance(
                                            id, "UPDATE"
                                        )
                                    )
                                } else {
                                    txtCalifDialog.text = "EL ENTRENAMIENTO NO HA SIDO CALIFICADO"
                                    txtCalifDialog.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }

            dialog.show()
        }
    }

    private fun deletePlayer() {
        cardDeletePlayer.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_player, null)
            val cancelDeletePlayer = view.findViewById<Button>(R.id.cancelDeletePlayer)
            val acceptDeletePlayer = view.findViewById<Button>(R.id.acceptDeletePlayer)
            val rvDeletePlayer = view.findViewById<RecyclerView>(R.id.rvDeletePlayer)
            val txtSelecPlayer = view.findViewById<TextView>(R.id.txtSelecPlayer)
            cancelDeletePlayer.setOnClickListener { dialog.dismiss() }
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)

            lifecycleScope.launch {
                DBAdministration.getPlayersActive()
                    .collect { players ->
                        if (players.isEmpty()) {
                            txtSelecPlayer.text = getString(R.string.no_existen_jugadores)
                            acceptDeletePlayer.isEnabled = false
                        } else {
                            acceptDeletePlayer.isEnabled = true
                            txtSelecPlayer.text = getString(R.string.selecciona_jugador)
                            val layoutManager =
                                LinearLayoutManager(
                                    requireContext(),
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                            rvDeletePlayer.setLayoutManager(layoutManager)
                            deletePlayerAdapter = DeletePlayerAdapter(players)
                            val playerAdapter = deletePlayerAdapter
                            rvDeletePlayer.setAdapter(playerAdapter)
                        }
                    }
            }

            acceptDeletePlayer.setOnClickListener {
                deletePlayerAdapter.delete() { playerDeleted ->
                    if (playerDeleted) {
                        dialog.dismiss()
                        Snackbar.make(
                            ctw,
                            rootView,
                            "Jugador eliminado",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Snackbar.make(
                            ctw,
                            rootView,
                            "No se ha podido eliminar al jugador",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            dialog.show()
        }
    }

    private fun deleteMatch() {
        cardDeleteMatch.setOnClickListener {
            (activity as MainActivity).changeFragmentTransitionRight(Matches.newInstance(ActionMatch.DELETE.toString()))
        }
    }

    private fun cancelMatch() {
        cardCancelMatch.setOnClickListener {
            (activity as MainActivity).changeFragmentTransitionRight(Matches.newInstance(ActionMatch.CANCEL.toString()))
        }
    }

    private fun finishMatch() {
        cardFinishMatch.setOnClickListener {
            (activity as MainActivity).changeFragmentTransitionRight(Matches.newInstance(ActionMatch.FINISH.toString()))
        }
    }

    private fun deleteTeam() {
        cardDeleteTeam.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_team, null)
            val cancelDeleteTeam = view.findViewById<Button>(R.id.cancelDeleteTeam)
            val acceptDeleteTeam = view.findViewById<Button>(R.id.acceptDeleteTeam)
            val rvDeleteTeam = view.findViewById<RecyclerView>(R.id.rvDeleteTeam)
            val txtSelecTeam = view.findViewById<TextView>(R.id.txtSelecTeam)
            cancelDeleteTeam.setOnClickListener { dialog.dismiss() }
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)

            DBAdministration.getTeamsActive { teams ->
                if (teams.isEmpty()) {
                    txtSelecTeam.text = getString(R.string.no_existen_equipos)
                    acceptDeleteTeam.isEnabled = false
                } else {
                    acceptDeleteTeam.isEnabled = true
                    txtSelecTeam.text = getString(R.string.selecciona_equipo)
                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    rvDeleteTeam.setLayoutManager(layoutManager)
                    deleteTeamAdapter = DeleteTeamAdapter(teams)
                    rvDeleteTeam.setAdapter(deleteTeamAdapter)
                }
            }

            acceptDeleteTeam.setOnClickListener {
                deleteTeamAdapter.delete() { teamDeleted ->
                    if (teamDeleted) {
                        dialog.dismiss()
                        Snackbar.make(
                            ctw,
                            rootView,
                            "Equipo eliminado",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Snackbar.make(
                            ctw,
                            rootView,
                            "No se ha podido eliminar el equipo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            dialog.show()
        }
    }

    private fun seeCodes() {
        cardSeeCodes.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_see_codes, null)
            val adminCode = view.findViewById<TextView>(R.id.seeCodeAdmin)
            val playerCode = view.findViewById<TextView>(R.id.seeCodePlayer)

            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()

            DBAdministration.seeCodes { admin, player ->
                admin?.let {
                    println(admin)
                    adminCode.text = admin
                }
                player?.let {
                    playerCode.text = player
                }
            }

            dialog.show()
        }
    }

    private fun addPos() {
        cardAddPosition.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_pos, null)
            val cancelAdd = view.findViewById<Button>(R.id.cancelAddPos)
            val acceptAdd = view.findViewById<Button>(R.id.acceptAddPos)
            val addPosName = view.findViewById<EditText>(R.id.addPosName)
            cancelAdd.setOnClickListener { dialog.dismiss() }
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)

            acceptAdd.setOnClickListener {
                val name = addPosName.text.toString().trim()
                if (name.isEmpty()) {
                    addPosName.error = "Rellena este campo"
                } else {
                    DBAdministration.addPosition(name) { added ->
                        if (added) {
                            Snackbar.make(
                                ctw,
                                rootView,
                                "Nueva posición añadida",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        } else {
                            Snackbar.make(
                                ctw,
                                view,
                                "La posición ya existe",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            dialog.show()
        }
    }

    private fun deletePos() {
        cardDeletePosition.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_pos, null)
            val cancelDeletePos = view.findViewById<Button>(R.id.cancelDeletePos)
            val acceptDeletePos = view.findViewById<Button>(R.id.acceptDeletePos)
            val rvPositions = view.findViewById<RecyclerView>(R.id.rvDeletePos)
            val txtDeletePos = view.findViewById<TextView>(R.id.txtDeletePos)
            var posAdapter: DeletePosAdapter? = null

            DBAdministration.getPositions() { positions ->
                if (positions.isEmpty()) {
                    txtDeletePos.text = getString(R.string.no_existen_posiciones)
                    acceptDeletePos.isEnabled = false
                } else {
                    acceptDeletePos.isEnabled = true
                    txtDeletePos.text = getString(R.string.selecciona_pos)
                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    rvPositions.setLayoutManager(layoutManager)
                    posAdapter = DeletePosAdapter(positions)
                    rvPositions.setAdapter(posAdapter)
                }
            }

            cancelDeletePos.setOnClickListener { dialog.dismiss() }
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)


            acceptDeletePos.setOnClickListener {
                posAdapter!!.delete { deleted ->
                    if (deleted) {
                        Snackbar.make(
                            ctw,
                            rootView,
                            "Posición eliminada",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    } else {
                        Snackbar.make(
                            ctw,
                            rootView,
                            "La posición no se pudo eliminar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            dialog.show()
        }
    }

    private fun addTeam() {
        cardAddTeam.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_team, null)
            val cancelAddTeam = view.findViewById<Button>(R.id.cancelAddTeam)
            val acceptAddTeam = view.findViewById<Button>(R.id.acceptAddTeam)
            imgAddTeam = view.findViewById(R.id.imgAddTeam)
            val nameAddTeam = view.findViewById<EditText>(R.id.nameAddTeam)
            cancelAddTeam.setOnClickListener { dialog.dismiss() }
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)

            imgAddTeam.setOnClickListener {
                OpenGallery.openGallery(requireActivity(), galleryLauncher)
            }

            acceptAddTeam.setOnClickListener {
                val name = nameAddTeam.text.toString().trim()
                if (name.length < 5) {
                    nameAddTeam.error = "Debe contener mínimo 5 caracteres"
                }
                if (imgAddTeamUri == null) {
                    Snackbar.make(
                        ctw,
                        view,
                        "Selecciona una imagen para el equipo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                if (name.isEmpty()) {
                    nameAddTeam.error = "Añade un nombre al equipo"
                }

                if (imgAddTeamUri != null && name.isNotEmpty() && name.length >= 5) {
                    DBAdministration.addTeam(imgAddTeamUri!!, name) { addedTeam ->
                        if (addedTeam) {
                            dialog.dismiss()
                            imgAddTeamUri = null
                        }
                    }
                }
            }
            dialog.show()
        }
    }

    private fun editTeam() {
        cardEditTeam.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_team, null)
            val cancelAddTeam = view.findViewById<Button>(R.id.cancelAddTeam)
            val acceptAddTeam = view.findViewById<Button>(R.id.acceptAddTeam)
            imgAddTeam = view.findViewById(R.id.imgAddTeam)
            val nameAddTeam = view.findViewById<EditText>(R.id.nameAddTeam)

            DBAdministration.getTeam("-OWNER") { team ->
                team?.let {
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(team.image)
                        .into(imgAddTeam)
                    nameAddTeam.hint = team.name
                }
            }

            cancelAddTeam.setOnClickListener { dialog.dismiss() }
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)

            imgAddTeam.setOnClickListener {
                OpenGallery.openGallery(requireActivity(), galleryLauncher)
            }

            acceptAddTeam.setOnClickListener {
                val name = nameAddTeam.text.toString().trim()
                if (name.length < 5) {
                    nameAddTeam.error = "Debe contener mínimo 5 caracteres"
                }

                if (name.isEmpty()) {
                    nameAddTeam.error = "Añade un nombre al equipo"
                }

                if (name.isNotEmpty() && name.length >= 5) {
                    if (imgAddTeamUri != null) {
                        DBAdministration.uploadImage(imgAddTeamUri!!, "-OWNER")
                    }
                    DBAdministration.editTeam(name) { edited ->
                        if (edited) {
                            dialog.dismiss()
                            imgAddTeamUri = null
                        }
                    }
                }
            }
            dialog.show()
        }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                imgAddTeamUri = result.data?.data
                if (imgAddTeamUri != null) {
                    imgAddTeam.setImageURI(imgAddTeamUri)
                }
            }
        }

    private fun addMatch() {
        cardAddMatch.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_match, null)

            val cancelAddMatch = view.findViewById<Button>(R.id.cancelAddMatch)
            val acceptAddMatch = view.findViewById<Button>(R.id.acceptAddMatch)
            val addSeasonSpinner = view.findViewById<Spinner>(R.id.addSeasonSpinner)
            val addLeagueMatch = view.findViewById<EditText>(R.id.addLeagueMatch)
            val addHourStart = view.findViewById<EditText>(R.id.addHourStart)
            val addMinutesStart = view.findViewById<EditText>(R.id.addMinutesStart)
            val addHourEnd = view.findViewById<EditText>(R.id.addHourEnd)
            val addMinutesEnd = view.findViewById<EditText>(R.id.addMinutesEnd)
            val addMatchPlace = view.findViewById<EditText>(R.id.addMatchPlace)
            addMatchDate = view.findViewById(R.id.addMatchDate)
            imgMatchTeam1 = view.findViewById(R.id.imgMatchTeam1)
            imgMatchTeam2 = view.findViewById(R.id.imgMatchTeam2)
            team1Name = view.findViewById(R.id.team1Name)
            team2Name = view.findViewById(R.id.team2Name)

            imgMatchTeam1.setOnClickListener {
                chooseTeams(1)
            }
            imgMatchTeam2.setOnClickListener {
                chooseTeams(2)
            }
            showDatesMatch()

            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            val dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)

            //hours comprobations
            addHourStart.filters = arrayOf<InputFilter>(MinMaxFilter(0, 24))
            addMinutesStart.filters = arrayOf<InputFilter>(MinMaxFilter(0, 59))
            addHourEnd.filters = arrayOf<InputFilter>(MinMaxFilter(0, 24))
            addMinutesEnd.filters = arrayOf<InputFilter>(MinMaxFilter(0, 59))
            addLeagueMatch.filters = arrayOf<InputFilter>(MinMaxFilter(1, 99))
            addMatchDate.text = getTodayDate()

            adapterSeasons.setDropDownViewResource(R.layout.spinner_item)
            addSeasonSpinner.setAdapter(adapterSeasons)
            addSeasonSpinner.onItemSelectedListener = this

            //buttons listeners
            cancelAddMatch.setOnClickListener { dialog.dismiss() }
            acceptAddMatch.setOnClickListener {
                var can = true
                val place = addMatchPlace.text.toString().trim()
                val hourStart = addHourStart.text.toString()
                val minutesStart = addMinutesStart.text.toString()
                val hourEnd = addHourEnd.text.toString()
                val minutesEnd = addMinutesEnd.text.toString()
                val team1 = team1Name.text.toString()
                val team2 = team2Name.text.toString()
                val league = addLeagueMatch.text.toString().trim()
                val date = addMatchDate.text.toString().trim()

                if (team1 == getString(R.string.equipo) || team2 == getString(R.string.equipo)) {
                    Snackbar.make(
                        ctw,
                        view,
                        "Selecciona los equipos",
                        Toast.LENGTH_SHORT
                    ).show()
                    can = false
                } else if (team1 == team2) {
                    Snackbar.make(
                        ctw,
                        view,
                        "Los equipos deben ser dos distintos",
                        Toast.LENGTH_SHORT
                    ).show()
                    can = false
                } else if (hourStart.isEmpty() || minutesStart.isEmpty() || hourEnd.isEmpty() || minutesEnd.isEmpty()) {
                    Snackbar.make(
                        ctw,
                        view,
                        "Introduce las horas del partido",
                        Toast.LENGTH_SHORT
                    ).show()
                    can = false
                } else if (hourStart > hourEnd) {
                    Snackbar.make(
                        ctw,
                        view,
                        "La hora final debe ser posterior a la de inicio",
                        Toast.LENGTH_SHORT
                    ).show()
                    can = false
                } else if (hourStart.toInt() >= hourEnd.toInt() && minutesStart.toInt() >= minutesEnd.toInt()) {
                    Snackbar.make(
                        ctw,
                        view,
                        "La hora final debe ser posterior a la de inicio",
                        Toast.LENGTH_SHORT
                    ).show()
                    can = false
                }

                if (place.isEmpty()) {
                    addMatchPlace.error = getString(R.string.campos_vacios)
                }

                if (league.isEmpty()) {
                    addLeagueMatch.error = getString(R.string.campos_vacios)
                }

                if (place.isNotEmpty() && hourStart.isNotEmpty() && minutesStart.isNotEmpty() && hourEnd.isNotEmpty() && minutesEnd.isNotEmpty() && team1.isNotEmpty() && team2.isNotEmpty() && team1 != team2 && league.isNotEmpty()) {
                    if (can) {
                        val start = hourStart + ":" + minutesStart
                        val finish = hourEnd + ":" + minutesEnd

                        DBAdministration.addMatch(
                            team1,
                            team2,
                            date,
                            place,
                            seasonSelected,
                            league,
                            start,
                            finish,
                            StatusMatch.SCHEDULED.toString(),
                            0.toString(),
                            0.toString()
                        ) { added ->
                            if (added) {
                                dialog.dismiss()
                                Snackbar.make(
                                    ctw,
                                    rootView,
                                    "El partido ha sido añadido",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            } else {
                                Snackbar.make(
                                    ctw,
                                    view,
                                    "Este partido ya existe",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }

            dialog.show()
        }
    }

    override fun onItemSelected(
        parent: AdapterView<*>,
        view: View?,
        position: Int,
        id: Long
    ) {
        seasonSelected = parent.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //default
    }

    private fun chooseTeams(number: Int) {
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_team, null)
        val cancelDeleteTeam = view.findViewById<Button>(R.id.cancelDeleteTeam)
        val acceptDeleteTeam = view.findViewById<Button>(R.id.acceptDeleteTeam)
        acceptDeleteTeam.text = getString(R.string.aceptar)
        val rvDeleteTeam = view.findViewById<RecyclerView>(R.id.rvDeleteTeam)
        val txtSelecTeam = view.findViewById<TextView>(R.id.txtSelecTeam)
        cancelDeleteTeam.setOnClickListener { dialog.dismiss() }
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(view)
        dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)

        DBAdministration.getAllTeamsActive { teams ->
            if (teams.isEmpty()) {
                txtSelecTeam.text = getString(R.string.no_existen_equipos)
                acceptDeleteTeam.isEnabled = false
            } else {
                acceptDeleteTeam.isEnabled = true
                txtSelecTeam.text = getString(R.string.selecciona_equipo)
                val layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                rvDeleteTeam.setLayoutManager(layoutManager)
                deleteTeamAdapter = DeleteTeamAdapter(teams)
                val teamAdapter = deleteTeamAdapter
                rvDeleteTeam.setAdapter(teamAdapter)
            }
        }

        acceptDeleteTeam.setOnClickListener {
            val team = deleteTeamAdapter.getTeamSelected()
            team?.let {
                if (number == 1) {
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(team.image)
                        .into(imgMatchTeam1)
                    team1Name.text = team.name
                } else {
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(team.image)
                        .into(imgMatchTeam2)
                    team2Name.text = team.name
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    inner class MinMaxFilter() : InputFilter {
        private var intMin: Int = 0
        private var intMax: Int = 0

        constructor(minValue: Int, maxValue: Int) : this() {
            this.intMin = minValue
            this.intMax = maxValue
        }

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dStart: Int,
            dEnd: Int
        ): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(intMin, intMax, input)) {
                    return null
                }
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
            return ""
        }

        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }

    private fun addTraining() {
        cardAddTraining.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_training, null)
            val dialogObservations = view.findViewById<EditText>(R.id.dialogObservations)
            val cancelAddTraining = view.findViewById<Button>(R.id.cancelAddTraining)
            val acceptAddTraining = view.findViewById<Button>(R.id.acceptAddTraining)
            calendarAddTraining = view.findViewById(R.id.calendarAddTraining)
            setTodayDefault()
            calendarAddListener()

            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            cancelAddTraining.setOnClickListener { dialog.dismiss() }

            acceptAddTraining.setOnClickListener {
                if (checkBeforeToday()) {
                    Snackbar.make(
                        ctw,
                        view,
                        "No se pueden añadir entrenamientos pasados",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    DBAdministration.addTraining(
                        ctw,
                        dateChosen,
                        dialogObservations.text.toString(),
                        view,
                        rootView
                    ) { trainingAdd ->
                        if (trainingAdd) {
                            dialog.dismiss()
                        }
                    }
                }
            }
            dialog.show()
        }
    }

    private fun deleteTraining() {
        cardDeleteTraining.setOnClickListener {
            val view =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_training, null)
            val cancelDeleteTraining = view.findViewById<Button>(R.id.cancelDeleteTraining)
            val acceptDeleteTraining = view.findViewById<Button>(R.id.acceptDeleteTraining)
            calendarDelete = view.findViewById(R.id.calendarDelete)
            setTodayDefault()
            calendarDeleteListener()

            val builder = AlertDialog.Builder(requireContext())
            builder.setView(view)
            dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            cancelDeleteTraining.setOnClickListener { dialog.dismiss() }

            acceptDeleteTraining.setOnClickListener {
                DBAdministration.deleteTraining(
                    ctw,
                    dateChosen,
                    view,
                    rootView
                ) { deleteTraining, id ->
                    dialog.dismiss()
                    DBCalification.deleteCalification(id!!)
                }
            }
            dialog.show()
        }
    }

    private fun checkBeforeToday(): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val today = dateFormat.format(Date())

        val comparator = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val actual = comparator.parse(today)
        val selected = comparator.parse(dateChosen)

        if (selected.before(actual)) {
            return true
        } else {
            return false
        }
    }

    fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    private fun calendarAddListener() {
        calendarAddTraining.setOnDateChangeListener { calendarView, i, i1, i2 ->
            dateChosen = i.toString() + "-" + (i1 + 1).toString() + "-" + i2.toString()
        }
    }

    private fun calendarDeleteListener() {
        calendarDelete.setOnDateChangeListener { calendarView, i, i1, i2 ->
            dateChosen = i.toString() + "-" + (i1 + 1).toString() + "-" + i2.toString()
        }
    }

    private fun setTodayDefault() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        dateChosen = dateFormat.format(calendar.time)
    }

    private fun logout() {
        adminLogout.setOnClickListener {
            sp = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
            val editor = sp.edit()
            editor.clear().apply()

            val intent = Intent(this@Administration.requireContext(), Login::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                requireContext(),
                R.anim.transition_fade_in,
                R.anim.transition_fade_out
            )
            startActivity(intent, options.toBundle())
            requireActivity().finish()
        }
    }

    private fun setInfo() {
        navImg = navigationView.getHeaderView(0).findViewById(R.id.navAdminProfile)
        navName = navigationView.getHeaderView(0).findViewById(R.id.navAdminName)
        navEmail = navigationView.getHeaderView(0).findViewById(R.id.navAdminEmail)
        adminLogout = navigationView.getHeaderView(0).findViewById(R.id.adminLogout)
        navAdminEdit = navigationView.getHeaderView(0).findViewById(R.id.navAdminEdit)
        editProfileListener()

        lifecycleScope.launch {
            DBAdministration.getAdmin(userId)
                .collect { admin ->
                    admin?.let {
                        Glide.with(requireContext())
                            .asBitmap()
                            .load(admin.profileImage)
                            .into(navImg)
                        Glide.with(requireContext())
                            .asBitmap()
                            .load(admin.profileImage)
                            .into(adminProfile)
                        profileImage = admin.profileImage
                        navName.text = admin.username
                        adminUsername.text = admin.username
                        navEmail.text = admin.email
                    }
                }
        }
    }

    private fun editProfileListener() {
        navAdminEdit.setOnClickListener {
            (activity as MainActivity).changeFragment(EditAdminProfile())
        }
    }

    private fun listenerMenu() {
        adminMenu.setOnClickListener { drawer_layout_admin.openDrawer(GravityCompat.START) }
    }
}