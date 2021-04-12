package br.com.breno.animallovers.service

import br.com.breno.animallovers.model.ChatMessage
import com.google.firebase.database.FirebaseDatabase

class ChatService {

    fun updateOrSetLatestMessage(fromId : String, toId : String, message: ChatMessage) {
        val latestMessageRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(message)

        val latestMessageToRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(message)
    }

    fun updateOrSetLatestFromMessage(fromId : String, toId : String, message: ChatMessage) {
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