package com.example.tfgkotlin.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.activities.Login
import com.example.tfgkotlin.activities.MainActivity
import com.example.tfgkotlin.adapters.PlayerAdapter
import com.example.tfgkotlin.adapters.PlayerCalifAdapter
import com.example.tfgkotlin.adapters.ProfileCalifAdapter
import com.example.tfgkotlin.adapters.TotalCalificationAdapter
import com.example.tfgkotlin.dbmanager.DBAccess
import com.example.tfgkotlin.dbmanager.DBAdministration
import com.example.tfgkotlin.dbmanager.DBCalification
import com.example.tfgkotlin.objects.AllPlayerCalifications
import com.example.tfgkotlin.objects.PlayerCalification
import com.example.tfgkotlin.objects.UserSex
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val ARG_PARAM = "param"

class PlayerProfile : Fragment() {

    private var idPlayer: String? = null
    private lateinit var txtNoCalificated: TextView
    private lateinit var spinner: Spinner
    private lateinit var rvProfileCalif: RecyclerView
    private lateinit var playerMenu: ImageView
    private lateinit var playerProfile: ImageView
    private lateinit var playerUsername: TextView
    private lateinit var playerNumber: TextView
    private lateinit var playerPosition: TextView
    private lateinit var userId: String
    private lateinit var navigationView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawer_layout_player: DrawerLayout
    private lateinit var sp: SharedPreferences
    private lateinit var selectedDate: String
    private lateinit var navPlayerEdit: ImageView
    private lateinit var navImg: ImageView
    private lateinit var player: String
    private lateinit var navEmail: TextView
    private lateinit var navName: TextView
    private lateinit var navPos: TextView
    private lateinit var navHeight: TextView
    private lateinit var navWeight: TextView
    private lateinit var navDate: TextView
    private lateinit var navSex: TextView
    private lateinit var navNum: TextView
    private var profileImage: String? = null
    private lateinit var playerLogout: Button
    private lateinit var totals: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idPlayer = it.getString(ARG_PARAM)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player_profile, container, false)
        setAttributes(view)
        if (idPlayer != null) {
            playerMenu.setImageDrawable(requireContext().getDrawable(R.drawable.edit_back))
            playerMenu.setOnClickListener { (activity as MainActivity).changeFragmentFade(Home()) }
            setInfoView()
        } else {
            playerMenu.visibility = View.VISIBLE
            setInfo()
            listenerMenu()
            logout()
        }

        if (idPlayer != null) {
            player = idPlayer!!
        } else {
            player = userId
        }

        setTrainings()
        viewTotals()
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param: String) =
            PlayerProfile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM, param)
                }
            }
    }

    private fun setInfoView() {
        lifecycleScope.launch {
            DBAdministration.getPlayer(idPlayer!!).collect() { player ->
                player?.let {
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(player.profileImage)
                        .into(playerProfile)
                    playerNumber.text = player.number
                    playerUsername.text = player.username
                    playerPosition.text = player.position
                }
            }
        }
    }

    private fun setAttributes(view: View) {
        txtNoCalificated = view.findViewById(R.id.txtNoCalificated)
        totals = view.findViewById(R.id.viewTotalCalif)
        spinner = view.findViewById(R.id.datesSpinner)
        rvProfileCalif = view.findViewById(R.id.rvProfileCalif)
        playerMenu = view.findViewById(R.id.playerMenu)
        playerProfile = view.findViewById(R.id.playerProfile)
        playerNumber = view.findViewById(R.id.playerNumber)
        playerPosition = view.findViewById(R.id.playerPosition)
        playerUsername = view.findViewById(R.id.playerUsername)
        userId = (activity as MainActivity).getUserId()

        drawer_layout_player = view.findViewById(R.id.drawer_layout_player)
        navigationView = view.findViewById(R.id.navigation_view)
        drawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            drawer_layout_player,
            R.string.open_nav,
            R.string.close_nav
        )
        drawer_layout_player.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setSpinner(list: MutableList<String>) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        val dates = list.map { LocalDate.parse(it, formatter) }

        val mostRecentDate = dates.maxOrNull()
        val date = mostRecentDate?.format(formatter)

        val sortedDateStrings = dates.sortedDescending().map { it.format(formatter) }

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortedDateStrings)
        adapter.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = adapter

        val position = sortedDateStrings.indexOf(date)
        spinner.setSelection(position)

        selectedDate = date!!

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedDate = parent.getItemAtPosition(position) as String
                setCalifications()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                setCalifications()
            }
        }

    }

    private fun listenerMenu() {
        playerMenu.setOnClickListener { drawer_layout_player.openDrawer(GravityCompat.START) }
    }

    private fun editProfileListener() {
        navPlayerEdit.setOnClickListener {
            (activity as MainActivity).changeFragment(EditPlayerProfile())
        }
    }

    private fun setInfo() {
        navImg = navigationView.getHeaderView(0).findViewById(R.id.navPlayerProfile)
        navName = navigationView.getHeaderView(0).findViewById(R.id.navPlayerName)
        navEmail = navigationView.getHeaderView(0).findViewById(R.id.navPlayerEmail)
        navPos = navigationView.getHeaderView(0).findViewById(R.id.navPlayerPos)
        navHeight = navigationView.getHeaderView(0).findViewById(R.id.navPlayerHeight)
        navWeight = navigationView.getHeaderView(0).findViewById(R.id.navPlayerWeight)
        navDate = navigationView.getHeaderView(0).findViewById(R.id.navPlayerDate)
        navSex = navigationView.getHeaderView(0).findViewById(R.id.navPlayerSex)
        navNum = navigationView.getHeaderView(0).findViewById(R.id.navPlayerNum)

        playerLogout = navigationView.getHeaderView(0).findViewById(R.id.playerLogout)
        navPlayerEdit = navigationView.getHeaderView(0).findViewById(R.id.navPlayerEdit)
        editProfileListener()

        lifecycleScope.launch {
            DBAdministration.getPlayer(userId)
                .collect { player ->
                    player?.let {
                        Glide.with(requireContext())
                            .asBitmap()
                            .load(player.profileImage)
                            .into(navImg)
                        Glide.with(requireContext())
                            .asBitmap()
                            .load(player.profileImage)
                            .into(playerProfile)

                        profileImage = player.profileImage
                        navName.text = player.username
                        playerUsername.text = player.username
                        navEmail.text = player.email
                        navPos.text = player.position
                        navNum.text = player.number
                        playerNumber.text = player.number
                        playerPosition.text = player.position
                        navHeight.text = player.height + " cm"
                        navWeight.text = player.weight + " kg"
                        navDate.text = player.birthdate
                        navSex.text = setSexText(player.sex!!)
                    }
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTrainings() {
        DBCalification.getTrainingsAssisted(player) { trainings ->
            if (trainings.isNotEmpty()) {
                setSpinner(trainings)
                setCalifications()
            } else {
                txtNoCalificated.text = "NO HA ASISTIDO A NINGÚN ENTRENAMIENTO"
                txtNoCalificated.visibility = View.VISIBLE
                spinner.visibility = View.GONE
                totals.visibility = View.GONE
            }
        }
    }

    private fun setCalifications() {
        DBCalification.getPlayerCalifications(
            selectedDate,
            player
        ) { califications ->
            califications?.let {
                if (califications.isEmpty()) {
                    txtNoCalificated.visibility = View.VISIBLE
                    rvProfileCalif.visibility = View.GONE
                } else {
                    rvProfileCalif.visibility = View.VISIBLE
                    txtNoCalificated.visibility = View.GONE
                    val layoutManager =
                        LinearLayoutManager(
                            requireContext(),
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                    rvProfileCalif.setLayoutManager(layoutManager)
                    val adapter = ProfileCalifAdapter(califications)
                    rvProfileCalif.setAdapter(adapter)
                }
            }
        }
    }

    fun calculateTotals(calif: MutableList<PlayerCalification>): MutableList<AllPlayerCalifications> {
        val sum = mutableMapOf<Pair<String, String>, Double>()
        val count = mutableMapOf<Pair<String, String>, Int>()

        for (c in calif) {
            val key = Pair(c.name!!, c.userId!!)
            sum[key] = sum.getOrDefault(key, 0.0) + c.calification!!.toInt()
            count[key] = count.getOrDefault(key, 0) + 1
        }

        val averageCalif = mutableListOf<AllPlayerCalifications>()
        for ((key, total) in sum) {
            val numCount = count[key] ?: 1
            val average = total / numCount
            averageCalif.add(AllPlayerCalifications(key.second, key.first, average.toString()))
        }

        return averageCalif
    }

    private fun viewTotals() {
        totals.setOnClickListener {
            DBCalification.getAllPlayerCalifications(player) { califications ->
                califications!!.let {
                    if (califications.isEmpty()) {
                        txtNoCalificated.text = "NO TIENE CALIFICACIONES"
                        txtNoCalificated.visibility = View.VISIBLE
                    } else {
                        rvProfileCalif.visibility = View.VISIBLE
                        val totalCalifications = calculateTotals(califications)
                        txtNoCalificated.visibility = View.GONE
                        val layoutManager =
                            LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        rvProfileCalif.setLayoutManager(layoutManager)
                        val adapter = TotalCalificationAdapter(totalCalifications, "PROFILE")
                        rvProfileCalif.setAdapter(adapter)
                    }
                }
            }
        }
    }

    private fun setSexText(sex: String): String {
        when (sex) {
            in UserSex.MALE.toString() -> {
                return getString(R.string.masculino)
            }

            in UserSex.FEMALE.toString() -> {
                return getString(R.string.femenino)
            }

            in UserSex.OTHER.toString() -> {
                return getString(R.string.otro)
            }
        }
        return ""
    }

    private fun logout() {
        playerLogout.setOnClickListener {
            sp = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
            val editor = sp.edit()
            editor.clear().apply()

            val intent = Intent(this@PlayerProfile.requireContext(), Login::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                requireContext(),
                R.anim.transition_fade_in,
                R.anim.transition_fade_out
            )
            startActivity(intent, options.toBundle())
            requireActivity().finish()
        }
    }
}