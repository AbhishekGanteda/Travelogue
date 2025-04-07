package com.journal.travelogue

data class SearchItem(
    var postId: Int? = null,
    var userId: Int? = null,
    var name: String? = null,
    var profile : String? = null,
    var place_name : String? = null,
    var description : String? = null,
    var placeImage : String? = null
)
