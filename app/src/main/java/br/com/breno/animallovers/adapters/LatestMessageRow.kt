package br.com.breno.animallovers.adapters

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.ChatMessage
import br.com.breno.animallovers.model.Login
import br.com.breno.animallovers.model.User
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_latest_message.view.*

class LatestMessageRow(val chatMessage: ChatMessage) : Item<ViewHolder>() {

    private lateinit var storage: FirebaseStorage
    var chatPartnerUser: User? = null
    private var base: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun getLayout(): Int {
        return R.layout.item_latest_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_message_latest.text = chatMessage.text

        if(!chatMessage.isRead) {
            viewHolder.itemView.cons_lay_item_latest_message.setBackgroundColor(viewHolder.itemView.context.getColor(R.color.backgroundNotificationItemUnread))
        }
        else {
            viewHolder.itemView.cons_lay_item_latest_message.setBackgroundColor(viewHolder.itemView.context.getColor(R.color.backgroundNotificationItem))
        }

        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
            viewHolder.itemView.tv_message_latest_who_sent.text = viewHolder.itemView.context.getString(R.string.txt_you_sent_last_message)
        } else {
            chatPartnerId = chatMessage.fromId
            viewHolder.itemView.tv_message_latest_who_sent.text = viewHolder.itemView.context.getString(R.string.empty)

        }

        base.child(AnimalLoversConstants.DATABASE_ENTITY_CONTROL_LOGIN.nome)
            .addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                @SuppressLint("UseCompatLoadingForDrawables")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(chatPartnerId)) {
                        val ownerLogin = snapshot.child(chatPartnerId).getValue<Login>()!!

                        if (ownerLogin.logged) {
                            viewHolder.itemView.tv_status_pet_chat_list.background =
                                viewHolder.itemView.context.getDrawable(R.drawable.logged_owner_icon)
                        } else {
                            viewHolder.itemView.tv_status_pet_chat_list.background =
                                viewHolder.itemView.context.getDrawable(R.drawable.likes_post_count)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }

            })

        val ref = FirebaseDatabase.getInstance().getReference("/conta/$chatPartnerId/dono")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                chatPartnerUser = snapshot.getValue(User::class.java)

                viewHolder.itemView.tv_username_latest.text = chatPartnerUser?.usuario

                if (chatPartnerUser?.pathFotoPerfil != "") {
                    storage = FirebaseStorage.getInstance()
                    var storageRef = storage.reference
                        .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                        .child(AnimalLoversConstants.STORAGE_ROOT_OWNER_PHOTOS.nome)
                        .child(chatPartnerUser?.id.toString())
                        .child(chatPartnerUser?.id.toString() +
                                AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

                    storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                        val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                        viewHolder.itemView.iv_latest_message.setImageBitmap(bmp)
                    }.addOnFailureListener {

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


}