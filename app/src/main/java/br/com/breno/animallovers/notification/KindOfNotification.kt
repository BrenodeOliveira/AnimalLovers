package br.com.breno.animallovers.notification

enum class KindOfNotification (val nome: String) {

    LIKED_POST("likedPost"),
    LIKED_COMMENT("likedComment"),
    COMMENTED_POST("commentedPost"),

    FRIENDSHIP_REQUEST_RECEIVED("friendShipRequestReceived"),
    FRIENDSHIP_REQUEST_ACCEPTED("friendShipRequestAccepted"),
    FRIENDSHIP_REQUEST_SENT("friendShipRequestSent"),

    NEW_CHAT_MESSAGE_RECEIVED("newChatMessageReceived")
}