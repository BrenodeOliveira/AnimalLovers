package br.com.breno.animallovers.model


class ChatMessage(
    val id: String, val text: String, val fromId: String, val toId: String,
    val timestamp: Long, var isRead: Boolean = true) {
    constructor() : this("","","","",-1, true)
}