package br.com.breno.animallovers.ui.fragment

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import br.com.breno.animallovers.R
import br.com.breno.animallovers.adapters.LatestMessageRow
import br.com.breno.animallovers.model.ChatMessage
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.User
import br.com.breno.animallovers.service.DonoService
import br.com.breno.animallovers.service.PetService
import br.com.breno.animallovers.ui.activity.ChatLogActivity
import br.com.breno.animallovers.ui.activity.NewMessageActivity
import br.com.breno.animallovers.ui.activity.NewMessageActivity.Companion.USER_KEY
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_notificacao.*
import kotlinx.android.synthetic.main.item_chat.*
import kotlinx.android.synthetic.main.item_latest_message.*
import kotlinx.android.synthetic.main.item_latest_message.view.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.nav_header.img_profile_nav_main

class ChatFragment:Fragment() {

    companion object {
        var currentUser: User? = null
    }

    val adapter = GroupAdapter<ViewHolder>()
    val latestMessagesMap = HashMap<String, ChatMessage>()
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab_new_chat.setOnClickListener {
            startActivity(Intent(requireContext(), NewMessageActivity::class.java))
        }

        auth = FirebaseAuth.getInstance()

        recycler_chat_last.adapter = adapter
        recycler_chat_last
            .addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        clickGoToUser()
        listenForLatestMessages()
    }

    private fun clickGoToUser() {
        adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(context, ChatLogActivity::class.java)
            val row = item as LatestMessageRow

            intent.putExtra(USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

}