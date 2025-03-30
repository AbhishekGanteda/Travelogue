package com.journal.travelogue.models

data class LoginResponse(
    val token: String,
    val user: User
)
