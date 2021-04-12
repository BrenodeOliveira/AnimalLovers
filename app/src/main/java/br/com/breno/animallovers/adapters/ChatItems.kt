package br.com.breno.animallovers.adapters

import android.os.Build
import androidx.annotation.RequiresApi
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.User
import br.com.breno.animallovers.utils.DateUtils
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_chat_from_row.view.*
import kotlinx.android.synthetic.main.item_chat_to_row.view.*
import kotlinx.android.synthetic.main.item_chat_to_row.view.tv_chat_to_row
import kotlinx.android.synthetic.main.item_date_message_chat_row.view.*
import kotlinx.android.synthetic.main.item_latest_message.view.*


class ChatFromItem(val text: String, var dateTimeMessage : String, val user: User) : Item<ViewHolder>() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_from_row.text = text
        viewHolder.itemView.tv_chat_time_message_sent.text = DateUtils.formatTimeToLocalFormat(dateTimeMessage)
    }

    override fun getLayout(): Int {
        return R.layout.item_chat_from_row
    }
}

class ChatToItem(val text: String, var dateTimeMessage : String, val user: User) : Item<ViewHolder>() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_to_row.text = text
        viewHolder.itemView.tv_chat_time_message_received.text = DateUtils.formatTimeToLocalFormat(dateTimeMessage)
    }

    override fun getLayout(): Int {
        return R.layout.item_chat_to_row
    }
}

class DateMessageItem(val text: String) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_date_message_chat_log.text = text
    }

    override fun getLayout(): Int {
        return R.layout.item_date_message_chat_row
    }
}