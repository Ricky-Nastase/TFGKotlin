package com.example.tfgkotlin.objects

data class Player(
    val id: String? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val weight: String? = null,
    val height: String? = null,
    val position: String? = null,
    val birthdate: String? = null,
    val number: String? = null,
    val sex: String? = null,
    val type: UserType? = null,
    val active: Boolean? = null,
    val profileImage: String? = null
)
