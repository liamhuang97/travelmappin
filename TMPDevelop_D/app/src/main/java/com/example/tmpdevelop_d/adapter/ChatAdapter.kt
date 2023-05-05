package com.example.tmpdevelop_d.adapter

import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmpdevelop_d.R
import com.example.tmpdevelop_d.users.Message
import com.example.tmpdevelop_d.users.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    companion object {
        private const val MESSAGE_TYPE_SENT = 0
        private const val MESSAGE_TYPE_RECEIVED = 1
    }

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val usersRef = Firebase.firestore.collection("Users")

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) {
            MESSAGE_TYPE_SENT
        } else {
            MESSAGE_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = if (viewType == MESSAGE_TYPE_SENT) {
            layoutInflater.inflate(R.layout.item_message_sent, parent, false)
        } else {
            layoutInflater.inflate(R.layout.item_message_received, parent, false)
        }
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message_text_view)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestamp_text_view)
        private val senderAvatar: CircleImageView? = itemView.findViewById(R.id.sender_avatar)

        fun bind(message: Message) {
            messageTextView.text = message.text
            timestampTextView.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

            if (senderAvatar != null && getItemViewType(adapterPosition) == MESSAGE_TYPE_RECEIVED) {
                loadSenderAvatar(message.senderId)
            }
        }

        private fun loadSenderAvatar(senderId: String) {
            usersRef.document(senderId).get().addOnSuccessListener { document ->
                val user = document.toObject(Users::class.java)
                if (user != null) {
                    Glide.with(itemView)
                        .load(user.imageUrl)
                        .into(senderAvatar!!)
                }
            }.addOnFailureListener { exception ->
                Log.d("ChatAdapter", "Error loading sender avatar: ", exception)
            }
        }
    }
}