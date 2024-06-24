package com.example.tfgkotlin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgkotlin.R
import com.example.tfgkotlin.activities.MainActivity
import com.example.tfgkotlin.adapters.PlayerAdapter
import com.example.tfgkotlin.adapters.SetCalificationAdapter
import com.example.tfgkotlin.dbmanager.DBCalification
import com.example.tfgkotlin.objects.Calification
import com.example.tfgkotlin.objects.PlayerCalification
import org.w3c.dom.Text

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SetCalifications : Fragment() {

    private var idTraining: String? = null
    private var action: String? = null
    private lateinit var rvCalifications: RecyclerView
    private lateinit var califications: List<Calification>
    private lateinit var acceptSetCalif: ImageView
    private lateinit var setCalifback: ImageView
    private lateinit var txtNoAssisted: TextView
    private lateinit var adapter: SetCalificationAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            idTraining = it.getString(ARG_PARAM1)
            action = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_set_califications, container, false)

        setAttributes(view)
        califications = listOf(
            Calification("ACTITUD"),
            Calification("CONCENTRACIÓN"),
            Calification("INICIATIVA"),
            Calification("FALTAS"),
            Calification("CONTACTOS"),
            Calification("SALIDAS FALSAS"),
            Calification("PASES RECEPCIONADOS"),
            Calification("FLAGS QUITADOS"),
            Calification("DESEMPEÑO"),
            Calification("INTERCEPCIONES")
        )

        if (action == "SET") {
            setCalifications()
        } else if (action == "UPDATE") {
            updateCalifications()
        }

        setBack()
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SetCalifications().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setAttributes(view: View) {
        rvCalifications = view.findViewById(R.id.rvCalifications)
        acceptSetCalif = view.findViewById(R.id.acceptSetCalif)
        setCalifback = view.findViewById(R.id.setCalifback)
        txtNoAssisted = view.findViewById(R.id.txtNoAssisted)
    }

    private fun setBack() {
        setCalifback.setOnClickListener {
            (activity as MainActivity).changeFragmentTransition(Administration())
        }
    }

    private fun setCalifications() {
        DBCalification.getPlayersTraining(idTraining!!) { users ->
            if (users.isNotEmpty()) {
                acceptSetCalif.visibility = View.VISIBLE
                txtNoAssisted.visibility = View.GONE
                val layoutManager =
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                rvCalifications.setLayoutManager(layoutManager)

                for (cal in califications) {
                    var copyList = mutableListOf<PlayerCalification>()
                    for (pl in users) {
                        copyList.add(PlayerCalification(pl.name, pl.userId, pl.calification))
                    }
                    cal.players = copyList
                }
                adapter = SetCalificationAdapter(califications)
                rvCalifications.setAdapter(adapter)
                setAccept()
            } else {
                acceptSetCalif.visibility = View.GONE
                txtNoAssisted.visibility = View.VISIBLE
            }
        }
    }

    private fun updateCalifications() {
        DBCalification.getPlayersTraining(idTraining!!) { users ->
            if (users.isNotEmpty()) {
                acceptSetCalif.visibility = View.VISIBLE
                txtNoAssisted.visibility = View.GONE
                val layoutManager =
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                rvCalifications.setLayoutManager(layoutManager)

                for (cal in califications) {
                    var copyList = mutableListOf<PlayerCalification>()
                    for (pl in users) {
                        copyList.add(PlayerCalification(pl.name, pl.userId, pl.calification))
                    }
                    cal.players = copyList
                }
                adapter = SetCalificationAdapter(califications)
                rvCalifications.setAdapter(adapter)
                setUpdate()
            } else {
                acceptSetCalif.visibility = View.GONE
                txtNoAssisted.visibility = View.VISIBLE
            }
        }
    }

    private fun setAccept() {
        acceptSetCalif.setOnClickListener {
            val calificationsList = adapter.getCalifications()
            DBCalification.setCalifications(calificationsList, idTraining!!)
            (activity as MainActivity).changeFragmentTransition(Administration())
        }
    }

    private fun setUpdate() {
        acceptSetCalif.setOnClickListener {
            val calificationsList = adapter.getCalifications()
            DBCalification.getCalificationId(idTraining!!) { califId ->
                DBCalification.setCalifications(calificationsList, idTraining!!, califId)
                (activity as MainActivity).changeFragmentTransition(Administration())
            }
        }
    }
}