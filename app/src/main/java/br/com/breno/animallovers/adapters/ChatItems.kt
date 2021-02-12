package br.com.breno.animallovers.adapters

import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.User
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_chat_from_row.view.*
import kotlinx.android.synthetic.main.item_chat_to_row.view.*


class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_from_row.text = text

        val uri = user.pathFotoPerfil
        val targerImageView = viewHolder.itemView.iv_person_from_row
//        Picasso.get().load(uri).into(targerImageView)
    }

    override fun getLayout(): Int {
        return R.layout.item_chat_from_row
    }
}

class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_to_row.text = text

        //load our user image inte the animallovers logo
        val uri = user.pathFotoPerfil
        val targerImageView = viewHolder.itemView.iv_person_to_row
//        Picasso.get().load(uri).into(targerImageView)
    }

    override fun getLayout(): Int {
        return R.layout.item_chat_to_row
    }
}