package com.journal.travelogue.models

data class User(
    val id : Int? = null,
    val name : String? = null,
    val email : String,
    val password : String,
    val image : String? = null
)
