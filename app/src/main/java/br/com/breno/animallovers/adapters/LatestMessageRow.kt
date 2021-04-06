package br.com.breno.animallovers.adapters

import android.content.Context
import android.graphics.BitmapFactory
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.ChatMessage
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.User
import br.com.breno.animallovers.service.DonoService
import br.com.breno.animallovers.service.PetService
import br.com.breno.animallovers.ui.activity.MainActivity
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_latest_message.view.*
import org.koin.dsl.koinApplication
import kotlin.coroutines.coroutineContext

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var accountInfo = Conta()
    private var donoService = DonoService()
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

                //Adicionar a parte de storage pq sem isso não vai

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


}