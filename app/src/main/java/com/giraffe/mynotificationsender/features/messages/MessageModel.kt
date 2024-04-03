package com.giraffe.mynotificationsender.features.messages

import com.giraffe.mynotificationsender.features.home.UserModel
import com.google.firebase.Timestamp


data class MessageModel(
    val id: String?,
    val title: String?,
    val body: String?,
    val time: Timestamp?,
    var users:List<UserModel>? = null
)