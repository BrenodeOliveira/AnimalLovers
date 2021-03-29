package br.com.breno.animallovers.ui.activity

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.R
import br.com.breno.animallovers.adapters.ChatFromItem
import br.com.breno.animallovers.adapters.ChatToItem
import br.com.breno.animallovers.model.ChatMessage
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.User
import br.com.breno.animallovers.service.NotificationService
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import br.com.breno.animallovers.ui.fragment.ChatFragment
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    private var database: DatabaseReference = Firebase.database.reference

    companion object {
        const val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recycler_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.usuario

        listenForMessages()


        if (et_chat_log.isPressed) {
            recycler_chat_log.scrollToPosition(adapter.itemCount - 1)
            mostraToast("Teste")
        }


        btn_send_message_log.setOnClickListener {
            perfomeSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.id
        val reference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$fromId/$toId")

        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                recycler_chat_log.scrollToPosition(adapter.itemCount - 1)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = ChatFragment.currentUser ?: return
                        adapter.add(
                            ChatFromItem(
                                chatMessage.text,
                                currentUser
                            )
                        )
                    } else {
                        adapter.add(
                            ChatToItem(
                                chatMessage.text,
                                toUser!!
                            )
                        )
                    }

                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                recycler_chat_log.scrollToPosition(adapter.itemCount - 1)

                if (chatMessage != null) {

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = ChatFragment.currentUser ?: return
                        adapter.add(
                            ChatFromItem(
                                chatMessage.text,
                                currentUser
                            )
                        )
                    } else {
                        adapter.add(
                            ChatToItem(
                                chatMessage.text,
                                toUser!!
                            )
                        )
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    private fun perfomeSendMessage() {
        val text = et_chat_log.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user!!.id

        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(
            reference.key!!, text, fromId, toId,
            System.currentTimeMillis() / 1000
        )
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")
                et_chat_log.text.clear()
                recycler_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)

        sendNotificationOfNewMessage(fromId, toId, chatMessage)
    }

    private fun sendNotificationOfNewMessage(fromId : String, toId : String, message : ChatMessage) {
        var notificationService = NotificationService(applicationContext)

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fromUser = snapshot.child(fromId).child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome).getValue<Conta>()!!
                val toUser = snapshot.child(toId).child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome).getValue<Conta>()!!

                notificationService.sendNotificationOfNewChatMessage(fromUser, toUser, message, (System.currentTimeMillis() / 1000).toString())
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.toString())
            }
        })
    }

    private fun keyboardUpdate() {
        //Checkar se o teclado abriu para que o chat atualize
        // recycler_chat_log.scrollToPosition(adapter.itemCount - 1)
    }
}
