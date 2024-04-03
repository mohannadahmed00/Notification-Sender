package com.giraffe.mynotificationsender.features.home

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.giraffe.mynotificationsender.R
import com.giraffe.mynotificationsender.databinding.UserRowBinding

class UsersAdapter(
    private val users:MutableList<UserModel>,
    private val onUserClick: OnUserClick
): Adapter<UsersAdapter.UserVH>() {
    class UserVH(val binding: UserRowBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        val binding = UserRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UserVH(binding)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserVH, position: Int) {
        val user = users[position]
        holder.binding.tvDate.visibility = View.GONE
        holder.binding.tvUser.text = user.email
        if (user.isSelected){
            val typedValue = TypedValue()
            holder.binding.root.context.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary,typedValue,true)
            holder.binding.tvUser.setTextColor(ContextCompat.getColor(holder.binding.root.context, typedValue.resourceId))
        }else{
            holder.binding.tvUser.setTextColor(ContextCompat.getColor(holder.binding.root.context,
                R.color.dark_gray
            ))
        }
        holder.binding.root.setOnClickListener {
            onUserClick.onClick(user,position)
        }
    }

    fun updateAllList(users:MutableList<UserModel>){
        this.users.clear()
        this.users.addAll(users)
        notifyDataSetChanged()
    }

    fun updateItem(user: UserModel, position: Int){
        this.users.replaceAll { if(it.id == user.id){
            user
        }else{
            it
        } }
        notifyItemChanged(position)
    }

    fun selectAll(){
        this.users.forEach{
            it.isSelected = true
        }
        notifyDataSetChanged()
    }

    fun unSelectAll(){
        this.users.forEach{
            it.isSelected = false
        }
        notifyDataSetChanged()
    }

    fun isAllSelected():Boolean{
        return users.none { !it.isSelected }
    }

    fun isAllUnSelected():Boolean{
        return users.none { it.isSelected }
    }

    fun getSelectedUsers():List<UserModel>{
        return users.filter {
            it.isSelected
        }
    }

    interface OnUserClick{
        fun onClick(user: UserModel, position: Int)
    }
}