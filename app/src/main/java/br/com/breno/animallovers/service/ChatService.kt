package br.com.breno.animallovers.service

import android.util.Log
import br.com.breno.animallovers.model.ChatMessage
import com.google.firebase.database.FirebaseDatabase

class ChatService {

    fun updateOrSetLatestMessage(fromId : String, toId : String, message: ChatMessage) {

        if(fromId.isNullOrEmpty() || toId.isNullOrEmpty()) {
            Log.w("ChatService", "Algum id veio nulo: quem envia a mensagem: $fromId; quem recebe a mensagem: $toId")
            return
        }
        val latestMessageRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(message)

        val latestMessageToRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(message)
    }

    fun updateOrSetLatestFromMessage(fromId : String, toId : String, message: ChatMessage) {
        if(fromId.isNullOrEmpty() || toId.isNullOrEmpty()) {
            Log.w("ChatService", "Algum id veio nulo: quem envia a mensagem: $fromId; quem recebe a mensagem: $toId")
            return
        }

        val latestMessageRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(message)
    }

    fun updateOrSetLatestToMessage(fromId : String, toId : String, message: ChatMessage) {
        val latestMessageToRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(message)
    }
}