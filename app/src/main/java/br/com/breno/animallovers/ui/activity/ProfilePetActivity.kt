package br.com.breno.animallovers.ui.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.adapters.FeedAdapter
import br.com.breno.animallovers.constants.KindOfPet
import br.com.breno.animallovers.constants.StatusSolicitacaoAmizade
import br.com.breno.animallovers.model.*
import br.com.breno.animallovers.service.*
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile_pet.*
import kotlinx.android.synthetic.main.analize_friendship_request.*
import kotlinx.android.synthetic.main.analize_friendship_request.view.*
import kotlinx.android.synthetic.main.cancel_friendship_request.*
import kotlinx.android.synthetic.main.cancel_friendship_request.view.*
import kotlinx.android.synthetic.main.undo_friendship.*
import kotlinx.android.synthetic.main.undo_friendship.view.*
import java.util.*

class ProfilePetActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    var db = Firebase.database.reference
    private var base: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private var petInfo = Pet()
    private var dono = Conta()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_pet)
        petInfo = (intent.getSerializableExtra("PET_INFO_PROFILE") as? Pet)!!

        refreshPetInfo(petInfo)
        loadPetInfo(petInfo)
        loadPetPosts(petInfo)

        seeFriends(petInfo)
        getOwnerLoginStatus(petInfo)

        val myPreferences = ProjectPreferences(baseContext)

        if(petInfo.id != myPreferences.getPetLogged().toString() || petInfo.idOwner != auth.uid.toString()) {
            friendshipStatus(petInfo)
        }
    }

    private fun friendshipStatus(petInfo: Pet) {
        val myPreferences = ProjectPreferences(baseContext)

        var solicitacaoAmizade = SolicitacaoAmizade()

        db.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).addListenerForSingleValueEvent(
            object :
                ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshots: DataSnapshot) {
                    var snapshot = snapshots.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString())
                    if (snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)) {
                        if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).hasChild(petInfo.idOwner)) {
                            if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).child(petInfo.idOwner).hasChild(petInfo.id)) {
                                solicitacaoAmizade = snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                                    .child(petInfo.idOwner)
                                    .child(petInfo.id)
                                    .getValue<SolicitacaoAmizade>()!!

                                when (solicitacaoAmizade.statusSolicitacao) {
                                    StatusSolicitacaoAmizade.ACCEPTED.status -> iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_aceitar)
                                    StatusSolicitacaoAmizade.WAITING.status -> iv_add_friend_search_pets.setImageResource(R.drawable.ic_relogio_waiting_friend_request)
                                    StatusSolicitacaoAmizade.SENT.status -> iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
                                }
                            }
                            else {
                                iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao)
                            }
                        }
                        else {
                            iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao)
                        }
                    }

                    iv_add_friend_search_pets.setOnClickListener {
                        val petLogged = snapshot.child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>()!!

                        val owner = snapshots.child(petInfo.idOwner).child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome).getValue<Conta>()!!
                        when (solicitacaoAmizade.statusSolicitacao) {
                            StatusSolicitacaoAmizade.ACCEPTED.status -> chamarMyDialogUndoFriendship(petInfo, solicitacaoAmizade, snapshots)
                            StatusSolicitacaoAmizade.WAITING.status -> chamarMyDialogAnalizeFriendshipRequest(petInfo, petLogged, solicitacaoAmizade, owner, snapshots)
                            StatusSolicitacaoAmizade.SENT.status -> chamarMyDialogCancelFriendshipRequest(petInfo, solicitacaoAmizade)
                            else -> {
                                sendFriendshipRequest(petInfo, petLogged, snapshots, solicitacaoAmizade)
                            }
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }

            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendFriendshipRequest(petInfo: Pet, petLogged : Pet, snapshots : DataSnapshot, solicitacaoAmizade: SolicitacaoAmizade) {
        val solicitacao = SolicitacaoAmizade()

        val owner = snapshots.child(petInfo.idOwner).child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome).getValue<Conta>()!!

        if(solicitacaoAmizade.statusSolicitacao == "") {
            solicitacaoAmizade.statusSolicitacao = StatusSolicitacaoAmizade.CANCELLED.status
        }

        when (solicitacaoAmizade.statusSolicitacao) {
            StatusSolicitacaoAmizade.SENT.status -> chamarMyDialogCancelFriendshipRequest(petInfo, solicitacaoAmizade)
            StatusSolicitacaoAmizade.WAITING.status -> chamarMyDialogAnalizeFriendshipRequest(petInfo, petLogged, solicitacaoAmizade, owner, snapshots)
            StatusSolicitacaoAmizade.ACCEPTED.status -> chamarMyDialogUndoFriendship(petInfo, solicitacaoAmizade, snapshots)
            StatusSolicitacaoAmizade.CANCELLED.status -> {
                performSendFriendshipRequest(petInfo, petLogged, solicitacao, owner, snapshots)
                iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
            }
        }
    }

    private fun performSendFriendshipRequest(pet: Pet, petLogged : Pet, solicitacao: SolicitacaoAmizade, owner : Conta, snapshots: DataSnapshot) {
        var friendShipService = FriendShipService(baseContext)
        var notificationService = NotificationService(baseContext)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun chamarMyDialogUndoFriendship(pet: Pet, solicitacao: SolicitacaoAmizade, snapshot: DataSnapshot) {
        var friendShipService = FriendShipService(baseContext)
        val myPreferences = ProjectPreferences(applicationContext)
        var numMutualFriends = 0

        val mDialogView = LayoutInflater.from(this@ProfilePetActivity).inflate(R.layout.undo_friendship, null)
        val mBuilder = AlertDialog.Builder(this@ProfilePetActivity).setView(mDialogView).setTitle("Desfazer amizade")
        val mAlertDialog = mBuilder.show()
        mAlertDialog.tv_pet_name_undo_friendship.text = pet.nome


        val petService = PetService(applicationContext)

        mAlertDialog.tv_friends_since_undo_friendship.text = "Amigos há " + petService.friendlyTextFriendshipStatusSince(solicitacao)

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

    private fun chamarMyDialogAnalizeFriendshipRequest(pet: Pet, petLogged : Pet, solicitacao: SolicitacaoAmizade, owner : Conta, snapshots : DataSnapshot) {
        var friendShipService = FriendShipService(baseContext)
        var notificationService = NotificationService(baseContext)

        val mDialogView = LayoutInflater.from(this@ProfilePetActivity).inflate(R.layout.analize_friendship_request, null)
        val mBuilder = AlertDialog.Builder(this@ProfilePetActivity).setView(mDialogView).setTitle("Solicitação de amizade")
        val mAlertDialog = mBuilder.show()
        mAlertDialog.tv_pet_name_analize_friendship_request.text = pet.nome

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

    private fun chamarMyDialogCancelFriendshipRequest(pet: Pet, solicitacao: SolicitacaoAmizade) {
        var friendShipService = FriendShipService(baseContext)
        val mDialogView = LayoutInflater.from(this@ProfilePetActivity).inflate(R.layout.cancel_friendship_request, null)
        val mBuilder = AlertDialog.Builder(this@ProfilePetActivity).setView(mDialogView).setTitle("Solicitação de amizade")
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

    private fun openChatWithPetOwner(petInfo: Pet, conta : Conta) {
        if(petInfo.idOwner == auth.uid.toString()) {
            return
        }

        var user = User(conta.id, conta.email, conta.usuario, conta.cidade, conta.pais, conta.pathFotoPerfil)

        val intent = Intent(applicationContext, ChatLogActivity::class.java)

        intent.putExtra(NewMessageActivity.USER_KEY, user)
        startActivity(intent)
    }

    private fun getOwnerLoginStatus(petInfo: Pet) {
        var donoService = DonoService()

        base.child(AnimalLoversConstants.DATABASE_ENTITY_CONTROL_LOGIN.nome).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var ownerLogin = snapshot.child(petInfo.idOwner).getValue<Login>()!!

                if(ownerLogin.logged) {
                    tv_status_pet_profile_pet.background = applicationContext.getDrawable(R.drawable.logged_owner_icon)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.toString())
            }

        })
    }

    private fun refreshPetInfo(pet : Pet) {
        swipe_refresh_pet_profile.setOnRefreshListener {
            loadPetPosts(pet)

            seeFriends(pet)
        }
    }

    private fun loadPetInfo(pet: Pet) {
        val petService = PetService(applicationContext)

        database = Firebase.database.reference

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(pet.idOwner)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshot: DataSnapshot) {
                    dono = snapshot.child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome)
                        .getValue<Conta>()!!

                    val numFriends = petService.numberOfFriendsPet(snapshot, pet)

                    val statusFriendShip = petService.statusFriendShip(pet, snapshot, baseContext)

                    if (dono.pathFotoPerfil != "") {
                        retrieveOwnerProfilePhoto(pet)
                    }
                    if (pet.pathFotoPerfil != "") {
                        var storageRef = storage.reference
                            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
                            .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
                            .child(pet.idOwner)
                            .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

                        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
                            val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                            iv_photo_profile_pet.setImageBitmap(bmp)

                        }.addOnFailureListener {

                        }
                    }
                    else {
                        when (pet.tipo) {
                            KindOfPet.DOG.tipo -> {
                                iv_photo_profile_pet.setImageResource(R.drawable.ic_dog_pet)
                            }
                            KindOfPet.CAT.tipo -> {
                                iv_photo_profile_pet.setImageResource(R.drawable.ic_cat_pet)
                            }
                            KindOfPet.BIRD.tipo -> {
                                iv_photo_profile_pet.setImageResource(R.drawable.ic_bird_pet)
                            }
                            else -> {
                                iv_photo_profile_pet.setImageResource(R.drawable.ic_unkown_pet)
                            }
                        }
                    }

                    tv_pet_name_profile_pet.text = pet.nome
                    tv_pet_description_profile_pet.text = pet.resumo

                    tv_friends_profile_pet.text = " Amigos"
                    tv_num_friends_profile_pet.text = numFriends.toString()

                    tv_pet_name_owner_profile_pet.text = dono.usuario

                    if (statusFriendShip != null) {
                        when (statusFriendShip.statusSolicitacao) {

                            StatusSolicitacaoAmizade.ACCEPTED.status -> {
                                iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_aceitar)
                            }

                            StatusSolicitacaoAmizade.WAITING.status -> {
                                iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
                            }

                            StatusSolicitacaoAmizade.SENT.status -> {
                                iv_add_friend_search_pets.setImageResource(R.drawable.ic_relogio_waiting_friend_request)
                            }
                        }
                    }
                    swipe_refresh_pet_profile.isRefreshing = false
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }

            })
    }

    private fun loadPetPosts(pet: Pet) {

        val postService = PostService(baseContext)
        var listPosts : MutableList<Post>


        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(pet.idOwner)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    dono = snapshot.child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome)
                        .getValue<Conta>()!!

                    val numPosts = snapshot.child(pet.id)
                        .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).childrenCount

                    tv_posts_profile_pet.text = " Posts"
                    tv_num_posts_profile_pet.text = numPosts.toString()

                    listPosts = postService.getAllPostsPet(pet, snapshot)

                    val recyclerView = findViewById<RecyclerView>(R.id.recycler_posts_profile_pet)
                    recyclerView.layoutManager = LinearLayoutManager(this@ProfilePetActivity)
                    recyclerView.adapter = FeedAdapter(listPosts, this@ProfilePetActivity, false)

                    val layoutManager = StaggeredGridLayoutManager(
                        1, StaggeredGridLayoutManager.VERTICAL
                    )
                    recyclerView.layoutManager = layoutManager
                    swipe_refresh_pet_profile.isRefreshing = false

                    iv_open_chat_profile_pet.setOnClickListener {
                        openChatWithPetOwner(petInfo, dono)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }

            })

    }

    private fun retrieveOwnerProfilePhoto(pet: Pet) {
        storage = FirebaseStorage.getInstance()
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.STORAGE_ROOT_OWNER_PHOTOS.nome)
            .child(pet.idOwner)
            .child(pet.idOwner + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytesPrm ->
            val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
            iv_icon_photo_owner_profile_pet.setImageBitmap(bmp)
        }.addOnFailureListener {

        }
    }

    private fun seeFriends(pet : Pet) {
        tv_see_pets_friends_profile_pet.setOnClickListener {
            val intent = Intent(this, PetFriendsService::class.java)
            intent.putExtra("PET_INFO_PROFILE", pet)
            ContextCompat.startActivity(this, intent, Bundle())
        }
    }
}