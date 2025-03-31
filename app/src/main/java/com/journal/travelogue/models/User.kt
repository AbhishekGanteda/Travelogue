package com.journal.travelogue.models

data class User(
    var id : Int? = null,
    var name : String? = null,
    var email : String? = null,
    var password_hash : String? = null,
    var profile_image : String? = null,
    var followers_count : Int? = null,
    var following_count : Int? = null
)
