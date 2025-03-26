package com.journal.travelogue

data class TravelItem(
    val userName: String,
    val profileImageRes: Int,
    val travelDescription: String,
    val placeImageRes: Int,
    val placeName: String,
    var isLiked: Boolean = false,
    var likeCount : Int =0,
    var isSaved: Boolean = false,
    var savedCount : Int = 0,
    val latitude: Double,
    val longitude: Double
)
