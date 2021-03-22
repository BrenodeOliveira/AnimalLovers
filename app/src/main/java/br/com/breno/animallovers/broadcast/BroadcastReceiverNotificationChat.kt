package br.com.breno.animallovers.broadcast

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.com.breno.animallovers.model.ChatMessage
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.service.NotificationService
import com.google.firebase.database.FirebaseDatabase

class BroadcastReceiverNotificationChat : BroadcastReceiver() {
    val KEY_TEXT_REPLY = "key_text_reply"

    override fun onReceive(context: Context?, intent: Intent?) {
        val toUser = intent?.getSerializableExtra("OWNER_INFO_PROFILE") as Conta
        val fromUser = intent.getSerializableExtra("OWNER_LOGGED_INFO_PROFILE") as Conta

        sendNewMessage(fromUser, toUser, intent, context!!)
    }

    private fun sendNewMessage(toUser : Conta, fromUser : Conta, intent: Intent, context : Context) {
        val reference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/${fromUser.id}/${toUser.id}").push()

        val toReference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/${toUser.id}/${fromUser.id}").push()

        val chatMessage = ChatMessage(
            reference.key!!, getMessageText(intent).toString(), fromUser.id, toUser.id,
            System.currentTimeMillis() / 1000
        )

        reference.setValue(chatMessage)
        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/${fromUser.id}/${toUser.id}")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/${toUser.id}/${fromUser.id}")
        latestMessageToRef.setValue(chatMessage)

        sendNotificationOfNewMessage(toUser, fromUser, chatMessage, context)
    }

    private fun sendNotificationOfNewMessage(toUser : Conta, fromUser : Conta, message : ChatMessage, context : Context) {
        var notificationService = NotificationService(context)

        notificationService.sendNotificationOfNewChatMessage(fromUser, toUser, message, (System.currentTimeMillis() / 1000).toString())

    }

    private fun getMessageText(intent: Intent): CharSequence? {
        return RemoteInput.getResultsFromIntent(intent)?.getCharSequence(KEY_TEXT_REPLY)
    }
}