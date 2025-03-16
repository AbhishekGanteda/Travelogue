package com.journal.travelogue

data class TravelItem(
    val userName: String,
    val profileImageRes: Int,
    val travelDescription: String,
    val placeImageRes: Int,
    val placeName: String,
    var isLiked: Boolean = false,
    var isSaved: Boolean = false,
    val latitude: Double,
    val longitude: Double
)
