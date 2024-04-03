package com.giraffe.mynotificationsender.features.home

import com.google.firebase.Timestamp

data class UserModel(
    val id: String? = null,
    val fcmToken: String? = null,
    val email: String? = null,
    var isSelected: Boolean = false,
    val seen:Timestamp? = null
)