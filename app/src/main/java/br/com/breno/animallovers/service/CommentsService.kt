package br.com.breno.animallovers.service

import br.com.breno.animallovers.model.Comentario
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class CommentsService {
    val database = Firebase.database.reference

    fun persistNewComment() {

    }

    fun retrieveComment(indexComment : Long, dSnapshot : DataSnapshot, idOwner : String, idPet : String) : Comentario {
        return dSnapshot.child(indexComment.toString())
            .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
            .child(idOwner)
            .child(idPet)
            .getValue<Comentario>()!!
    }


}