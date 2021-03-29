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
    }

    override fun getLayout(): Int {
        return R.layout.item_chat_from_row
    }
}

class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_to_row.text = text
    }

    override fun getLayout(): Int {
        return R.layout.item_chat_to_row
    }
}