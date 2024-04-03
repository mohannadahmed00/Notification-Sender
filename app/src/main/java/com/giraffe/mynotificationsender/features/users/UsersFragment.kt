package com.giraffe.mynotificationsender.features.users

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.giraffe.mynotificationsender.R
import com.giraffe.mynotificationsender.databinding.FragmentUsersBinding
import com.giraffe.mynotificationsender.features.home.UserModel
import com.giraffe.mynotificationsender.features.messages.MessageModel
import com.giraffe.mynotificationsender.features.messages.MessagesAdapter
import com.giraffe.mynotificationsender.features.messages.MessagesFragment
import com.google.firebase.firestore.FirebaseFirestore

class UsersFragment : Fragment() {
    companion object {
        private const val TAG = "UsersFragment"
    }

    private lateinit var binding: FragmentUsersBinding
    private val args: UsersFragmentArgs by navArgs()
    private lateinit var adapter: SeenUsersAdapter
    private val users = mutableListOf<UserModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = SeenUsersAdapter(users)
        binding.rvUsers.adapter = adapter
        getUsers()
    }

    private fun getUsers() {
        FirebaseFirestore.getInstance().collection("notifications")
            .document(args.messageId)
            .collection("users")
            .addSnapshotListener { value, error ->
                if (value?.documents!=null) {
                    val list = mutableListOf<UserModel>()
                    for (document in value.documents) {
                        val user = UserModel(
                            id = document.id,
                            email = document.getString("email"),
                            seen = document.getTimestamp("seen")
                        )
                        Log.d(TAG, "getUsers: $user")
                        list.add(user)
                    }
                    Log.i(TAG, "getUsers: $list")
                    adapter.updateAllList(list)
                } else {
                    Log.e(TAG, "getUsers: ", error)
                }
            }
            /*.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val list = mutableListOf<UserModel>()
                    for (document in it.result) {
                        val user = UserModel(
                            id = document.id,
                            email = document.getString("email"),
                            seen = document.getTimestamp("seen")
                        )
                        Log.d(TAG, "getUsers: $user")
                        list.add(user)
                    }
                    Log.i(TAG, "getUsers: $list")
                    adapter.updateAllList(list)
                } else {
                    Log.e(TAG, "getUsers: ", it.exception)
                }
            }*/

    }


}