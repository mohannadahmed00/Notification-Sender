package com.giraffe.mynotificationsender

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {

    fun currentUserId() = FirebaseAuth.getInstance().currentUser?.uid

    fun currentUserDetails()  = FirebaseFirestore.getInstance().collection("users").document(
        currentUserId() ?:"")

    fun isLoggedIn() = FirebaseAuth.getInstance().currentUser != null
}