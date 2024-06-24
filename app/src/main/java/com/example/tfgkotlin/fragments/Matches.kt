package com.example.tfgkotlin.fragments

import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfgkotlin.R
import com.example.tfgkotlin.activities.MainActivity
import com.example.tfgkotlin.adapters.ModifyMatchAdapter
import com.example.tfgkotlin.adapters.MatchAdapter
import com.example.tfgkotlin.dbmanager.DBAdministration
import com.example.tfgkotlin.objects.ActionMatch
import java.text.SimpleDateFormat

class Matches : Fragment() {
    private lateinit var rvMatches: RecyclerView
    private var action: String? = null
    private lateinit var matchesBack: ImageView
    private lateinit var ctw: ContextThemeWrapper
    private lateinit var txtNoMatchesFound: TextView
    private lateinit var titleMatches: TextView

    companion object {
        private const val ARG_PARAM = "action"

        fun newInstance(param: String): Matches {
            val fragment = Matches()
            val args = Bundle()
            args.putString(ARG_PARAM, param)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            action = it.getString(ARG_PARAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_matches, container, false)
        setAttributes(view)
        setTitle()

        DBAdministration.updateMatchesStatus { updated ->
            if (updated) {
                if (!action.isNullOrEmpty()) {
                    setModify(action!!)
                } else {
                    DBAdministration.updateMatchesStatus { updated ->
                        if (updated) {
                            setMatches()
                        }
                    }
                }
            }
        }

        return view
    }

    private fun setAttributes(view: View) {
        rvMatches = view.findViewById(R.id.rvMatches)
        matchesBack = view.findViewById(R.id.matchesBack)
        ctw = ContextThemeWrapper(requireContext(), R.style.CustomSnackbarTheme)
        txtNoMatchesFound = view.findViewById(R.id.txtNoMatchesFound)
        titleMatches = view.findViewById(R.id.titleMatches)
    }

    private fun setTitle() {
        if (!action.isNullOrEmpty()) {
            when (action) {
                ActionMatch.FINISH.toString() -> titleMatches.text = "FINALIZAR PARTIDO"
                ActionMatch.CANCEL.toString() -> titleMatches.text = "CANCELAR PARTIDO"
                ActionMatch.DELETE.toString() -> titleMatches.text = "ELIMINAR PARTIDO"
            }
        } else {
            titleMatches.text = getString(R.string.partidos)
        }
    }

    private fun setMatches() {
        DBAdministration.getMatches() { matches ->
            if (matches.isEmpty()) {
                txtNoMatchesFound.visibility = View.VISIBLE
            } else {
                txtNoMatchesFound.visibility = View.GONE
                val layoutManager =
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                rvMatches.setLayoutManager(layoutManager)
                val adapter = MatchAdapter(matches.sortedByDescending {
                    it.date?.let { date ->
                        SimpleDateFormat("d/M/yyyy").parse(date)
                    }
                }, requireContext())
                rvMatches.setAdapter(adapter)
            }
        }
    }

    private fun setModify(action: String) {
        setBack()
        DBAdministration.getMatches() { matches ->
            if (matches.isEmpty()) {
                txtNoMatchesFound.visibility = View.VISIBLE
            } else {
                txtNoMatchesFound.visibility = View.GONE
                val layoutManager =
                    LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                rvMatches.setLayoutManager(layoutManager)
                val adapter =
                    ModifyMatchAdapter(matches.sortedByDescending {
                        it.date?.let { date ->
                            SimpleDateFormat("d/M/yyyy").parse(date)
                        }
                    }.toMutableList(), requireContext(), ctw, action)
                rvMatches.setAdapter(adapter)
            }
        }
    }

    private fun setBack() {
        matchesBack.visibility = View.VISIBLE
        matchesBack.setOnClickListener {
            (activity as MainActivity).changeFragmentTransition(Administration())
        }
    }
}