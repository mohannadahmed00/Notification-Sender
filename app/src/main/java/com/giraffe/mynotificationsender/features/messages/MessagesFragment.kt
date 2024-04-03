package com.giraffe.mynotificationsender.features.messages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.giraffe.mynotificationsender.databinding.FragmentMessagesBinding
import com.giraffe.mynotificationsender.features.home.HomeFragment
import com.giraffe.mynotificationsender.features.home.UserModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesFragment : Fragment(),MessagesAdapter.OnMessageClick {
    companion object {
        private const val TAG = "MessagesFragment"
    }

    private lateinit var binding: FragmentMessagesBinding
    private lateinit var adapter: MessagesAdapter
    private val messages = mutableListOf<MessageModel>()
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
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MessagesAdapter(messages,this)
        binding.rvMessages.adapter = adapter
        getMessages()
    }

    private fun getMessages() {
        FirebaseFirestore.getInstance().collection("notifications")
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val list = mutableListOf<MessageModel>()
                    for (document in it.result) {
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
                    Log.e(TAG, "getMessages: ", it.exception)
                }
            }
    }

    override fun onClick(message: MessageModel, position: Int) {
        Log.i(TAG, "onClick: $message")
        val action = MessagesFragmentDirections.actionMessagesFragmentToUsersFragment(message.id?:"")
        findNavController().navigate(action)
    }


}