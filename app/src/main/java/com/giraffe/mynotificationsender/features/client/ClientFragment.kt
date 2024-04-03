package com.giraffe.mynotificationsender.features.client

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.giraffe.mynotificationsender.FirebaseUtils
import com.giraffe.mynotificationsender.databinding.FragmentClientBinding
import com.giraffe.mynotificationsender.features.messages.MessageModel
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging


class ClientFragment : Fragment() {
    companion object {
        private const val TAG = "ClientFragment"
    }
    private lateinit var adapter: MessagesAdapter
    private val messages = mutableListOf<MessageModel>()
    private lateinit var binding: FragmentClientBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        binding.tvEmail.text = FirebaseAuth.getInstance().currentUser?.email ?: ""
        adapter = MessagesAdapter(messages)
        binding.rvMessages.adapter = adapter
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && !notificationManager.isNotificationPolicyAccessGranted
        ) {
            val intent = Intent(
                Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            )
            startActivity(intent)
        }
        Firebase.messaging.isAutoInitEnabled = true
        getNotifications()
        handleClicks()
        getFCMToken()
    }


    private fun getNotifications() {
        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseUtils.currentUserId() ?: "")
            .collection("notifications")
            .addSnapshotListener { value, error ->
                if (value?.documents != null) {
                    val list = mutableListOf<MessageModel>()
                    for (document in value.documents) {
                        Log.i(TAG, "getNotifications: ${document.getTimestamp("seen")}")
                        if(document.getTimestamp("seen")==null){
                            makeItSeen(document.id)
                        }

                        val message = MessageModel(
                            document.id,
                            document.getString("title"),
                            document.getString("body"),
                            document.getTimestamp("time")
                        )
                        Log.d(TAG, "getMessages: $message")
                        list.add(message)
                    }
                    Log.i(TAG, "getMessages: $list")
                    adapter.updateAllList(list)
                } else {
                    Log.e(TAG, "getMessages: ", error)
                }
            }
    }

    private fun makeItSeen(notificationId: String) {
        Log.d(TAG, "makeItSeen: ${FirebaseUtils.currentUserId() ?: ""}")
        Log.d(TAG, "makeItSeen: $notificationId")
        val map = hashMapOf<String, Any>()
        map["seen"] = Timestamp.now()
        if (FirebaseAuth.getInstance().currentUser!=null) {
            FirebaseFirestore.getInstance().collection("notifications")
                .document(notificationId)
                .collection("users")
                .document(FirebaseUtils.currentUserId() ?: "")
                .update(map)
            FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseUtils.currentUserId() ?: "")
                .collection("notifications")
                .document(notificationId)
                .update(map)
        }
    }

    private fun handleClicks() {
        binding.bntLogout.setOnClickListener {
            logout()
        }

    }

    private fun logout() {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.e(TAG, "logout: ", it.exception)
            } else {
                FirebaseAuth.getInstance().signOut()
                val action = ClientFragmentDirections.actionClientFragmentToLoginFragment()
                findNavController().navigate(action)
            }
        }

    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.e(TAG, "getFCMToken: ", it.exception)
            } else {
                val token = it.result
                FirebaseUtils.currentUserDetails().update("fcmToken", token)
                Log.i(TAG, "getFCMToken: $token")
            }
        }
    }

}