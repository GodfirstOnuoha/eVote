package com.chibufirst.evote.models

data class Nominee(
    val image: String? = null,
    val name: String? = null,
    val email: String? = null,
    val position: String? = null,
    val level: String? = null,
    val votes: Int? = null
)
