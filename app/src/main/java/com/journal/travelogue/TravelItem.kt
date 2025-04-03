package com.journal.travelogue

data class TravelItem(
    val userId: Int?,
    val postId: Int?,
    var userName: String?,
    var profileImageRes: String?,
    var travelDescription: String?,
    var placeImageRes: String?,
    var placeName: String?,
    var isFollowed : Boolean?,
    var isLiked: Boolean?,
    var likeCount : Int?,
    var isSaved: Boolean?,
    var savedCount : Int?,
    var latitude: Double?,
    var longitude: Double?
)
