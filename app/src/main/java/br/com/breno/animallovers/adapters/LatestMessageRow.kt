package br.com.breno.animallovers.adapters

import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.ChatMessage
import br.com.breno.animallovers.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_latest_message.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>() {
    var chatPartnerUser: User? = null

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

        val ref = FirebaseDatabase.getInstance().getReference("/conta/$chatPartnerId/dono")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)

                viewHolder.itemView.tv_username_latest.text = chatPartnerUser?.usuario

                val targetImageView = viewHolder.itemView.iv_latest_message
                //modificar todos os picassos quando os usuarios todos tiverem foto.
//                    Picasso.get().load(chatPartnerUser?.pathFotoPerfil).into(targetImageView)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

}