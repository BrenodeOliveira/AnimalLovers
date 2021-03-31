package br.com.breno.animallovers.service

import android.content.Context
import br.com.breno.animallovers.model.Comentario
import br.com.breno.animallovers.model.LikeComment
import br.com.breno.animallovers.model.LikePost
import br.com.breno.animallovers.model.Notification
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class LikeService (context : Context){
    val myPreferences = ProjectPreferences(context)
    val database = Firebase.database.reference
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var commentsService = CommentsService(context)

    fun dislikePost(post : Post, likePost : LikePost, notification : Notification) {
        likePost.ativo = false
        notification.ativo = false

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(likePost)

        //Esconde a notificação de like no post
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .child(notification.incrementIdNotification)
            .setValue(notification)
    }

    fun likePost(post: Post, likePost: LikePost) : LikePost {

        if(likePost.uniqueId.isEmpty()) {
            var ref = database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(post.idOwner)
                .child(post.idPet)
                .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                .child(post.idPost)
                .child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)
                .push()

            likePost.dataHora = DateUtils.dataFormatWithMilliseconds()
            likePost.uniqueId = ref.key!!
        }
        else {
            likePost.ativo = true
        }
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(likePost)

        return likePost
    }

    fun dislikeComment(post : Post, comentario : Comentario, likeComment: LikeComment, notification: Notification) {
        likeComment.ativo = false
        notification.ativo = false

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)//ID do dono do post
            .child(post.idPet)//ID do pet do post
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
            .child(comentario.idComentario)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(likeComment)

        //Esconde a notificação de like no comentário
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(comentario.idOwner)
            .child(comentario.idPet)
            .child(AnimalLoversConstants.DATABASE_NODE_NOTIFICATIONS.nome)
            .child(notification.incrementIdNotification)
            .setValue(notification)
    }

    fun likeComment(post : Post, comentario : Comentario, likeComment: LikeComment) : LikeComment{
        if(likeComment.uniqueId.isEmpty()) {
            var ref = database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                .child(post.idOwner)//ID do dono do post
                .child(post.idPet)//ID do pet do post
                .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                .child(post.idPost)
                .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
                .child(comentario.idComentario)
                .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)
                .push()

            likeComment.dataHora = DateUtils.dataFormatWithMilliseconds()
            likeComment.uniqueId = ref.key!!
        }
        else {
            likeComment.ativo = true
        }
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)//ID do dono do post
            .child(post.idPet)//ID do pet do post
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)
            .child(comentario.idComentario)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(likeComment)

        return likeComment
    }

    fun checkIfUserLikedComment(snapshot: DataSnapshot, post : Post, comentario: Comentario) : LikeComment {
        var likeComment = LikeComment()

        val dataSnapshot = commentsService.getRootSnapshotForComment(post, comentario, snapshot)

        if(dataSnapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome)) {
            if(dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome).hasChild(auth.uid.toString())) {
                if(dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome).child(auth.uid.toString()).hasChild(myPreferences.getPetLogged().toString())) {
                    likeComment = dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT_LIKES.nome).child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).getValue<LikeComment>()!!
                }
            }
        }
        return likeComment
    }

    fun checkIfUserLikedPost(snapshot: DataSnapshot) : LikePost {
        var likePost = LikePost()

        val dataSnapshot = snapshot

        if(dataSnapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)) {
            if(dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome).hasChild(auth.uid.toString())) {
                if(dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome).child(auth.uid.toString()).hasChild(myPreferences.getPetLogged().toString())) {
                    likePost = dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome).child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).getValue<LikePost>()!!
                }
            }
        }
        return likePost
    }
}