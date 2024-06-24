package com.example.tfgkotlin.objects

import android.graphics.Bitmap

data class Match(
    val team1: String? = null,
    val team2: String? = null,
    var date: String? = null,
    val place: String? = null,
    val season: String? = null,
    val league: String? = null,
    val start: String? = null,
    val finish: String? = null,
    var status: String? = null,
    var scoreTeam1: String? = null,
    var scoreTeam2: String? = null,
    var id: String? = null,
)
