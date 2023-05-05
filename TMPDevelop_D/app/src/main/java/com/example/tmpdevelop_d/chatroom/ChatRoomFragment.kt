package com.example.tmpdevelop_d.chatroom

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmpdevelop_d.R
import com.example.tmpdevelop_d.adapter.ChatAdapter
import com.example.tmpdevelop_d.users.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChatRoomFragment : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private val messages = arrayListOf<Message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 設置聊天室佈局
        val view = inflater.inflate(R.layout.fragment_chat_room, container, false)

        // 初始化 RecyclerView 以顯示聊天消息
        chatRecyclerView = view.findViewById(R.id.chat_recycler_view)
        chatRecyclerView.layoutManager = LinearLayoutManager(activity)

        // 設置適配器以顯示消息
        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.adapter = chatAdapter

        // 初始化 EditText 和 Button，讓用戶輸入和發送消息
        messageEditText = view.findViewById(R.id.message_edit_text)
        sendButton = view.findViewById(R.id.send_button)

        // 設置發送按鈕的點擊事件監聽器
        sendButton.setOnClickListener {
            sendMessage()
        }

        // 獲取聊天室內的消息並更新 RecyclerView
        fetchMessagesAndUpdateRecyclerView()

        return view
    }

    private fun fetchMessagesAndUpdateRecyclerView() {
        val groupId = requireArguments().getString("groupId")!!
        val db = Firebase.firestore
        val messagesRef = db.collection("GroupMessages").document(groupId).collection("Message")

        messagesRef.orderBy("timestamp").addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.d(TAG, "Error getting messages: ", exception)
                return@addSnapshotListener
            }

            messages.clear()
            snapshot?.let {
                for (document in it) {
                    val message = document.toObject(Message::class.java)
                    messages.add(message)
                }
                chatAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()

        if (messageText.isNotEmpty()) {
            val groupId = requireArguments().getString("groupId")
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserId != null && groupId != null) {
                val db = Firebase.firestore
                val messagesRef =
                    db.collection("GroupMessages").document(groupId).collection("Messages")
                val newMessage = Message(
                    senderId = currentUserId,
                    text = messageText,
                    timestamp = System.currentTimeMillis()
                )
                messagesRef.add(newMessage).addOnSuccessListener {
                    messageEditText.text.clear()
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.itemCount)
                }.addOnFailureListener { exception ->
                    Log.d(TAG, "Error sending message: ", exception)
                }
            } else {
                Toast.makeText(activity, "無法發送消息，請確保已加入群組。", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, "請輸入消息。", Toast.LENGTH_SHORT).show()
        }
    }
}