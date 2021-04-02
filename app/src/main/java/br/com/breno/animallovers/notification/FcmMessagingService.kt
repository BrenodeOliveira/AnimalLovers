package br.com.breno.animallovers.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import br.com.breno.animallovers.R
import br.com.breno.animallovers.broadcast.BroadcastReceiverNotificationChat
import br.com.breno.animallovers.broadcast.BroadcastReceiverNotificationFriendship
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.model.User
import br.com.breno.animallovers.ui.activity.ChatLogActivity
import br.com.breno.animallovers.ui.activity.ProfilePetActivity
import br.com.breno.animallovers.ui.activity.SinglePostActivity
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import org.json.JSONException
import java.util.*


class FcmMessagingService: FirebaseMessagingService() {

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private val description = "AnimalLovers"
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()

    var type = ""
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            type = "json"

            when {
                remoteMessage.data["kindOfNotification"] == KindOfNotification.COMMENTED_POST.nome -> {
                    sendNotificationForPosts(remoteMessage.data)
                }
                remoteMessage.data["kindOfNotification"] == KindOfNotification.LIKED_POST.nome -> {
                    sendNotificationForPosts(remoteMessage.data)
                }
                remoteMessage.data["kindOfNotification"] == KindOfNotification.LIKED_COMMENT.nome -> {
                    sendNotificationForPosts(remoteMessage.data)
                }
                remoteMessage.data["kindOfNotification"] == KindOfNotification.FRIENDSHIP_REQUEST_RECEIVED.nome -> {
                    sendNotificationForFriendship(remoteMessage.data)
                }
                remoteMessage.data["kindOfNotification"] == KindOfNotification.FRIENDSHIP_REQUEST_ACCEPTED.nome -> {
                    sendNotificationForFriendship(remoteMessage.data)
                }
                remoteMessage.data["kindOfNotification"] == KindOfNotification.NEW_CHAT_MESSAGE_RECEIVED.nome -> {
                    sendNotificationForChat(remoteMessage.data)
                }
            }
        }
        if (remoteMessage.notification != null) {
            type = "message"
        }
    }

    private fun sendNotificationForPosts(messageBody: Map<String, String>) {
        var postInfo: Post
        var petInfo : Pet
        var idNotification  = messageBody["idNotification"]
        if (type == "json") {
            try {
                val gson = Gson()
                
                postInfo = gson.fromJson(messageBody["post"], Post::class.java)
                petInfo = gson.fromJson(messageBody["pet"], Pet::class.java)

                var shouldOpenCommentsInIntent = false

                when {
                    messageBody["kindOfNotification"] == KindOfNotification.COMMENTED_POST.nome -> {
                        shouldOpenCommentsInIntent = true
                    }
                    messageBody["kindOfNotification"] == KindOfNotification.LIKED_POST.nome -> {

                    }
                    messageBody["kindOfNotification"] == KindOfNotification.LIKED_COMMENT.nome -> {
                        shouldOpenCommentsInIntent = true
                    }
                }
                notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val intent = Intent(this, SinglePostActivity::class.java)
                intent.putExtra("POST_INFO", postInfo)
                intent.putExtra("CAME_FROM_NOTIFICATION", true)
                intent.putExtra("SHOULD_INFLATE_COMMENT", shouldOpenCommentsInIntent)
                val pendingIntent = PendingIntent.getActivity(this, Random().nextInt(), intent, 0)

                val ref = storage.reference
                    .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                    .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                    .child(petInfo.idOwner)
                    .child(petInfo.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)
                try {
                    ref.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                        val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            notificationChannel = NotificationChannel(
                                channelId,
                                description,
                                NotificationManager.IMPORTANCE_HIGH
                            )
                            notificationChannel.enableLights(false)
                            notificationChannel.lightColor = Color.WHITE
                            notificationChannel.enableVibration(false)
                            notificationManager.createNotificationChannel(notificationChannel)

                            builder = Notification.Builder(this, channelId)
                                .setSmallIcon(R.drawable.ic_coracao)
                                .setLargeIcon(bmp)
                                .setContentIntent(pendingIntent)
                                .setContentTitle(messageBody["title"])
                                .setContentText(messageBody["body"])

                            notificationManager.notify(idNotification!!.toInt(), builder.build())
                        }
                        else {
                            builder = Notification.Builder(this)
                                .setSmallIcon(R.drawable.ic_coracao)
                                .setLargeIcon(bmp)
                                .setContentIntent(pendingIntent)
                                .setContentTitle(messageBody["title"])
                                .setContentText(messageBody["body"])
                            notificationManager.notify(idNotification!!.toInt(), builder.build())

                        }
                    }.addOnFailureListener { itException ->
                        println(itException.toString())
                    }
                } catch (ex: Exception) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationChannel = NotificationChannel(
                            channelId,
                            description,
                            NotificationManager.IMPORTANCE_HIGH
                        )
                        notificationChannel.enableLights(false)
                        notificationChannel.lightColor = Color.WHITE
                        notificationChannel.enableVibration(false)
                        notificationManager.createNotificationChannel(notificationChannel)

                        builder = Notification.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_coracao)
                            .setLargeIcon(
                                BitmapFactory.decodeResource(
                                    resources,
                                    R.drawable.animalovers_logo_simples
                                )
                            )
                            .setContentIntent(pendingIntent)
                            .setContentTitle(messageBody["title"])
                            .setContentText(messageBody["body"])
                        notificationManager.notify(idNotification!!.toInt(), builder.build())

                    }
                    else {
                        builder = Notification.Builder(this)
                            .setSmallIcon(R.drawable.ic_coracao)
                            .setLargeIcon(
                                BitmapFactory.decodeResource(
                                    resources,
                                    R.drawable.animalovers_logo_simples
                                )
                            )
                            .setContentIntent(pendingIntent)
                            .setContentTitle(messageBody["title"])
                            .setContentText(messageBody["body"])
                        notificationManager.notify(idNotification!!.toInt(), builder.build())

                    }
                }
            } catch (e: JSONException) {
                println(e.toString())
            }
        } else if (type == "message") {

        }
    }

    private fun sendNotificationForFriendship(messageBody: Map<String, String>) {
        var petInfo : Pet
        var petLoggedInfo : Pet
        var idNotification  = messageBody["idNotification"]
        if (type == "json") {
            try {
                val gson = Gson()

                petInfo = gson.fromJson(messageBody["pet"], Pet::class.java)
                petLoggedInfo = gson.fromJson(messageBody["petLogged"], Pet::class.java)

                var titleOfNotificationButton = ""
                var kindOfNotification = messageBody["kindOfNotification"]

                notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val intent = Intent(this, ProfilePetActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                intent.putExtra("PET_INFO_PROFILE", petInfo)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val buttonIntent = Intent(this, BroadcastReceiverNotificationFriendship::class.java)
                buttonIntent.apply {
                    action = "Do Pending Task"
                    putExtra("PET_INFO_PROFILE", petInfo)
                    putExtra("PET_LOGGED_INFO_PROFILE", petLoggedInfo)
                    putExtra("KIND_OF_FRIENDSHIP_REQUEST", kindOfNotification)
                }
                val buttonPendingIntent = PendingIntent.getBroadcast(
                    this,
                    Random().nextInt(),
                    buttonIntent,
                    0
                )


                val ref = storage.reference
                    .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                    .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                    .child(petInfo.idOwner)
                    .child(petInfo.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)
                try {
                    ref.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                        val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            notificationChannel = NotificationChannel(
                                channelId,
                                description,
                                NotificationManager.IMPORTANCE_HIGH
                            )
                            notificationChannel.enableLights(false)
                            notificationChannel.lightColor = Color.WHITE
                            notificationChannel.enableVibration(false)
                            notificationManager.createNotificationChannel(notificationChannel)

                            builder = Notification.Builder(this, channelId)
                                .setSmallIcon(R.drawable.ic_coracao)
                                .setLargeIcon(bmp)
                                .setContentIntent(pendingIntent)
                                .setContentTitle(messageBody["title"])
                                .setContentText(messageBody["body"])
                                .addAction(
                                    R.drawable.animalovers_logo_simples,
                                    titleOfNotificationButton,
                                    buttonPendingIntent
                                )
                            notificationManager.notify(idNotification!!.toInt(), builder.build())
                        }
                        else {
                            builder = Notification.Builder(this)
                                .setSmallIcon(R.drawable.ic_coracao)
                                .setLargeIcon(bmp)
                                .setContentIntent(pendingIntent)
                                .setContentTitle(messageBody["title"])
                                .setContentText(messageBody["body"])
                                .addAction(
                                    R.drawable.animalovers_logo_simples,
                                    titleOfNotificationButton,
                                    buttonPendingIntent
                                )
                            notificationManager.notify(idNotification!!.toInt(), builder.build())

                        }
                    }.addOnFailureListener { itException ->
                        println(itException.toString())
                    }
                } catch (ex: Exception) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationChannel = NotificationChannel(
                            channelId,
                            description,
                            NotificationManager.IMPORTANCE_HIGH
                        )
                        notificationChannel.enableLights(false)
                        notificationChannel.lightColor = Color.WHITE
                        notificationChannel.enableVibration(false)
                        notificationManager.createNotificationChannel(notificationChannel)

                        builder = Notification.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_coracao)
                            .setLargeIcon(
                                BitmapFactory.decodeResource(
                                    resources,
                                    R.drawable.animalovers_logo_simples
                                )
                            )
                            .setContentIntent(pendingIntent)
                            .setContentTitle(messageBody["title"])
                            .setContentText(messageBody["body"])
                            .addAction(
                                R.drawable.animalovers_logo_simples,
                                titleOfNotificationButton,
                                buttonPendingIntent
                            )
                        notificationManager.notify(idNotification!!.toInt(), builder.build())

                    }
                    else {
                        builder = Notification.Builder(this)
                            .setSmallIcon(R.drawable.ic_coracao)
                            .setLargeIcon(
                                BitmapFactory.decodeResource(
                                    resources,
                                    R.drawable.animalovers_logo_simples
                                )
                            )
                            .setContentIntent(pendingIntent)
                            .setContentTitle(messageBody["title"])
                            .setContentText(messageBody["body"])
                            .addAction(
                                R.drawable.animalovers_logo_simples,
                                titleOfNotificationButton,
                                buttonPendingIntent
                            )
                        notificationManager.notify(idNotification!!.toInt(), builder.build())

                    }
                }
            } catch (e: JSONException) {
                println(e.toString())
            }
        } else if (type == "message") {

        }
    }

    private fun sendNotificationForChat(messageBody: Map<String, String>) {
        var ownerInfo : Conta
        var ownerLoggedInfo : Conta
        var idNotification  = messageBody["idNotification"]
        if (type == "json") {
            try {
                val gson = Gson()

                ownerInfo = gson.fromJson(messageBody["user"], Conta::class.java)
                ownerLoggedInfo = gson.fromJson(messageBody["userLogged"], Conta::class.java)

                var titleOfNotificationButton = ""

                notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val intent = Intent(this, ChatLogActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                val user = User(ownerInfo.id, ownerInfo.email, ownerInfo.usuario, ownerInfo.cidade, ownerInfo.pais, ownerInfo.pathFotoPerfil)

                intent.putExtra("USER_KEY", user)
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val buttonIntent = Intent(this, BroadcastReceiverNotificationChat::class.java)
                buttonIntent.apply {
                    action = "Do Pending Task"
                    putExtra("OWNER_INFO_PROFILE", ownerInfo)
                    putExtra("OWNER_LOGGED_INFO_PROFILE", ownerLoggedInfo)
                }

                val KEY_TEXT_REPLY = "key_text_reply"
                var replyLabel: String = ("Responder")

                var remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
                    setLabel(replyLabel)
                    build()
                }

                val buttonPendingIntent = PendingIntent.getBroadcast(this, Random().nextInt(), buttonIntent, 0)

                var action: Notification.Action = Notification.Action.Builder(R.drawable.ic_baseline_send_24, ("Responder"), buttonPendingIntent).addRemoteInput(remoteInput).build()

                val ref = storage.reference
                    .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                    .child(AnimalLoversConstants.STORAGE_ROOT_OWNER_PHOTOS.nome)
                    .child(ownerLoggedInfo.id)
                    .child(ownerLoggedInfo.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)
                try {
                    ref.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                        val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
                            notificationChannel.enableLights(false)
                            notificationChannel.lightColor = Color.WHITE
                            notificationChannel.enableVibration(false)
                            notificationManager.createNotificationChannel(notificationChannel)

                            builder = Notification.Builder(this, channelId)
                                .setSmallIcon(R.drawable.ic_coracao)
                                .setLargeIcon(bmp)
                                .setContentIntent(pendingIntent)
                                .setContentTitle(messageBody["title"])
                                .setContentText(messageBody["body"])
                                .addAction(action)


                            notificationManager.notify(idNotification!!.toInt(), builder.build())
                        }
                        else {
                            builder = Notification.Builder(this)
                                .setSmallIcon(R.drawable.ic_coracao)
                                .setLargeIcon(bmp)
                                .setContentIntent(pendingIntent)
                                .setContentTitle(messageBody["title"])
                                .setContentText(messageBody["body"])
                                .addAction(
                                    R.drawable.animalovers_logo_simples,
                                    titleOfNotificationButton,
                                    buttonPendingIntent
                                )
                            notificationManager.notify(idNotification!!.toInt(), builder.build())

                        }
                    }.addOnFailureListener { itException ->
                        println(itException.toString())
                    }
                } catch (ex: Exception) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationChannel = NotificationChannel(
                            channelId,
                            description,
                            NotificationManager.IMPORTANCE_HIGH
                        )
                        notificationChannel.enableLights(false)
                        notificationChannel.lightColor = Color.WHITE
                        notificationChannel.enableVibration(false)
                        notificationManager.createNotificationChannel(notificationChannel)

                        builder = Notification.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_coracao)
                            .setLargeIcon(
                                BitmapFactory.decodeResource(
                                    resources,
                                    R.drawable.animalovers_logo_simples
                                )
                            )
                            .setContentIntent(pendingIntent)
                            .setContentTitle(messageBody["title"])
                            .setContentText(messageBody["body"])
                            .addAction(action)
                        notificationManager.notify(idNotification!!.toInt(), builder.build())

                    }
                    else {
                        builder = Notification.Builder(this)
                            .setSmallIcon(R.drawable.ic_coracao)
                            .setLargeIcon(
                                BitmapFactory.decodeResource(
                                    resources,
                                    R.drawable.animalovers_logo_simples
                                )
                            )
                            .setContentIntent(pendingIntent)
                            .setContentTitle(messageBody["title"])
                            .setContentText(messageBody["body"])
                            .addAction(action)
                        notificationManager.notify(idNotification!!.toInt(), builder.build())

                    }
                }
            } catch (e: JSONException) {
                println(e.toString())
            }
        } else if (type == "message") {

        }
    }
}