package com.example.tfgkotlin.objects

data class Calification(
    var name: String? = null,
    var visible: Boolean = false,
    var players: MutableList<PlayerCalification>? = null
)
