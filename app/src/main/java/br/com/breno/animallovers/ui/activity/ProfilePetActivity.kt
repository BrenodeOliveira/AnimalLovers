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
import br.com.breno.animallovers.constants.StatusSolicitacaoAmizade
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.service.FeedAdapter
import br.com.breno.animallovers.service.PetFriendsService
import br.com.breno.animallovers.service.PetService
import br.com.breno.animallovers.service.PostService
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile_pet.*

class ProfilePetActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
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
    }

    private fun refreshPetInfo(pet : Pet) {
        swipe_refresh_pet_profile.setOnRefreshListener {
            loadPetPosts(pet)

            seeFriends(pet)
        }
    }

    private fun loadPetInfo(pet: Pet) {
        val petService = PetService()

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

                    tv_pet_name_profile_pet.text = pet.nome
                    tv_pet_description_profile_pet.text = pet.resumo

                    tv_friends_profile_pet.text = " Amigos"
                    tv_num_friends_profile_pet.text = numFriends.toString()

                    tv_pet_name_owner_profile_pet.text = dono.usuario
                    tv_pet_owner_user_profile_pet.text = dono.email

                    if (statusFriendShip != null) {
                        when (statusFriendShip.statusSolicitacao) {
                            StatusSolicitacaoAmizade.CANCELLED.status -> tv_status_friendship_profile_pet.text = "Enviar solicitação"

                            StatusSolicitacaoAmizade.ACCEPTED.status -> {
                                iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_check)
                                tv_status_friendship_profile_pet.text = "Amigos há " + petService.friendlyTextFriendshipStatusSince(pet, snapshot, applicationContext)
                            }

                            StatusSolicitacaoAmizade.WAITING.status -> {
                                iv_add_friend_search_pets.setImageResource(R.drawable.ic_coracao_espera)
                                tv_status_friendship_profile_pet.text = "Solicitação enviada há " + petService.friendlyTextFriendshipStatusSince(statusFriendShip)
                            }

                            StatusSolicitacaoAmizade.SENT.status -> {
                                iv_add_friend_search_pets.setImageResource(R.drawable.ic_relogio_waiting_friend_request)
                                tv_status_friendship_profile_pet.text = "Aguardando sua análise"
                            }
                        }
                    } else {
                        tv_status_friendship_profile_pet.text = "Enviar solicitação"
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
                    recyclerView.adapter = FeedAdapter(listPosts, this@ProfilePetActivity)

                    val layoutManager = StaggeredGridLayoutManager(
                        1, StaggeredGridLayoutManager.VERTICAL
                    )
                    recyclerView.layoutManager = layoutManager
                    swipe_refresh_pet_profile.isRefreshing = false
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