package com.example.tfgkotlin.objects

data class Administrator(
    val id: String? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val type: UserType? = null,
    val active: Boolean? = null,
    val profileImage: String? = null
)
