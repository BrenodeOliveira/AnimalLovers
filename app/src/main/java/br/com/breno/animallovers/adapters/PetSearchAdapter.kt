package br.com.breno.animallovers.adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.breno.animallovers.R
import br.com.breno.animallovers.constants.KindOfPet
import br.com.breno.animallovers.constants.StatusSolicitacaoAmizade
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.model.SolicitacaoAmizade
import br.com.breno.animallovers.service.FriendShipService
import br.com.breno.animallovers.service.NotificationService
import br.com.breno.animallovers.service.PetService
import br.com.breno.animallovers.ui.activity.ProfilePetActivity
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.analize_friendship_request.*
import kotlinx.android.synthetic.main.analize_friendship_request.view.*
import kotlinx.android.synthetic.main.cancel_friendship_request.*
import kotlinx.android.synthetic.main.cancel_friendship_request.view.*
import kotlinx.android.synthetic.main.pet_search_item.view.*
import kotlinx.android.synthetic.main.undo_friendship.*
import kotlinx.android.synthetic.main.undo_friendship.view.*
import java.util.*


class PetSearchAdapter(private val pets: List<Pet>, private val context: Context) : RecyclerView.Adapter<PetSearchAdapter.ViewHolder>() {
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var dBase: DatabaseReference
    private var friendShipService = FriendShipService(context)
    private var notificationService = NotificationService(context)
    private var solicitacao = SolicitacaoAmizade()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.pet_search_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = pets[(pets.size - 1) - position]

        holder.let {
            when (pet.tipo) {
                KindOfPet.DOG.tipo -> {
                    it.photoProfile.setImageResource(R.drawable.ic_dog_pet)
                }
                KindOfPet.CAT.tipo -> {
                    it.photoProfile.setImageResource(R.drawable.ic_cat_pet)
                }
                KindOfPet.BIRD.tipo -> {
                    it.photoProfile.setImageResource(R.drawable.ic_bird_pet)
                }
                else -> {
                    it.photoProfile.setImageResource(R.drawable.ic_unkown_pet)
                }
            }
        }


        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        val myPreferences = ProjectPreferences(context)

        if (pet.pathFotoPerfil != "") {
            var storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(pet.idOwner)
                .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)
            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                holder.let {
                    it.name.text = pet.nome
                    it.photoProfile.setImageBitmap(bmp)

                    if(it.photoProfile.drawable == null) {
                        it.photoProfile.visibility = View.INVISIBLE
                    }
                }
            }.addOnFailureListener {

            }
        }

        holder.let {
            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).addListenerForSingleValueEvent(
                object :
                    ValueEventListener {
                    override fun onDataChange(snapshots: DataSnapshot) {
                        var snapshot = snapshots.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString())
                        if (snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)) {
                            if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).hasChild(pet.idOwner)) {
                                if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).child(pet.idOwner).hasChild(pet.id)) {
                                    var solicitacaoAmizade = snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                                        .child(pet.idOwner)
                                        .child(pet.id)
                                        .getValue<SolicitacaoAmizade>()!!

                                    when (solicitacaoAmizade.statusSolicitacao) {
                                        StatusSolicitacaoAmizade.ACCEPTED.status -> it.icSendFriendShipRequest.setImageResource(R.drawable.ic_coracao_aceitar)
                                        StatusSolicitacaoAmizade.WAITING.status -> it.icSendFriendShipRequest.setImageResource(R.drawable.ic_relogio_waiting_friend_request)
                                        StatusSolicitacaoAmizade.SENT.status -> it.icSendFriendShipRequest.setImageResource(R.drawable.ic_coracao_espera)
                                    }
                                }
                                else {
                                    it.icSendFriendShipRequest.setImageResource(R.drawable.ic_coracao)
                                }
                            }
                            else {
                                it.icSendFriendShipRequest.setImageResource(R.drawable.ic_coracao)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.toString())
                    }

                })

            it.icSendFriendShipRequest.setOnClickListener {

                database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).addListenerForSingleValueEvent(
                    object :
                        ValueEventListener {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onDataChange(snapshots: DataSnapshot) {

                            var owner = snapshots.child(pet.idOwner).child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome).getValue<Conta>()!!

                            var snapshot = snapshots.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString())
                            val petLogged = snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>()!!
                            if (snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)) {
                                if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).hasChild(pet.idOwner)) {
                                    if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).child(pet.idOwner).hasChild(pet.id)) {
                                        var solicitacaoAmizade = snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                                            .child(pet.idOwner)
                                            .child(pet.id)
                                            .getValue<SolicitacaoAmizade>()!!

                                        when (solicitacaoAmizade.statusSolicitacao) {
                                            StatusSolicitacaoAmizade.SENT.status -> chamarMyDialogCancelFriendshipRequest(pet, solicitacaoAmizade)
                                            StatusSolicitacaoAmizade.WAITING.status -> chamarMyDialogAnalizeFriendshipRequest(pet, petLogged, solicitacaoAmizade, owner, snapshots)
                                            StatusSolicitacaoAmizade.ACCEPTED.status -> chamarMyDialogUndoFriendship(pet, solicitacaoAmizade)
                                            StatusSolicitacaoAmizade.CANCELLED.status -> {
                                                sendFriendShipRequest(pet, petLogged, solicitacao, owner, snapshots)
                                                it.iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
                                            }
                                        }
                                    } else {
                                        sendFriendShipRequest(pet, petLogged, solicitacao, owner, snapshots)
                                        it.iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
                                    }
                                } else {
                                    sendFriendShipRequest(pet, petLogged, solicitacao, owner, snapshots)
                                    it.iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
                                }
                            } else {
                                sendFriendShipRequest(pet, petLogged, solicitacao, owner, snapshots)
                                it.iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            println(error.toString())
                        }
                    })
            }

            it.layoutItem.setOnClickListener {
                val intent = Intent(context, ProfilePetActivity::class.java)
                intent.putExtra("PET_INFO_PROFILE", pet)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                startActivity(context, intent, Bundle())
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.tv_pet_name_search_pets
        var photoProfile: ImageView = itemView.iv_icon_foto_perfil_search_pets
        var icSendFriendShipRequest : ImageView = itemView.iv_add_friend_search_pets
        var layoutItem : ConstraintLayout = itemView.constraint_add_friend

        fun bindView(post: Post, pet: Pet) {
            val name = itemView.tv_pet_name_search_pets

            name.text = pet.nome
        }
    }

    fun sendFriendShipRequest(pet: Pet, petLogged : Pet, solicitacao: SolicitacaoAmizade, owner : Conta, snapshots: DataSnapshot) {
        //Adiciona no perfil desse usuário
        solicitacao.dataEnvioSolicitacao = DateUtils.dataFormatWithMilliseconds()
        solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.SENT.status

        friendShipService.persistFriendShipRequestInReceiver(solicitacao, pet)

        //Adiciona no perfil da solicitação enviada
        solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.WAITING.status

        friendShipService.persistFriendShipRequestInSender(solicitacao, pet)

        notificationService.persistNotificationOfNewFriendshipRequest(petLogged, pet, snapshots, solicitacao)
        notificationService.sendNotificationOfFriendshiRequestReceived(pet, petLogged, owner)
    }

    fun chamarMyDialogCancelFriendshipRequest(pet: Pet, solicitacao: SolicitacaoAmizade) {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.cancel_friendship_request, null)
        val mBuilder = AlertDialog.Builder(context).setView(mDialogView).setTitle("Solicitação de amizade")
        val mAlertDialog = mBuilder.show()
        mAlertDialog.tv_pet_name_cancel_friendship_request.text = pet.nome

        if(pet.pathFotoPerfil != "") {
            var storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(pet.idOwner)
                .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                mAlertDialog.iv_icon_foto_perfil_cancel_friendship_request.setImageBitmap(bmp)

                if(mAlertDialog.iv_icon_foto_perfil_cancel_friendship_request.drawable == null) {
                    mAlertDialog.iv_icon_foto_perfil_cancel_friendship_request.visibility = View.INVISIBLE
                }
            }
        }
        else {
            when (pet.tipo) {
                KindOfPet.DOG.tipo -> {
                    mAlertDialog.iv_icon_foto_perfil_cancel_friendship_request.setImageResource(R.drawable.ic_dog_pet)
                }
                KindOfPet.CAT.tipo -> {
                    mAlertDialog.iv_icon_foto_perfil_cancel_friendship_request.setImageResource(R.drawable.ic_cat_pet)
                }
                KindOfPet.BIRD.tipo -> {
                    mAlertDialog.iv_icon_foto_perfil_cancel_friendship_request.setImageResource(R.drawable.ic_bird_pet)
                }
                else -> {
                    mAlertDialog.iv_icon_foto_perfil_cancel_friendship_request.setImageResource(R.drawable.ic_unkown_pet)
                }
            }
        }
        mDialogView.btn_cancel_friendship_request.setOnClickListener {
            dBase = Firebase.database.reference
            solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.CANCELLED.status

            //Desfaz a solicitação no usuário
            friendShipService.undoFriendshipInSender(pet, solicitacao)

            //Desfaz a solicitação no perfil do pet que enviou
            friendShipService.undoFriendshipInReceiver(pet, solicitacao)


            mAlertDialog.dismiss()
        }

        mDialogView.btn_donot_cancel_friendship_request.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    fun chamarMyDialogAnalizeFriendshipRequest(pet: Pet, petLogged : Pet, solicitacao: SolicitacaoAmizade, owner : Conta, snapshots : DataSnapshot) {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.analize_friendship_request, null)
        val mBuilder = AlertDialog.Builder(context).setView(mDialogView).setTitle("Solicitação de amizade")
        val mAlertDialog = mBuilder.show()
        mAlertDialog.tv_pet_name_analize_friendship_request.text = pet.nome

        dBase = Firebase.database.reference
        val myPreferences = ProjectPreferences(context)

        if(pet.pathFotoPerfil != "") {
            var storageRef = storage.reference.child(AnimalLoversConstants.STORAGE_ROOT.nome)
                .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                .child(pet.idOwner)
                .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                mAlertDialog.iv_icon_foto_perfil_analize_friendship_request.setImageBitmap(bmp)

                if(mAlertDialog.iv_icon_foto_perfil_analize_friendship_request.drawable == null) {
                    mAlertDialog.iv_icon_foto_perfil_analize_friendship_request.visibility = View.INVISIBLE
                }
            }
        }
        else {
            when (pet.tipo) {
                KindOfPet.DOG.tipo -> {
                    mAlertDialog.iv_icon_foto_perfil_analize_friendship_request.setImageResource(R.drawable.ic_dog_pet)
                }
                KindOfPet.CAT.tipo -> {
                    mAlertDialog.iv_icon_foto_perfil_analize_friendship_request.setImageResource(R.drawable.ic_cat_pet)
                }
                KindOfPet.BIRD.tipo -> {
                    mAlertDialog.iv_icon_foto_perfil_analize_friendship_request.setImageResource(R.drawable.ic_bird_pet)
                }
                else -> {
                    mAlertDialog.iv_icon_foto_perfil_analize_friendship_request.setImageResource(R.drawable.ic_unkown_pet)
                }
            }
        }
        mDialogView.btn_confirm_friendship_request.setOnClickListener {

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

            mAlertDialog.dismiss()

            notificationService.sendNotificationOfFriendshipAccepted(pet, petLogged, owner)
        }

        mDialogView.btn_decline_friendship_request.setOnClickListener {
            solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.CANCELLED.status
            //Desfaz a solicitação no usuário
            friendShipService.undoFriendshipInSender(pet, solicitacao)

            //Desfaz a solicitação no perfil do pet que enviou
            friendShipService.undoFriendShipRequestInReceiver(pet, solicitacao)
            mAlertDialog.dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun chamarMyDialogUndoFriendship(pet: Pet, solicitacao: SolicitacaoAmizade) {
        val myPreferences = ProjectPreferences(context)
        dBase = Firebase.database.reference
        var numMutualFriends = 0

        val mDialogView = LayoutInflater.from(context).inflate(R.layout.undo_friendship, null)
        val mBuilder = AlertDialog.Builder(context).setView(mDialogView).setTitle("Desfazer amizade")
        val mAlertDialog = mBuilder.show()
        mAlertDialog.tv_pet_name_undo_friendship.text = pet.nome


        val petService = PetService(context)

        mAlertDialog.tv_friends_since_undo_friendship.text = "Amigos há " + petService.friendlyTextFriendshipStatusSince(solicitacao)

        dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.child(pet.idOwner).child(pet.id).child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).hasChildren() && snapshot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).hasChildren()) {

                    val amgsSearchPet: HashMap<String, HashMap<String, HashMap<String, String>>> = snapshot.child(pet.idOwner)
                        .child(pet.id)
                        .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).value as HashMap<String, HashMap<String, HashMap<String, String>>>

                    val amgsPet: HashMap<String, HashMap<String, HashMap<String, String>>> = snapshot.child(auth.uid.toString())
                        .child(myPreferences.getPetLogged().toString())
                        .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).value as HashMap<String, HashMap<String, HashMap<String, String>>>

                    for (i in 0 until amgsPet.size) {
                        for (j in 0 until amgsSearchPet.size) {
                            val idOwnerAmgSearchPet = amgsSearchPet.keys.toList()[j]
                            val idOwnerAmgPet = amgsPet.keys.toList()[i]

                            if (idOwnerAmgSearchPet == idOwnerAmgPet) {

                                for (k in 0 until (amgsPet[idOwnerAmgPet]?.size ?: 0)) {
                                    for (l in 0 until (amgsSearchPet[idOwnerAmgSearchPet]?.size ?: 0)) {
                                        if (amgsPet[idOwnerAmgPet]?.keys?.toList()?.get(k)?.equals(
                                                amgsSearchPet[idOwnerAmgSearchPet]?.keys?.toList()?.get(l)!!)!!) {
                                            numMutualFriends++
                                        }
                                    }
                                }
                            }
                        }
                    }

                    when (numMutualFriends) {
                        0 -> {
                            mAlertDialog.tv_mutual_friends_undo_friendship.text = "Nenhum amigo em comum"
                        }
                        1 -> {
                            mAlertDialog.tv_mutual_friends_undo_friendship.text = numMutualFriends.toString() + " amigo em comum"
                        }
                        else -> {
                            mAlertDialog.tv_mutual_friends_undo_friendship.text = numMutualFriends.toString() + " amigos em comum"
                        }
                    }
                } else {
                    mAlertDialog.tv_mutual_friends_undo_friendship.text = "Nenhum amigo em comum"
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
                mAlertDialog.iv_icon_foto_perfil_undo_friendship.setImageBitmap(bmp)

                if(mAlertDialog.iv_icon_foto_perfil_undo_friendship.drawable == null) {
                    mAlertDialog.iv_icon_foto_perfil_undo_friendship.visibility = View.INVISIBLE
                }
            }
        }
        else {
            when (pet.tipo) {
                KindOfPet.DOG.tipo -> {
                    mAlertDialog.iv_icon_foto_perfil_undo_friendship.setImageResource(R.drawable.ic_dog_pet)
                }
                KindOfPet.CAT.tipo -> {
                    mAlertDialog.iv_icon_foto_perfil_undo_friendship.setImageResource(R.drawable.ic_cat_pet)
                }
                KindOfPet.BIRD.tipo -> {
                    mAlertDialog.iv_icon_foto_perfil_undo_friendship.setImageResource(R.drawable.ic_bird_pet)
                }
                else -> {
                    mAlertDialog.iv_icon_foto_perfil_undo_friendship.setImageResource(R.drawable.ic_unkown_pet)
                }
            }
        }
        mDialogView.btn_undo_friendship.setOnClickListener {
            solicitacao.statusSolicitacao = StatusSolicitacaoAmizade.CANCELLED.status

            //Desfaz a solicitação no usuário
            friendShipService.undoFriendShipRequestInSender(pet, solicitacao)


            friendShipService.persistFriendShipRequestInReceiver(solicitacao, pet)

            //Desfaz a solicitação no perfil do pet que enviou
            friendShipService.undoFriendShipRequestInReceiver(pet, solicitacao)

            friendShipService.persistFriendShipRequestInSender(solicitacao, pet)

            mAlertDialog.dismiss()
        }
        mDialogView.btn_donot_undo_friendship.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    override fun getItemCount(): Int {
        return pets.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

}