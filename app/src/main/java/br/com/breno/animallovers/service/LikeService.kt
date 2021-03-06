package br.com.breno.animallovers.service

import android.content.Context
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LikeService (context : Context){
    val myPreferences = ProjectPreferences(context)
    val database = Firebase.database.reference
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun dislikePost(post :Post) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(null)
    }

    fun likePost(post: Post) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(DateUtils.dataFormatWithMilliseconds())
    }

    fun checkIfPetLikkedPost(snapshot: DataSnapshot) {

    }
}