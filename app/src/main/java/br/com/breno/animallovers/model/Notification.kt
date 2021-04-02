package br.com.breno.animallovers.model

import java.io.Serializable

class Notification : Serializable {
    var dataHora = ""
    var tipo = ""
    var ativo = true
    var visualizada = false
    var petRemetente = Pet()
    var postNotification = Post()

    var incrementIdNotification = ""
    var uniqueIdNotification = ""
    var idActionNotification = ""
    var notificationType = 0
}