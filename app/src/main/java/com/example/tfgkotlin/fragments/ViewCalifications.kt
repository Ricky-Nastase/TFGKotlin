package com.example.tfgkotlin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tfgkotlin.R
import com.example.tfgkotlin.adapters.DeletePlayerAdapter
import com.example.tfgkotlin.adapters.PlayerCalifAdapter
import com.example.tfgkotlin.adapters.ProfileCalifAdapter
import com.example.tfgkotlin.adapters.SetCalificationAdapter
import com.example.tfgkotlin.adapters.TotalCalificationAdapter
import com.example.tfgkotlin.dbmanager.DBAdministration
import com.example.tfgkotlin.dbmanager.DBCalification
import com.example.tfgkotlin.objects.AllPlayerCalifications
import com.example.tfgkotlin.objects.Calification
import com.example.tfgkotlin.objects.PlayerCalification
import kotlinx.coroutines.launch

class ViewCalifications : Fragment() {

    private lateinit var rvViewCalifications: RecyclerView
    private lateinit var constraintTotals: ConstraintLayout
    private lateinit var layFirst: LinearLayout
    private lateinit var laySecond: LinearLayout
    private lateinit var layThird: LinearLayout
    private lateinit var imgFirst: ImageView
    private lateinit var nameFirst: TextView
    private lateinit var califFirst: TextView

    private lateinit var imgSecond: ImageView
    private lateinit var nameSecond: TextView
    private lateinit var califSecond: TextView

    private lateinit var imgThird: ImageView
    private lateinit var nameThird: TextView
    private lateinit var califThird: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_califications, container, false)
        setAttributes(view)
        setCalifications()
        return view
    }

    private fun setAttributes(view: View) {
        rvViewCalifications = view.findViewById(R.id.rvViewCalifications)
        constraintTotals = view.findViewById(R.id.constraintTotals)
        nameFirst = view.findViewById(R.id.nameFirst)
        imgFirst = view.findViewById(R.id.imgFirst)
        califFirst = view.findViewById(R.id.califFirst)
        nameSecond = view.findViewById(R.id.nameSecond)
        imgSecond = view.findViewById(R.id.imgSecond)
        califSecond = view.findViewById(R.id.califSecond)
        nameThird = view.findViewById(R.id.nameThird)
        imgThird = view.findViewById(R.id.imgThird)
        califThird = view.findViewById(R.id.califThird)
        layFirst = view.findViewById(R.id.layFirst)
        laySecond = view.findViewById(R.id.laySecond)
        layThird = view.findViewById(R.id.layThird)

    }

    private fun setCalifications() {
        lifecycleScope.launch {
            DBAdministration.getPlayersActive()
                .collect { players ->
                    if (players.isEmpty()) {
                        constraintTotals.visibility = View.GONE
                    } else {
                        DBCalification.getTotals(players) { califications ->
                            val sortedList =
                                califications.sortedByDescending { it.calification!!.toDouble() }.toMutableList()

                            val size = sortedList.size
                            if (size == 1) {
                                laySecond.visibility = View.GONE
                                layThird.visibility = View.GONE
                            } else if (size == 2) {
                                layThird.visibility = View.GONE
                            }

                            for (i in 0 until size) {
                                when (i) {
                                    0 -> {
                                        Glide.with(requireContext())
                                            .asBitmap()
                                            .load(sortedList[i].profileImage)
                                            .into(imgFirst)
                                        nameFirst.text = sortedList[i].calificationName
                                        val formattedCalif = String.format("%.1f", sortedList[i].calification!!.toDouble())
                                        califFirst.text = formattedCalif
                                    }

                                    1 -> {
                                        Glide.with(requireContext())
                                            .asBitmap()
                                            .load(sortedList[i].profileImage)
                                            .into(imgSecond)
                                        nameSecond.text = sortedList[i].calificationName
                                        val formattedCalif = String.format("%.1f", sortedList[i].calification!!.toDouble())
                                        califSecond.text = formattedCalif
                                    }

                                    2 -> {
                                        Glide.with(requireContext())
                                            .asBitmap()
                                            .load(sortedList[i].profileImage)
                                            .into(imgThird)
                                        nameThird.text = sortedList[i].calificationName
                                        val formattedCalif = String.format("%.1f", sortedList[i].calification!!.toDouble())
                                        califThird.text = formattedCalif
                                    }
                                }
                            }

                            if (size == 1) {
                                sortedList.removeAt(0)
                            } else if (size == 2) {
                                sortedList.removeAt(1)
                                sortedList.removeAt(0)
                            } else if (size >= 3) {
                                sortedList.removeAt(2)
                                sortedList.removeAt(1)
                                sortedList.removeAt(0)
                            }

                            val layoutManager =
                                LinearLayoutManager(
                                    requireContext(),
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )
                            rvViewCalifications.setLayoutManager(layoutManager)
                            val adapter = TotalCalificationAdapter(sortedList,"TOTALS")
                            rvViewCalifications.setAdapter(adapter)
                        }
                    }
                }
        }
    }

}