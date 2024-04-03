package com.giraffe.mynotificationsender.features.client

import com.google.firebase.Timestamp

data class ClientModel(
    val id:String,
    val email:String,
    val timestamp: Timestamp
)