package com.giraffe.mynotificationsender

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.giraffe.mynotificationsender.databinding.ActivityMainBinding
import com.giraffe.mynotificationsender.features.home.UserModel
import com.giraffe.mynotificationsender.features.home.UsersAdapter


class MainActivity : AppCompatActivity()/*, UsersAdapter.OnUserClick*/ {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private val users = mutableListOf<UserModel>()
    private lateinit var adapter: UsersAdapter
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)
        navController.addOnDestinationChangedListener { _: NavController?, navDestination: NavDestination, _: Bundle? ->
            if (navDestination.id == R.id.homeFragment || navDestination.id == R.id.messagesFragment) {
                binding.bottomNavView.visibility = View.VISIBLE
            } else {
                binding.bottomNavView.visibility = View.GONE
            }
        }
        /*adapter = UsersAdapter(users, this)
        binding.rvUsers.adapter = adapter
        getUsers()
        handleClicks()*/
    }

    /*private fun getUsers() {
        lifecycleScope.launch {
            FirebaseFirestore.getInstance().collection("users")
                .get().addOnCompleteListener {
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
                }
        }
    }

    private fun handleClicks() {
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
            if (title.isNotBlank() && body.isNotBlank()) {
                val notificationId = storeNotification(title, body)
                adapter.getSelectedUsers().forEach {
                    it.fcmToken?.let { token ->
                        sendNotification(token, title, body, notificationId)
                        registerUsers(it.email?:"",notificationId, token)
                    }
                }
            }
        }
    }

    private fun registerUsers(email:String,notificationId: String, userToken: String) {
        val map = HashMap<String, String>()
        map["email"] = email
        map["seen"] = "false"
        map["time"] = Timestamp.now().toString()
        FirebaseFirestore.getInstance().collection("notifications")
            .document(notificationId)
            .collection("users")
            .document(userToken)
            .set(map)
    }

    private fun storeNotification(title: String, body: String): String {
        val map = HashMap<String, String>()
        map["title"] = title
        map["body"] = body
        map["time"] = Timestamp.now().toString()
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
    }*/

}