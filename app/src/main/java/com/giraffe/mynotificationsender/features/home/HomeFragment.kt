package com.giraffe.mynotificationsender.features.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.giraffe.mynotificationsender.databinding.FragmentHomeBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment(), UsersAdapter.OnUserClick {
    companion object {
        private const val TAG = "HomeFragment"
    }
    private lateinit var handler:Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private lateinit var binding: FragmentHomeBinding
    private val users = mutableListOf<UserModel>()
    private lateinit var adapter: UsersAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = UsersAdapter(users, this)
        binding.rvUsers.adapter = adapter
        getUsers()
        handleClicks()
    }


    private fun getUsers() {
        lifecycleScope.launch {
            FirebaseFirestore.getInstance().collection("users")
                .addSnapshotListener { value, error ->
                    if (value?.documents!=null) {
                        val list = mutableListOf<UserModel>()
                        for (document in value.documents) {
                            val user = UserModel(
                                document.getString("id"),
                                document.getString("fcmToken"),
                                document.getString("email")
                            )
                            Log.d(TAG, "getUsers: $user")
                            list.add(user)
                        }
                        Log.i(TAG, "getUsers: $list")
                        adapter.updateAllList(list)
                        Log.i(TAG, "getUsers: $users")
                    } else {
                        Log.e(TAG, "getUsers: ", error)
                    }
                }

                /*.get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        val list = mutableListOf<UserModel>()
                        for (document in it.result) {
                            val user = UserModel(
                                document.getString("id"),
                                document.getString("fcmToken"),
                                document.getString("email")
                            )
                            Log.d(TAG, "getUsers: $user")
                            list.add(user)
                        }
                        Log.i(TAG, "getUsers: $list")
                        adapter.updateAllList(list)
                        Log.i(TAG, "getUsers: $users")
                    } else {
                        Log.e(TAG, "getUsers: ", it.exception)
                    }
                }*/
        }
    }

    private fun handleClicks() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            findNavController().navigate(action)
        }
        binding.tvSelectAll.setOnClickListener {
            if (binding.tvSelectAll.text == "select all") {
                adapter.selectAll()
                binding.tvSelectAll.text = "unselect all"
            } else {
                adapter.unSelectAll()
                binding.tvSelectAll.text = "select all"
            }
        }
        binding.btnSend.setOnClickListener {
            Log.d(TAG, "handleClicks: ${adapter.getSelectedUsers()}")
            val title = binding.edtTitle.text.toString()
            val body = binding.edtBody.text.toString()
            if (isValid(title,body)) {
                val notificationId = storeNotification(title, body)
                adapter.getSelectedUsers().forEach {
                    it.fcmToken?.let { token ->
                        sendNotification(token, title, body, notificationId)
                        registerUsers(it.email ?: "", notificationId, it.id?:"",title,body)
                    }
                }
            }
        }
    }

    private fun isValid(title: String,body: String):Boolean{

        if (title.isBlank()){
            Toast.makeText(requireContext(),"title is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (body.isBlank()){
            Toast.makeText(requireContext(),"body is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (adapter.getSelectedUsers().isEmpty()){
            Toast.makeText(requireContext(),"no user has been selected", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registerUsers(email: String, notificationId: String, userToken: String,title: String,body: String) {
        val map = HashMap<String, Any>()
        map["email"] = email
        //map["seen"] = false
        //map["time"] = FieldValue.serverTimestamp()
        FirebaseFirestore.getInstance().collection("notifications")
            .document(notificationId)
            .collection("users")
            .document(userToken)
            .set(map)

        val notificationMap = HashMap<String, Any>()
        notificationMap["title"] = title
        notificationMap["body"] = body
        notificationMap["time"] = Timestamp.now()
        FirebaseFirestore.getInstance().collection("users")
            .document(userToken)
            .collection("notifications")
            .document(notificationId)
            .set(notificationMap)
    }

    private fun storeNotification(title: String, body: String): String {
        val map = HashMap<String, Any>()
        map["title"] = title
        map["body"] = body
        map["time"] = Timestamp.now()
        val ref = FirebaseFirestore.getInstance().collection("notifications").document()
        val id = ref.id
        ref.set(map)
        Log.i(TAG, "storeNotification: $id")
        return id
    }

    private fun sendNotification(
        token: String,
        title: String,
        body: String,
        notificationId: String
    ) {
        try {
            val jsonObj = JSONObject()
            val jsonData = JSONObject()
            jsonData.put("notificationId", notificationId)
            jsonData.put("title", title)
            jsonData.put("body", body)
            jsonObj.put("data", jsonData)
            jsonObj.put("to", token)
            callApi(jsonObj)
        } catch (e: Exception) {
            Log.e(TAG, "sendNotification: ", e)
        }
    }

    private fun callApi(jsonObj: JSONObject) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val requestBody = RequestBody.create(mediaType, jsonObj.toString())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header(
                "Authorization",
                "Bearer AAAALuhp_Lc:APA91bHX9VclmQ0-4FDU8_KwiMOJ4IK1A4W9uFyD1p027qJbzszT0Ki-ew8vnz1A94Q1YnggMKzBP-yrmU-BpyhWoo4Sj5thh-dw5UtAVp4m5JnLxMwIpefaSwZ979vMj0kxTgQTizIT"
            )
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure: ", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "onResponse: ")
                handler.post {
                    binding.edtTitle.setText("")
                    binding.edtBody.setText("")
                }
            }

        })
    }
    override fun onClick(user: UserModel, position: Int) {
        user.isSelected = !user.isSelected
        adapter.updateItem(user, position)
        if (adapter.isAllSelected()) {
            binding.tvSelectAll.text = "unselect all"
        } else if (adapter.isAllUnSelected()) {
            binding.tvSelectAll.text = "select all"
        }
    }
}