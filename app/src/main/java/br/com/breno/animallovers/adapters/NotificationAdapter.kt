package br.com.breno.animallovers.adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.com.breno.animallovers.R
import br.com.breno.animallovers.constants.KindOfPet
import br.com.breno.animallovers.constants.StatusSolicitacaoAmizade
import br.com.breno.animallovers.model.*
import br.com.breno.animallovers.notification.KindOfNotification
import br.com.breno.animallovers.service.*
import br.com.breno.animallovers.ui.activity.ProfilePetActivity
import br.com.breno.animallovers.ui.activity.SinglePostActivity
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationAdapter(context: Context, list: MutableList<Notification>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var dateUtils = DateUtils()
    private var notificationService = NotificationService(context)
    private var postService = PostService(context)
    private var commentsService = CommentsService(context)
    private var likeService = LikeService(context)
    private var friendShipService = FriendShipService(context)
    private val myPreferences = ProjectPreferences(context)
    private val database = Firebase.database.reference


    private var hasPetLikedComment = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == VIEW_TYPE_ZERO_TYPE_POSTS) {
            return ViewPostViewHolder(
                LayoutInflater.from(context).inflate(R.layout.notification_related_to_post_item, parent, false)
            )
        }
        return ViewFriendshipViewHolder(
            LayoutInflater.from(context).inflate(R.layout.notification_related_to_friendship_item, parent, false)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (list[position].notificationType === VIEW_TYPE_ZERO_TYPE_POSTS) {
            (holder as ViewPostViewHolder).bind(position)
        } else {
            (holder as ViewFriendshipViewHolder).bind(position)
        }
    }

    companion object {
        const val VIEW_TYPE_ZERO_TYPE_POSTS = 0
        const val VIEW_TYPE_ONE_TYPE_FRIENDSHIP = 1
    }

    private val context: Context = context
    var list: MutableList<Notification> = list

    private inner class ViewPostViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var petName: TextView = itemView.findViewById(R.id.tv_pet_name_post_notification_item)
        var notificationDiscription : TextView = itemView.findViewById(R.id.tv_post_notification_description_notification_item)
        var datetime : TextView = itemView.findViewById(R.id.tv_datetime_notification_post_item)

        var iconFav : ImageView = itemView.findViewById(R.id.iv_action_fav)
        var isNotificationUnread : ImageView = itemView.findViewById(R.id.iv_status_of_notification_post_notification_item)
        var actionsNotification : ImageView = itemView.findViewById(R.id.iv_icon_options_post_notification_item)
        var petProfilePhoto : CircleImageView = itemView.findViewById(R.id.iv_icon_profile_photo_post_notification)
        var layoutPost : ConstraintLayout = itemView.findViewById(R.id.cons_lay_post_notification)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(position: Int) {
            val notification = list[position]
            val pet = notification.petRemetente
            val storage = FirebaseStorage.getInstance()
            var shouldOpenCommentsInIntent = false
            var postInfo = Post()
            var comentario = Comentario()

            when (notification.tipo) {
                KindOfNotification.LIKED_COMMENT.nome -> {
                    notificationDiscription.text = pet.nome + " " + itemView.context.resources.getText(R.string.liked_your_comment)
                    iconFav.visibility = INVISIBLE
                }
                KindOfNotification.LIKED_POST.nome -> {
                    notificationDiscription.text = pet.nome + " " + itemView.context.resources.getText(R.string.liked_your_post)
                    iconFav.visibility = INVISIBLE
                }
                KindOfNotification.COMMENTED_POST.nome -> {
                    notificationDiscription.text = pet.nome + " " + itemView.context.resources.getText(R.string.commented_your_post)

                }
            }

            datetime.text = dateUtils.dateDiffInTextFormat(LocalDateTime.parse(notification.dataHora, DateTimeFormatter.ofPattern(DateUtils.dateFrmt())))

            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        when (notification.tipo) {
                            KindOfNotification.LIKED_COMMENT.nome -> {
                                shouldOpenCommentsInIntent = true
                                postInfo = notification.postNotification
                            }
                            KindOfNotification.COMMENTED_POST.nome -> {
                                shouldOpenCommentsInIntent = true
                                postInfo = postService.getPostByUniqueIdOfCommentInPost(snapshot, notification.idActionNotification, notification.petRemetente)
                                comentario = commentsService.getCommentByUniqueIdOfCommentInPost(snapshot, notification.idActionNotification, notification.petRemetente)

                                if(comentario.textoComentario != "") {
                                    notificationDiscription.text = pet.nome + " " + itemView.context.resources.getText(R.string.commented_your_post) + ":\n\"" + comentario.textoComentario + "\""
                                }
                            }
                            KindOfNotification.LIKED_POST.nome -> {
                                shouldOpenCommentsInIntent = false
                                postInfo = postService.getPostByUniqueIdOfLikeInPost(snapshot, notification.idActionNotification, notification.petRemetente)
                            }
                        }

                        layoutPost.setOnClickListener {
                            layoutPost.setBackgroundColor(itemView.context.getColor(R.color.backgroundNotificationItem))
                            isNotificationUnread.setImageResource((R.drawable.ic_read_notiication))
                            if(postInfo.uniqueIdPost == "" || !postInfo.postAtivo) {
                                Toasty.error(context, "Tivemos um problema para encontrar a publicação").show()
                            }
                            else {
                                val intent = Intent(context, SinglePostActivity::class.java)
                                intent.putExtra("POST_INFO", postInfo)
                                intent.putExtra("CAME_FROM_NOTIFICATION", false)
                                intent.putExtra("SHOULD_INFLATE_COMMENT", shouldOpenCommentsInIntent)

                                changeNotificationStatus(notification, true)

                                ContextCompat.startActivity(context, intent, Bundle())
                            }
                        }

                        if(iconFav.visibility != INVISIBLE) {
                            if(notification.tipo == KindOfNotification.COMMENTED_POST.nome) {
                                hasPetLikedComment = commentsService.checkIfUserLikedComment(postInfo, comentario, snapshot)

                                if (hasPetLikedComment) {
                                    iconFav.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY)
                                }
                                else {
                                    iconFav.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint), android.graphics.PorterDuff.Mode.MULTIPLY)
                                }
                            }
                        }

                        iconFav.setOnClickListener {
                            if(iconFav.visibility != INVISIBLE) {
                                layoutPost.setBackgroundColor(itemView.context.getColor(R.color.backgroundNotificationItem))
                                isNotificationUnread.setImageResource((R.drawable.ic_read_notiication))
                                if(notification.tipo == KindOfNotification.COMMENTED_POST.nome) {
                                    val likeComment = likeService.checkIfUserLikedComment(snapshot, postInfo, comentario)
                                    val notificationModel = notificationService.getNotificationModelOfLikeInComment(snapshot, comentario, likeComment)

                                    hasPetLikedComment = if (hasPetLikedComment) {
                                        likeService.dislikeComment(postInfo, comentario, likeComment, notificationModel)
                                        iconFav.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint), android.graphics.PorterDuff.Mode.MULTIPLY)
                                        false
                                    } else {
                                        val likeInComment = likeService.likeComment(postInfo, comentario, likeComment)
                                        iconFav.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY)

                                        if(comentario.idOwner != auth.uid) {
                                            notificationService.sendNotificationOfLikedComment(postInfo, comentario, snapshot, likeInComment, notificationModel)
                                        }
                                        true
                                    }
                                }
                            }
                        }

                        actionsNotification.setOnClickListener {
                            val popupMenu = PopupMenu(context, itemView, Gravity.RIGHT)

                            popupMenu.setOnMenuItemClickListener {
                                when (it.itemId) {
                                    R.id.item_change_viewed_notification_options -> {

                                        var statusNotification = notification.visualizada

                                        statusNotification = !statusNotification

                                        if(!statusNotification) {
                                            layoutPost.setBackgroundColor(itemView.context.getColor(R.color.backgroundNotificationItemUnread))
                                            isNotificationUnread.setImageResource((R.drawable.ic_unread_notification))
                                        }
                                        else {
                                            layoutPost.setBackgroundColor(itemView.context.getColor(R.color.backgroundNotificationItem))
                                            isNotificationUnread.setImageResource((R.drawable.ic_read_notiication))
                                        }

                                        changeNotificationStatus(notification, statusNotification)

                                        return@setOnMenuItemClickListener true
                                    }
                                    R.id.item_remove_notification_options -> {
                                        changeNotificationStatusAndActive(notification, status = true, active = false)

                                        list.removeAt(position)

                                        notifyItemRemoved(position)
                                        notifyItemRangeChanged(position, list.toMutableList().size)
                                        itemView.visibility = INVISIBLE
                                        return@setOnMenuItemClickListener true
                                    }
                                }
                                return@setOnMenuItemClickListener false
                            }
                            popupMenu.inflate(R.menu.menu_options_notification)
                            popupMenu.show()
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.toString())
                    }

                })
            if(pet.pathFotoPerfil != "") {

                var storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome)
                    .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                    .child(pet.idOwner)
                    .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

                storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                    val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                    petName.text = pet.nome
                    petProfilePhoto.setImageBitmap(bmp)
                }.addOnFailureListener {

                }
            }
            else {
                when (pet.tipo) {
                    KindOfPet.DOG.tipo -> {
                        petProfilePhoto.setImageResource(R.drawable.ic_dog_pet)
                    }
                    KindOfPet.CAT.tipo -> {
                        petProfilePhoto.setImageResource(R.drawable.ic_cat_pet)
                    }
                    KindOfPet.BIRD.tipo -> {
                        petProfilePhoto.setImageResource(R.drawable.ic_bird_pet)
                    }
                    else -> {
                        petProfilePhoto.setImageResource(R.drawable.ic_unkown_pet)
                    }
                }
            }

            if(!notification.visualizada) {
                layoutPost.setBackgroundColor(itemView.context.getColor(R.color.backgroundNotificationItemUnread))
                isNotificationUnread.setImageResource((R.drawable.ic_unread_notification))
            }
        }
    }

    private inner class ViewFriendshipViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var petName: TextView = itemView.findViewById(R.id.tv_pet_name_friendship_notification_item)
        var notificationDiscription : TextView = itemView.findViewById(R.id.tv_friendship_notification_description_notification_item)
        var datetime : TextView = itemView.findViewById(R.id.tv_datetime_notification_friendship_item)
        var iconAccept : Button = itemView.findViewById(R.id.btn_confirm_friendship_request_notification_item)
        var iconDecline : Button = itemView.findViewById(R.id.btn_decline_friendship_request_notification_item)
        var isNotificationUnread : ImageView = itemView.findViewById(R.id.iv_status_of_notification_friendship_notification_item)
        var actionsNotification : ImageView = itemView.findViewById(R.id.iv_icon_options_friendship_notification_item)
        var petProfilePhoto : CircleImageView = itemView.findViewById(R.id.iv_icon_profile_photo_friendship_notification)
        var layoutFriendship : ConstraintLayout = itemView.findViewById(R.id.cons_lay_friendship_notification)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(position: Int) {
            val notification = list[position]
            val pet = notification.petRemetente
            val storage = FirebaseStorage.getInstance()

            datetime.text = dateUtils.dateDiffInTextFormat(LocalDateTime.parse(notification.dataHora, DateTimeFormatter.ofPattern(DateUtils.dateFrmt())))

            layoutFriendship.setOnClickListener {
                layoutFriendship.setBackgroundColor(itemView.context.getColor(R.color.backgroundNotificationItem))
                isNotificationUnread.setImageResource((R.drawable.ic_read_notiication))

                val intent = Intent(context, ProfilePetActivity::class.java)
                intent.putExtra("PET_INFO_PROFILE", pet)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                changeNotificationStatus(notification, true)

                ContextCompat.startActivity(context, intent, Bundle())
            }
            actionsNotification.setOnClickListener {
                Toasty.success(itemView.context,"ffAAAAAAAA").show()
            }

            if(pet.pathFotoPerfil != "") {

                var storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome)
                    .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                    .child(pet.idOwner)
                    .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

                storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                    val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                    petName.text = pet.nome
                    petProfilePhoto.setImageBitmap(bmp)
                }.addOnFailureListener {

                }
            }
            else {
                when (pet.tipo) {
                    KindOfPet.DOG.tipo -> {
                        petProfilePhoto.setImageResource(R.drawable.ic_dog_pet)
                    }
                    KindOfPet.CAT.tipo -> {
                        petProfilePhoto.setImageResource(R.drawable.ic_cat_pet)
                    }
                    KindOfPet.BIRD.tipo -> {
                        petProfilePhoto.setImageResource(R.drawable.ic_bird_pet)
                    }
                    else -> {
                        petProfilePhoto.setImageResource(R.drawable.ic_unkown_pet)
                    }
                }
            }

            if(notification.tipo == KindOfNotification.FRIENDSHIP_REQUEST_RECEIVED.nome) {
                notificationDiscription.text = pet.nome + " enviou-lhe uma solicitação de amizade"
            }
            else if(notification.tipo == KindOfNotification.FRIENDSHIP_REQUEST_ACCEPTED.nome) {
                notificationDiscription.text = pet.nome + " aceitou a sua solicitação de amizade"
                iconAccept.visibility = INVISIBLE
                iconDecline.visibility = INVISIBLE
            }

            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        var owner = snapshot.child(pet.idOwner).child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome).getValue<Conta>()!!
                        var solicitacaoAmizade = SolicitacaoAmizade()

                        var dSnapshot = snapshot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString())
                        val petLogged = dSnapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>()!!
                        if (dSnapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)) {
                            if (dSnapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).hasChild(pet.idOwner)) {
                                if (dSnapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).child(pet.idOwner).hasChild(pet.id)) {
                                    solicitacaoAmizade = dSnapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                                        .child(pet.idOwner)
                                        .child(pet.id)
                                        .getValue<SolicitacaoAmizade>()!!

                                }
                            }
                        }
                        iconAccept.setOnClickListener {
                            if(iconAccept.visibility != INVISIBLE) {
                                layoutFriendship.setBackgroundColor(itemView.context.getColor(R.color.backgroundNotificationItem))
                                isNotificationUnread.setImageResource((R.drawable.ic_read_notiication))
                                if (notification.tipo == KindOfNotification.FRIENDSHIP_REQUEST_RECEIVED.nome) {
                                    acceptFriendshipRequest(notification.petRemetente, petLogged, solicitacaoAmizade, owner, snapshot)
                                    changeNotificationStatusAndActive(notification, status = true, active = false)
                                }
                            }
                        }

                        iconDecline.setOnClickListener {
                            if(iconDecline.visibility != INVISIBLE) {
                                layoutFriendship.setBackgroundColor(itemView.context.getColor(R.color.backgroundNotificationItem))
                                isNotificationUnread.setImageResource((R.drawable.ic_read_notiication))
                                if (notification.tipo == KindOfNotification.FRIENDSHIP_REQUEST_RECEIVED.nome) {
                                    declineFriendshipRequest(pet, solicitacaoAmizade)
                                    changeNotificationStatusAndActive(notification, status = true, active = false)
                                }
                            }
                        }

                        actionsNotification.setOnClickListener {
                            val popupMenu = PopupMenu(context, itemView, Gravity.RIGHT)

                            popupMenu.setOnMenuItemClickListener {
                                when (it.itemId) {
                                    R.id.item_change_viewed_notification_options -> {

                                        var statusNotification = notification.visualizada

                                        statusNotification = !statusNotification

                                        if(!statusNotification) {
                                            layoutFriendship.setBackgroundColor(itemView.context.getColor(R.color.backgroundNotificationItemUnread))
                                            isNotificationUnread.setImageResource((R.drawable.ic_unread_notification))
                                        }
                                        else {
                                            layoutFriendship.setBackgroundColor(itemView.context.getColor(R.color.backgroundNotificationItem))
                                            isNotificationUnread.setImageResource((R.drawable.ic_read_notiication))
                                        }

                                        changeNotificationStatus(notification, statusNotification)

                                        return@setOnMenuItemClickListener true
                                    }
                                    R.id.item_remove_notification_options -> {
                                        changeNotificationStatusAndActive(notification, status = true, active = false)

                                        list.removeAt(position)

                                        notifyItemRemoved(position)
                                        notifyItemRangeChanged(position, list.toMutableList().size)
                                        itemView.visibility = INVISIBLE
                                        return@setOnMenuItemClickListener true
                                    }
                                }
                                return@setOnMenuItemClickListener false
                            }
                            popupMenu.inflate(R.menu.menu_options_notification)
                            popupMenu.show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.toString())
                    }
                })

            if(!notification.visualizada) {
                layoutFriendship.setBackgroundColor(itemView.context.getColor(R.color.backgroundNotificationItemUnread))
                isNotificationUnread.setImageResource((R.drawable.ic_unread_notification))
            }
        }

    }

    fun acceptFriendshipRequest(pet: Pet, petLogged : Pet, solicitacao: SolicitacaoAmizade, owner : Conta, snapshots : DataSnapshot) {
            val dataInicioAmizade = DateUtils.dataFormatWithMilliseconds()
            solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.ACCEPTED.status
            //Persiste a solicitação aceita no usuário
            friendShipService.persistFriendShipRequestInReceiver(solicitacao, pet)

            //Persiste na lista de amigos o novo amigo
            friendShipService.saveNewFriendShipReceiver(dataInicioAmizade, pet)

            //-----------------------------------------------------------------------//
            //Desfaz a solicitação no perfil do pet que enviou
            friendShipService.persistFriendShipRequestInSender(solicitacao, pet)
            notificationService.persistNotificationOfNewFriendshipAccepted(petLogged, pet, snapshots, solicitacao)

            //Persiste na lista de amigos o novo amigo
            friendShipService.saveNewFriendShipSender(dataInicioAmizade, pet)

            notificationService.sendNotificationOfFriendshipAccepted(pet, petLogged, owner)
    }

    fun declineFriendshipRequest(pet: Pet, solicitacao: SolicitacaoAmizade) {
        solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.CANCELLED.status
        //Desfaz a solicitação no usuário
        friendShipService.undoFriendshipInSender(pet, solicitacao)

        //Desfaz a solicitação no perfil do pet que enviou
        friendShipService.undoFriendShipRequestInReceiver(pet, solicitacao)
    }

    private fun changeNotificationStatus(notification: Notification, status : Boolean) {
        notification.visualizada = status

        notificationService.updateNotificationInReceiver(notification)
    }

    private fun changeNotificationStatusAndActive(notification: Notification, status : Boolean, active : Boolean) {
        notification.visualizada = status
        notification.ativo = active

        notificationService.updateNotificationInReceiver(notification)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].notificationType
    }
}