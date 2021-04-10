package br.com.breno.animallovers.ui.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.adapters.FeedAdapter
import br.com.breno.animallovers.adapters.LatestMessageRow
import br.com.breno.animallovers.constants.KindOfPet
import br.com.breno.animallovers.constants.StatusSolicitacaoAmizade
import br.com.breno.animallovers.model.*
import br.com.breno.animallovers.service.DonoService
import br.com.breno.animallovers.service.PetFriendsService
import br.com.breno.animallovers.service.PetService
import br.com.breno.animallovers.service.PostService
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile_pet.*

class ProfilePetActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
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
        var listPosts : List<Post>


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