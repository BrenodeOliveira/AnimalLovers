package br.com.breno.animallovers.service

import android.content.Context
import br.com.breno.animallovers.model.Comentario
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.model.ReportComment
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class CommentsService(context : Context) {
    val database = Firebase.database.reference
    val myPreferences = ProjectPreferences(context)
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun persistNewComment() {

    }

    fun retrieveComment(indexComment : Long, dSnapshot : DataSnapshot, idOwner : String, idPet : String) : Comentario {
        return dSnapshot.child(indexComment.toString())
            .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
            .child(idOwner)
            .child(idPet)
            .getValue<Comentario>()!!
    }

    fun getRootSnapshotForComment(post : Post, comentario : Comentario, dSnapshot: DataSnapshot) : DataSnapshot {
        return dSnapshot.child(comentario.idOwner)
            .child(comentario.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
            .child(comentario.idComentario)
    }

    fun persistComment(post : Post, comentario : Comentario) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
            .child(comentario.idComentario)
            .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
            .child(comentario.idOwner)
            .child(comentario.idPet)
            .setValue(comentario)
    }

    fun retrieveAllCommentsOfPost(post : Post, comentario : Comentario, dSnapshot: DataSnapshot) {
        val snapshot = getRootSnapshotForComment(post, comentario, dSnapshot)
    }

    fun checkIfUserLikedComment(post : Post, comentario : Comentario, dSnapshot: DataSnapshot) : Boolean {
        val snapshot = getRootSnapshotForComment(post, comentario, dSnapshot)

        if (snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)) {
            if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome).hasChild(auth.uid.toString())) {
                if (snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome).child(auth.uid.toString()).hasChild(myPreferences.getPetLogged().toString())) {
                    return true
                }
            }
        }
        return false
    }

    fun getNumOfLikesInComment(post : Post, comentario : Comentario, dSnapshot: DataSnapshot) :Int {
        val snapshot = getRootSnapshotForComment(post, comentario, dSnapshot)

        var numLikes = 0
        if (snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)) {
            val ownersPets: HashMap<String, HashMap<String, String>> =
                snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome).value as HashMap<String, HashMap<String, String>>
            for (i in 0 until ownersPets.size) {
                val idOwnerPet = ownersPets.keys.toMutableList()[i]
                for (j in 0 until (ownersPets.toMutableMap()[idOwnerPet]?.values?.size!!)) {
                    numLikes++
                }
            }
        }
        return numLikes
    }

    fun getOwnersPetsComments(post : Post, comentario : Comentario, dSnapshot: DataSnapshot): HashMap<String, HashMap<String, String>> {
        val snapshot = getRootSnapshotForComment(post, comentario, dSnapshot)//.child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)

        var owners = HashMap<String, HashMap<String, String>>()

        if (snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)) {
            owners = snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome).value as HashMap<String, HashMap<String, String>>
        }
        return owners
    }

    fun reportComment(snapshot: DataSnapshot, reportComment: ReportComment) {
        if(snapshot.hasChildren() && snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_REPORT_COMMENT.nome)) {
            val numReporttedComments = snapshot.child(AnimalLoversConstants.DATABASE_NODE_REPORT_COMMENT.nome).childrenCount + 1

            database.child(AnimalLoversConstants.DATABASE_ENTITY_ADMIN.nome)
                .child(AnimalLoversConstants.DATABASE_NODE_REPORT_COMMENT.nome)
                .child(numReporttedComments.toString())
                .setValue(reportComment)
        }
        else {
            database.child(AnimalLoversConstants.DATABASE_ENTITY_ADMIN.nome)
                .child(AnimalLoversConstants.DATABASE_NODE_REPORT_COMMENT.nome)
                .child("1")
                .setValue(reportComment)
        }
    }

}