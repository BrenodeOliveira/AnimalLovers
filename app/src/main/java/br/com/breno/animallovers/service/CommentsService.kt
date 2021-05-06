package br.com.breno.animallovers.service

import android.content.Context
import android.util.Log
import br.com.breno.animallovers.model.*
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
    private var cmt = Comentario()

    fun persistNewComment() {

    }

    fun getCommentByUniqueIdOfCommentInPost(snapshot: DataSnapshot, uniquePostId : String, petRemetente : Pet) : Comentario {
        var comentario = Comentario()

        var dataSnapshot = snapshot.child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)

        var numOfPosts = dataSnapshot.childrenCount

        for (i in 1 until numOfPosts + 1) {
            if(dataSnapshot.child(i.toString()).hasChild(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)) {
                var numOfComments = dataSnapshot.child(i.toString()).child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome).childrenCount

                for(j in 1 until numOfComments + 1) {
                    var nodeComment = dataSnapshot.child(i.toString())
                        .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
                        .child(j.toString())
                        .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
                    if(nodeComment.hasChild(petRemetente.idOwner)) {
                        var commentPost = nodeComment.child(petRemetente.idOwner).child(petRemetente.id).getValue<Comentario>()
                        if(commentPost == null) {
                            Log.i("CommentsService:getCommentByUniqueIdOfCommentInPost", "commentPost veio nulo")
                        }
                        Log.i("CommentsService:getCommentByUniqueIdOfCommentInPost", "Comentario: $commentPost \n id action: $uniquePostId")
                        if(commentPost!!.uniqueIdComment == uniquePostId && commentPost.comentarioAtivo) {
                            return commentPost
                        }
                    }
                }
            }
        }
        return comentario
    }

    fun retrieveComment(indexComment : Long, dSnapshot : DataSnapshot, idOwner : String, idPet : String) : Comentario {
        return dSnapshot.child(indexComment.toString())
            .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
            .child(idOwner)
            .child(idPet)
            .getValue<Comentario>()!!
    }

    fun getRootSnapshotForComment(post : Post, comentario : Comentario, dSnapshot: DataSnapshot) : DataSnapshot {
        return dSnapshot.child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
            .child(comentario.idComentario)
    }

    fun getRootSnapshotForAllCommentsOfPost(post : Post, dSnapshot: DataSnapshot) : DataSnapshot {
        return dSnapshot.child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
    }

    fun getAllComments(post : Post, dSnapshot: DataSnapshot) : ArrayList<Comentario> {
        val dataSnapshot = getRootSnapshotForAllCommentsOfPost(post, dSnapshot)

        var arraylist = ArrayList<Comentario>()
        arraylist.clear()

        for (i in 1 until dataSnapshot.childrenCount + 1) {
            if (dataSnapshot.hasChild(i.toString())) {
                var rootNodeComment = dataSnapshot.child(i.toString()).child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome).value as HashMap<String, HashMap<String, HashMap<String, String>>>
                val idOnwer = rootNodeComment.keys.toMutableList()[0]
                val idPet = rootNodeComment[idOnwer]?.keys?.toMutableList()?.get(0)

                cmt = (dataSnapshot.child(i.toString())
                    .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
                    .child(idOnwer)
                    .child(idPet.toString())
                    .getValue<Comentario>())!!
                if (cmt.comentarioAtivo) {
                    arraylist.add(cmt)
                }
            }
        }
        return arraylist
    }

    fun saveNewComment(post : Post, comentario : Comentario, dataSnapshot: DataSnapshot) : Comentario {
        comentario.idComentario = (getRootSnapshotForAllCommentsOfPost(post, dataSnapshot).childrenCount + 1).toString()

        var ref = database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
            .push()

        comentario.uniqueIdComment = ref.key!!

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
            .child(comentario.idComentario)
            .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(comentario)

        return comentario
    }

    fun updateOrDeleteComment(post : Post, comentario : Comentario) {
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
                    val likeComment = snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome).child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).getValue<LikeComment>()

                    if(likeComment!!.ativo) {
                        return true
                    }
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
                    val idPet = ownersPets.toMutableMap()[idOwnerPet]?.keys?.toMutableList()?.get(j)

                    val likeComment = snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome).child(idOwnerPet).child(idPet!!).getValue<LikeComment>()
                    if(likeComment!!.ativo) {
                        numLikes ++
                    }
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
