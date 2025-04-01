package com.journal.travelogue.models


data class Post(
    var id : Int? = null,
    var user_id : Int? = null,
    var place_name : String? = null,
    var description : String? = null,
    var image : String? = null,
    var latitude : Double? = null,
    var longitude : Double? = null
)
