package com.giraffe.mynotificationsender.features.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.giraffe.mynotificationsender.databinding.MessageRowBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesAdapter(
    private val messages:MutableList<MessageModel>,
    private val onMessageClick: OnMessageClick
): Adapter<MessagesAdapter.MessageVH>() {
    class MessageVH(val binding: MessageRowBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageVH {
        val binding = MessageRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MessageVH(binding)
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessageVH, position: Int) {
        val message = messages[position]
        holder.binding.tvTitle.text = message.title
        holder.binding.tvMsg.text = message.body
        //holder.binding.tvDate.text = message.time?.toDate().toString()
        holder.binding.tvDate.text = convertDate(message.time)
        holder.binding.root.setOnClickListener {
            onMessageClick.onClick(message,position)
        }
    }

    fun updateAllList(messages :MutableList<MessageModel>){
        this.messages.clear()
        this.messages.addAll(messages)
        notifyDataSetChanged()
    }


    interface OnMessageClick{
        fun onClick(message: MessageModel, position: Int)
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