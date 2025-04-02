package com.journal.travelogue

data class TravelItem(
    val userId: Int?,
    val postId: Int?,
    val userName: String?,
    val profileImageRes: String?,
    val travelDescription: String?,
    val placeImageRes: String?,
    val placeName: String?,
    var isFollowed : Boolean?,
    var isLiked: Boolean?,
    var likeCount : Int?,
    var isSaved: Boolean?,
    var savedCount : Int?,
    val latitude: Double?,
    val longitude: Double?
)
