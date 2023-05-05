package com.example.tmpdevelop_d.fragment

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tmpdevelop_d.adapter.GroupAdapter
import com.example.tmpdevelop_d.dialog.CreateGroupDialog
import com.example.tmpdevelop_d.R
import com.example.tmpdevelop_d.users.Group
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class FirstFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val groups = arrayListOf<Group>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 設定佈局
        val view = inflater.inflate(R.layout.fragment_first, container, false)

        // 綁定 RecyclerView
        recyclerView = view.findViewById(R.id.group_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = GroupAdapter(groups)
        recyclerView.adapter = adapter

        // 綁定 FloatingActionButton
        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            // 點擊 FloatingActionButton 後顯示創建群組的 Dialog
            showCreateGroupDialog()
        }

        // 綁定 SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        // 設定 SwipeRefreshLayout 的刷新監聽器
        swipeRefreshLayout.setOnRefreshListener {
            fetchGroupListAndUpdateRecyclerView {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // 獲取群組列表並更新 RecyclerView
        fetchGroupListAndUpdateRecyclerView()

        return view
    }

    // 從 Firestore 中獲取群組列表並更新 RecyclerView
    private fun fetchGroupListAndUpdateRecyclerView(onComplete: (() -> Unit)? = null) {
        val db = Firebase.firestore
        val groupsRef = db.collection("Groups")
        // 獲取當前用戶 ID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        groupsRef.get().addOnSuccessListener { result ->
            groups.clear()
            for (document in result) {
                val group = document.toObject(Group::class.java)
                // 如果當前用戶是群組成員，則添加到群組列表中
                if (group.memberIds.contains(currentUserId)) {
                    groups.add(group)
                }
            }
            // 通知 Adapter 數據已更改
            adapter.notifyDataSetChanged()
            onComplete?.invoke()
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error getting documents: ", exception)
            onComplete?.invoke()
        }
    }

    // 顯示創建群組的 Dialog
    private fun showCreateGroupDialog() {
        val createGroupDialog = CreateGroupDialog()
        createGroupDialog.show(childFragmentManager, "createGroupDialog")
    }
}