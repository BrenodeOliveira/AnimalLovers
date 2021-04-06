package br.com.breno.animallovers.adapters

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.ChatMessage
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Login
import br.com.breno.animallovers.model.User
import br.com.breno.animallovers.service.DonoService
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_latest_message.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var accountInfo = Conta()
    private var donoService = DonoService()
    var chatPartnerUser: User? = null
    private var base: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun getLayout(): Int {
        return R.layout.item_latest_message
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {


        viewHolder.itemView.tv_message_latest.text = chatMessage.text

        val chatPartnerId: String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.toId
        } else {
            chatPartnerId = chatMessage.fromId
        }

        base.child(AnimalLoversConstants.DATABASE_ENTITY_CONTROL_LOGIN.nome).addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChild(chatMessage.fromId)) {
                    val ownerLogin = snapshot.child(chatMessage.fromId).getValue<Login>()!!

                    if(ownerLogin.logged) {
                        viewHolder.itemView.tv_status_pet_chat_list.background = viewHolder.itemView.context.getDrawable(R.drawable.logged_owner_icon)
                    }
                    else {
                        viewHolder.itemView.tv_status_pet_chat_list.background = viewHolder.itemView.context.getDrawable(R.drawable.likes_post_count)
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

                val targetImageView = viewHolder.itemView.iv_latest_message

                //Adicionar a parte de storage pq sem isso não vai

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


}