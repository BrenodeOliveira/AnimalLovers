package br.com.breno.animallovers.service

import android.content.Context
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.*
import br.com.breno.animallovers.notification.Api
import br.com.breno.animallovers.notification.KindOfNotification
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class NotificationService(context : Context) {
    private var donoService = DonoService()
    private var petService = PetService(context)
    val myPreferences = ProjectPreferences(context)
    var ctx = context
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun sendNotificationOfCommentInPost(post : Post, comentario : Comentario, snapshot: DataSnapshot) {
        val owner = donoService.retrieveOwnerInfo(snapshot, post.idOwner)

        val petWhoCommented = petService.retrievePetInfo(myPreferences.getPetLogged().toString(), snapshot.child(auth.uid.toString()))
        val petWhoReceivedComment = petService.retrievePetInfo(post.idPet, snapshot.child(post.idOwner))

        val bodyText = "[" + petWhoReceivedComment.nome + "]: " + petWhoCommented.nome + " comentou na sua publicação"
        val title = petWhoCommented.nome + " " + ctx.getString(R.string.commented_your_post)

        sendNotificationsRelatedToPosts(post, petWhoCommented, owner.deviceToken, title, bodyText, KindOfNotification.COMMENTED_POST.nome, "1")
    }

    fun sendNotificationOfLikedComment(post : Post, comentario : Comentario, snapshot: DataSnapshot) {
        val owner = donoService.retrieveOwnerInfo(snapshot, comentario.idOwner)

        val petWhoCommented = petService.retrievePetInfo(myPreferences.getPetLogged().toString(), snapshot.child(auth.uid.toString()))
        val petWhoReceivedComment = petService.retrievePetInfo(comentario.idPet, snapshot.child(comentario.idOwner))

        val bodyText = "[" + petWhoReceivedComment.nome + "]: " + petWhoCommented.nome + " curtiu um comentário seu deixado em uma publicação"
        val title = petWhoCommented.nome + " " + ctx.getString(R.string.liked_your_comment)

        sendNotificationsRelatedToPosts(post, petWhoCommented, owner.deviceToken, title, bodyText, KindOfNotification.LIKED_COMMENT.nome, "3")
    }

    fun sendNotificationOfLikedPost(petWhoReceivedLike : Pet, petWhoLiked : Pet, post : Post, conta : Conta) {

        val bodyText = "[" + petWhoReceivedLike.nome + "]: " + petWhoLiked.nome + " curtiu a sua publicação "
        val title = petWhoLiked.nome + " " + ctx.getString(R.string.liked_your_post)
        sendNotificationsRelatedToPosts(post, petWhoLiked, conta.deviceToken, title, bodyText, KindOfNotification.LIKED_POST.nome, "2")
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