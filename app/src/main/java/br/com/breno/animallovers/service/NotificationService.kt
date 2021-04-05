package br.com.breno.animallovers.service

import android.content.Context
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.ChatMessage
import br.com.breno.animallovers.model.Comentario
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.LikeComment
import br.com.breno.animallovers.model.LikePost
import br.com.breno.animallovers.model.Notification
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.model.SolicitacaoAmizade
import br.com.breno.animallovers.notification.Api
import br.com.breno.animallovers.notification.KindOfNotification
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class NotificationService(context: Context) {
    private var donoService = DonoService()
    private var petService = PetService(context)
    val myPreferences = ProjectPreferences(context)
    var ctx = context
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database = Firebase.database.reference

    fun updateNotificationInReceiver(notification: Notification) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .child(notification.incrementIdNotification)
            .setValue(notification)
    }

    private fun persistNotificationOfNewComment(petWhoCommented: Pet, petReceiver: Pet, snapshot: DataSnapshot, comentario: Comentario, post : Post) {
        var notification = Notification()

        var ref = database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(petReceiver.idOwner)
            .child(petReceiver.id)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .push()

        notification.dataHora = DateUtils.dataFormatWithMilliseconds()
        notification.petRemetente = petWhoCommented
        notification.tipo = KindOfNotification.COMMENTED_POST.nome
        notification.uniqueIdNotification = ref.key!!
        notification.incrementIdNotification = getIdOfLastNotification(snapshot, petReceiver).toString()
        notification.idActionNotification = comentario.uniqueIdComment
        notification.notificationType = 0//Serve para saber qual tupo de layout será inflado na NotificationFragment
        notification.postNotification = post

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS_SENT.nome)
            .child(KindOfNotification.COMMENTED_POST.nome)
            .child(comentario.uniqueIdComment)
            .setValue(DateUtils.dataFormatWithMilliseconds())

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(petReceiver.idOwner)
            .child(petReceiver.id)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .child(notification.incrementIdNotification)
            .setValue(notification)
    }

    fun persistNotificationOfNewFriendshipRequest(petWhoAccepted: Pet, petReceiver: Pet, snapshot: DataSnapshot, solicitacao: SolicitacaoAmizade) {
        var notification = Notification()

        var ref = database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(petReceiver.idOwner)
            .child(petReceiver.id)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .push()

        notification.dataHora = DateUtils.dataFormatWithMilliseconds()
        notification.petRemetente = petWhoAccepted
        notification.tipo = KindOfNotification.FRIENDSHIP_REQUEST_RECEIVED.nome
        notification.uniqueIdNotification = ref.key!!
        notification.incrementIdNotification = getIdOfLastNotification(snapshot, petReceiver).toString()
        notification.idActionNotification = solicitacao.uniqueId
        notification.notificationType = 1//Serve para saber qual tipo de layout será inflado na NotificationFragment

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS_SENT.nome)
            .child(KindOfNotification.FRIENDSHIP_REQUEST_SENT.nome)
            .child(solicitacao.uniqueId)
            .setValue(DateUtils.dataFormatWithMilliseconds())

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(petReceiver.idOwner)
            .child(petReceiver.id)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .child(notification.incrementIdNotification)
            .setValue(notification)
    }

    fun persistNotificationOfNewFriendshipAccepted(petWhoAccepted: Pet, petReceiver: Pet, snapshot: DataSnapshot, solicitacao: SolicitacaoAmizade) {
        var notification = Notification()

        var ref = database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(petReceiver.idOwner)
            .child(petReceiver.id)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .push()

        notification.dataHora = DateUtils.dataFormatWithMilliseconds()
        notification.petRemetente = petWhoAccepted
        notification.tipo = KindOfNotification.FRIENDSHIP_REQUEST_ACCEPTED.nome
        notification.uniqueIdNotification = ref.key!!
        notification.incrementIdNotification = getIdOfLastNotification(snapshot, petReceiver).toString()
        notification.idActionNotification = solicitacao.uniqueId
        notification.notificationType = 1//Serve para saber qual tupo de layout será inflado na NotificationFragment

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS_SENT.nome)
            .child(KindOfNotification.FRIENDSHIP_REQUEST_ACCEPTED.nome)
            .child(solicitacao.uniqueId)
            .setValue(DateUtils.dataFormatWithMilliseconds())

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(petReceiver.idOwner)
            .child(petReceiver.id)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .child(notification.incrementIdNotification)
            .setValue(notification)
    }

    private fun persistNotificationOfNewLikedComment(notification: Notification, petReceiver: Pet, petWhoCommented : Pet, snapshot: DataSnapshot, comentario: Comentario, likeComment: LikeComment, post : Post) : Notification{

        //Saber se o usuário já curtiu o comentário alguma vez
        val sSnap = snapshot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString())
        if(sSnap.hasChild(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS_SENT.nome)) {
            if(sSnap.child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS_SENT.nome).hasChild(KindOfNotification.LIKED_COMMENT.nome)) {
                if(sSnap.child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS_SENT.nome).child(KindOfNotification.LIKED_COMMENT.nome).hasChild(likeComment.uniqueId)) {

                    notification.ativo = true
                    database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                        .child(petReceiver.idOwner)
                        .child(petReceiver.id)
                        .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
                        .child(notification.incrementIdNotification)
                        .setValue(notification)
                    return notification
                }
            }
        }
        var ref = database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(petReceiver.idOwner)
            .child(petReceiver.id)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .push()


        notification.dataHora = DateUtils.dataFormatWithMilliseconds()
        notification.petRemetente = petWhoCommented
        notification.tipo = KindOfNotification.LIKED_COMMENT.nome
        notification.uniqueIdNotification = ref.key!!
        notification.incrementIdNotification = getIdOfLastNotification(snapshot, petReceiver).toString()
        notification.idActionNotification = likeComment.uniqueId
        notification.notificationType = 0//Serve para saber qual tupo de layout será inflado na NotificationFragment
        notification.postNotification = post

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS_SENT.nome)
            .child(KindOfNotification.LIKED_COMMENT.nome)
            .child(likeComment.uniqueId)
            .setValue(DateUtils.dataFormatWithMilliseconds())

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(petReceiver.idOwner)
            .child(petReceiver.id)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .child(notification.incrementIdNotification)
            .setValue(notification)
        return notification
    }

    private fun persistNotificationOfNewLikeInPost(notification: Notification, petReceiver: Pet, petWhoCommented : Pet, snapshot: DataSnapshot, likePost: LikePost) : Notification{

        //Saber se o usuário já curtiu o comentário alguma vez
        val sSnap = snapshot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString())
        if(sSnap.hasChild(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS_SENT.nome)) {
            if(sSnap.child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS_SENT.nome).hasChild(KindOfNotification.LIKED_POST.nome)) {
                if(sSnap.child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS_SENT.nome).child(KindOfNotification.LIKED_POST.nome).hasChild(likePost.uniqueId)) {

                    notification.ativo = true
                    database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                        .child(petReceiver.idOwner)
                        .child(petReceiver.id)
                        .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
                        .child(notification.incrementIdNotification)
                        .setValue(notification)
                    return notification
                }
            }
        }
        var ref = database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(petReceiver.idOwner)
            .child(petReceiver.id)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .push()


        notification.dataHora = DateUtils.dataFormatWithMilliseconds()
        notification.petRemetente = petWhoCommented
        notification.tipo = KindOfNotification.LIKED_POST.nome
        notification.uniqueIdNotification = ref.key!!
        notification.incrementIdNotification = getIdOfLastNotification(snapshot, petReceiver).toString()
        notification.idActionNotification = likePost.uniqueId
        notification.notificationType = 0//Serve para saber qual tupo de layout será inflado na NotificationFragment

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS_SENT.nome)
            .child(KindOfNotification.LIKED_POST.nome)
            .child(likePost.uniqueId)
            .setValue(DateUtils.dataFormatWithMilliseconds())

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(petReceiver.idOwner)
            .child(petReceiver.id)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .child(notification.incrementIdNotification)
            .setValue(notification)
        return notification
    }

    private fun getIdOfLastNotification(snapshot: DataSnapshot, pet: Pet) : Int {
        return if(snapshot.child(pet.idOwner).child(pet.id).hasChild(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)) {
            snapshot.child(pet.idOwner)
                .child(pet.id)
                .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
                .childrenCount.toInt()
        } else {
            0
        }
    }

    fun getNotificationModelOfLikeInComment(dataSnapshot: DataSnapshot, comentario: Comentario, likeComment: LikeComment) : Notification {
        var notification: Notification

        if(likeComment.uniqueId.isEmpty()) {
            return Notification()
        }

        val snapshot = dataSnapshot.child(comentario.idOwner).child(comentario.idPet)

        if(snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)) {
            val numNotifications = snapshot.child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome).childrenCount

            for (i in 0 until numNotifications) {
                notification = snapshot.child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome).child(i.toString()).getValue<Notification>()!!

                if(notification.idActionNotification == likeComment.uniqueId) {
                    return notification
                }
            }
        }

        return Notification()
    }

    fun getNotificationModelOfLikeInPost(dataSnapshot: DataSnapshot, post : Post, likePost : LikePost) : Notification {
        var notification: Notification

        if(likePost.uniqueId.isEmpty()) {
            return Notification()
        }

        val snapshot = dataSnapshot.child(post.idOwner).child(post.idPet)

        if(snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)) {
            val numNotifications = snapshot.child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome).childrenCount

            for (i in 0 until numNotifications) {
                notification = snapshot.child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome).child(i.toString()).getValue<Notification>()!!

                if(notification.idActionNotification == likePost.uniqueId) {
                    return notification
                }
            }
        }

        return Notification()
    }

    fun sendNotificationOfCommentInPost(post: Post, comentario: Comentario, snapshot: DataSnapshot) {
        val owner = donoService.retrieveOwnerInfo(snapshot, post.idOwner)

        val petWhoCommented = petService.retrievePetInfo(myPreferences.getPetLogged().toString(), snapshot.child(auth.uid.toString()))
        val petWhoReceivedComment = petService.retrievePetInfo(post.idPet, snapshot.child(post.idOwner))

        val bodyText = "[" + petWhoReceivedComment.nome + "]: " + petWhoCommented.nome + " comentou na sua publicação"
        val title = petWhoCommented.nome + " " + ctx.getString(R.string.commented_your_post)

        persistNotificationOfNewComment(petWhoCommented, petWhoReceivedComment, snapshot, comentario, post)
        sendNotificationsRelatedToPosts(post, petWhoCommented, owner.deviceToken, title, bodyText, KindOfNotification.COMMENTED_POST.nome, "1")
    }

    fun sendNotificationOfLikedComment(post: Post, comentario: Comentario, snapshot: DataSnapshot, likeComment: LikeComment, notification: Notification) {
        val owner = donoService.retrieveOwnerInfo(snapshot, comentario.idOwner)

        val petWhoCommented = petService.retrievePetInfo(myPreferences.getPetLogged().toString(), snapshot.child(auth.uid.toString()))
        val petWhoReceivedComment = petService.retrievePetInfo(comentario.idPet, snapshot.child(comentario.idOwner))


        val sentNotification = persistNotificationOfNewLikedComment(notification, petWhoReceivedComment, petWhoCommented, snapshot, comentario, likeComment, post)

        val bodyText = "[" + petWhoReceivedComment.nome + "]: " + petWhoCommented.nome + " curtiu um comentário seu deixado em uma publicação"
        val title = petWhoCommented.nome + " " + ctx.getString(R.string.liked_your_comment)

        sendNotificationsRelatedToPosts(post, petWhoCommented, owner.deviceToken, title, bodyText, sentNotification.tipo, "3")
    }

    fun sendNotificationOfLikedPost(petWhoReceivedLike : Pet, petWhoLiked : Pet, post : Post, conta : Conta, notification: Notification, likePost: LikePost, snapshot: DataSnapshot) {

        val sentNotification = persistNotificationOfNewLikeInPost(notification, petWhoReceivedLike, petWhoLiked, snapshot, likePost)

        val bodyText = "[" + petWhoReceivedLike.nome + "]: " + petWhoLiked.nome + " curtiu a sua publicação "
        val title = petWhoLiked.nome + " " + ctx.getString(R.string.liked_your_post)
        sendNotificationsRelatedToPosts(post, petWhoLiked, conta.deviceToken, title, bodyText, sentNotification.tipo, "2")
    }

    private fun sendNotificationsRelatedToPosts(post : Post, pet : Pet, deviceToken : String, titleNotification : String, bodyNotification : String, kindOfNotification : String, idKindOfNotification : String) {

        val retrofit = Retrofit.Builder()
            .baseUrl(AnimalLoversConstants.API_ROOT_URL.nome)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api: Api = retrofit.create(Api::class.java)

        val title = titleNotification
        val body = bodyNotification

        val jsonPost = Gson().toJson(post)
        val jsonPet = Gson().toJson(pet)

        val call: Call<ResponseBody> = api.sendNotificationPosts(deviceToken, title, body, jsonPost, jsonPet, kindOfNotification, idKindOfNotification)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    println(response.body()!!.string())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println(call.toString())
                println(t.stackTrace)
            }
        })
    }

    fun sendNotificationOfFriendshipAccepted(pet : Pet, petLogged : Pet, owner : Conta) {
        val bodyText = "[" + pet.nome + "]: " + petLogged.nome + " aceitou sua solicitação de amizade "
        val title = ctx.getString(R.string.friendship_request_accepted)

        sendNotificationsRelatedToFriendship(pet, petLogged, owner.deviceToken, title, bodyText, KindOfNotification.FRIENDSHIP_REQUEST_ACCEPTED.nome, "4")
    }

    fun sendNotificationOfFriendshiRequestReceived(pet : Pet, petLogged : Pet, owner : Conta) {
        val bodyText = "[" + pet.nome + "]: " + petLogged.nome + " enviou uma solicitação de amizade"
        val title = ctx.getString(R.string.friendship_request_received)

        sendNotificationsRelatedToFriendship(pet, petLogged, owner.deviceToken, title, bodyText, KindOfNotification.FRIENDSHIP_REQUEST_RECEIVED.nome, "5")
    }

    private fun sendNotificationsRelatedToFriendship(pet : Pet, petLogged : Pet, deviceToken : String, titleNotification : String, bodyNotification : String, kindOfNotification : String, idKindOfNotification : String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(AnimalLoversConstants.API_ROOT_URL.nome)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api: Api = retrofit.create(Api::class.java)

        val title = titleNotification
        val body = bodyNotification

        val jsonPet = Gson().toJson(pet)
        val jsonPetLogged = Gson().toJson(petLogged)

        val call: Call<ResponseBody> = api.sendNotificationFriendship(deviceToken, title, body, jsonPetLogged, jsonPet, kindOfNotification, idKindOfNotification)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    println(response.body()!!.string())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println(call.toString())
                println(t.stackTrace)
            }
        })
    }

    fun sendNotificationOfNewChatMessage(loggedOwner : Conta, owner : Conta, message : ChatMessage, idNotification : String) {
        val bodyText = message.text
        val title = "Nova mensagem: " + loggedOwner.usuario

        sendNotificationRelatedToChat(loggedOwner, owner, owner.deviceToken, title, bodyText, KindOfNotification.NEW_CHAT_MESSAGE_RECEIVED.nome, idNotification)
    }

    private fun sendNotificationRelatedToChat(loggedOwner : Conta, owner : Conta, deviceToken : String, titleNotification : String, bodyNotification : String, kindOfNotification : String, idKindOfNotification : String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(AnimalLoversConstants.API_ROOT_URL.nome)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api: Api = retrofit.create(Api::class.java)

        val title = titleNotification
        val body = bodyNotification

        val jsonOwner = Gson().toJson(owner)
        val jsonOwnerLogged = Gson().toJson(loggedOwner)

        val call: Call<ResponseBody> = api.sendNotificationChat(deviceToken, title, body, jsonOwnerLogged, jsonOwner, kindOfNotification, idKindOfNotification)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    println(response.body()!!.string())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println(call.toString())
                println(t.stackTrace)
            }
        })
    }

}