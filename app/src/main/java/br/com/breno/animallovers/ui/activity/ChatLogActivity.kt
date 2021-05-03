package br.com.breno.animallovers.ui.activity

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.R
import br.com.breno.animallovers.adapters.ChatFromItem
import br.com.breno.animallovers.adapters.ChatToItem
import br.com.breno.animallovers.adapters.DateMessageItem
import br.com.breno.animallovers.model.ChatMessage
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Login
import br.com.breno.animallovers.model.User
import br.com.breno.animallovers.service.ChatService
import br.com.breno.animallovers.service.NotificationService
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import br.com.breno.animallovers.ui.fragment.ChatFragment
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.action_bar_chat_log.*
import kotlinx.android.synthetic.main.action_bar_chat_log.view.*
import kotlinx.android.synthetic.main.activity_chat_log.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class ChatLogActivity : AppCompatActivity() {

    private var database: DatabaseReference = Firebase.database.reference
    private var base: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var storage: FirebaseStorage

    private var chatService = ChatService()

    companion object {
        const val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recycler_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.usuario

        this.supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.setCustomView(R.layout.action_bar_chat_log)
        supportActionBar!!.elevation = 0f
        val view: View = supportActionBar!!.customView

        if (toUser?.pathFotoPerfil != "") {
            storage = FirebaseStorage.getInstance()
            var storageRef = storage.reference
                .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_OWNER_PHOTOS.nome)
                .child(toUser?.id.toString())
                .child(toUser?.id.toString() + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                view.iv_photo_owner_chat_log.setImageBitmap(bmp)
            }.addOnFailureListener {

            }
        }

        base.child(AnimalLoversConstants.DATABASE_ENTITY_CONTROL_LOGIN.nome)
            .addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                @SuppressLint("UseCompatLoadingForDrawables")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.hasChild(toUser?.id.toString())) {
                        return
                    }
                    val ownerLogin = snapshot.child(toUser?.id.toString()).getValue<Login>()!!

                    if (ownerLogin.logged) {
                        view.tv_status_owner_chat_log.background =
                            applicationContext.getDrawable(R.drawable.logged_owner_icon)
                        view.tv_detailed_status_owner.text =
                            applicationContext.getText(R.string.online)
                    } else {
                        val dt = Instant.ofEpochSecond(ownerLogin.lastLogin)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                        var dateUtils = DateUtils()
                        view.tv_status_owner_chat_log.background =
                            applicationContext.getDrawable(R.drawable.likes_post_count)
                        view.tv_detailed_status_owner.text =
                            "Último acesso há " + dateUtils.dateDiffInTextFormat(dt)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }

            })

        view.tv_username_chat_log.text = toUser?.usuario
        listenForMessages()

        iv_back_chat_log.setOnClickListener {
            super.onBackPressed()
        }

        btn_send_message_log.setOnClickListener {
            perfomeSendMessage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.id
        val reference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$fromId/$toId")

        var timeStampMessages = ""

        reference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                var rawDateTimeMessage = Instant.ofEpochSecond(chatMessage!!.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
                val currentMessageDate = DateUtils.formatDateToLocalFormat(rawDateTimeMessage.toString())
                if(timeStampMessages == "" || timeStampMessages != currentMessageDate) {
                    timeStampMessages = currentMessageDate

                    adapter.add(DateMessageItem(timeStampMessages))
                }

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = ChatFragment.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, rawDateTimeMessage.toString(), currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, rawDateTimeMessage.toString(), toUser!!))
                    }
                    //Atualiza as últimas mensagens como lidas
                    chatMessage.isRead = true

                    chatService.updateOrSetLatestFromMessage(fromId.toString(), toId.toString(), chatMessage)
                }

                recycler_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                var rawDateTimeMessage = Instant.ofEpochSecond(chatMessage!!.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
                val currentMessageDate = DateUtils.formatDateToLocalFormat(rawDateTimeMessage.toString())
                if(timeStampMessages == "" || timeStampMessages != currentMessageDate) {
                    timeStampMessages = currentMessageDate
                    adapter.add(DateMessageItem(timeStampMessages))
                }

                recycler_chat_log.scrollToPosition(adapter.itemCount - 1)

                if (chatMessage != null) {

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = ChatFragment.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, rawDateTimeMessage.toString(), currentUser))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, rawDateTimeMessage.toString(), toUser!!))
                    }

                    //Atualiza as últimas mensagens como lidas
                    chatMessage.isRead = true

                    chatService.updateOrSetLatestFromMessage(fromId.toString(), toId.toString(), chatMessage)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    //Verificar a performance
    private fun perfomeSendMessage() {
        val text = et_chat_log.text.toString()

        if (text.isEmpty()) {
            mostraToast("Digite uma mensagem para essa conversa")
            return
        }

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user!!.id

        if (fromId == null) return

        val reference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$toId/$fromId").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000, false)

        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")
                et_chat_log.text.clear()
                recycler_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)

        //Atualiza as últimas mensagens como lidas
        chatMessage.isRead = false
        chatService.updateOrSetLatestMessage(fromId, toId, chatMessage)

        sendNotificationOfNewMessage(fromId, toId, chatMessage)
    }

    private fun sendNotificationOfNewMessage(fromId: String, toId: String, message: ChatMessage) {
        var notificationService = NotificationService(applicationContext)

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val fromUser =
                            snapshot.child(fromId)
                                .child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome)
                                .getValue<Conta>()!!
                        val toUser = snapshot.child(toId)
                                .child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome)
                                .getValue<Conta>()!!

                        notificationService.sendNotificationOfNewChatMessage(fromUser, toUser, message, (System.currentTimeMillis() / 1000).toString())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.toString())
                    }
                })
    }
}
