package com.giraffe.mynotificationsender.features.users

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.giraffe.mynotificationsender.R
import com.giraffe.mynotificationsender.databinding.UserRowBinding
import com.giraffe.mynotificationsender.features.home.UserModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SeenUsersAdapter(
    private val users:MutableList<UserModel>,
): Adapter<SeenUsersAdapter.UserVH>() {
    class UserVH(val binding: UserRowBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        val binding = UserRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserVH(binding)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserVH, position: Int) {
        val user = users[position]
        holder.binding.tvUser.text = user.email
        if (user.seen!=null){
            holder.binding.tvDate.visibility = View.VISIBLE
            //holder.binding.tvDate.text = user.seen.toDate().toString()
            holder.binding.tvDate.text = convertDate(user.seen)
        }else{
            holder.binding.tvDate.visibility = View.GONE
        }
    }

    fun updateAllList(users:MutableList<UserModel>){
        this.users.clear()
        this.users.addAll(users)
        notifyDataSetChanged()
    }
    private fun convertDate(timestamp: Timestamp?):String{
        return if (timestamp!=null) {
            val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
            val sdf = SimpleDateFormat("HH:mm EEEE, MMM d, yyyy", Locale.US)
            val netDate = Date(milliseconds)
            sdf.format(netDate).toString()
        }else {
            ""
        }
    }
}