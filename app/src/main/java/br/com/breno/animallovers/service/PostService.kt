package br.com.breno.animallovers.service

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import br.com.breno.animallovers.model.Comentario
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.model.ReportPost
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

class PostService(context : Context) {
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var pet = Pet()
    val database = Firebase.database.reference
    val commentsService = CommentsService(context)
    val myPreferences = ProjectPreferences(context)

    @RequiresApi(Build.VERSION_CODES.O)
    fun persistNewPetPost(id : String, dataPicture : ByteArray, post : Post) {
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        //Pega a data/hora do post com millisegundos, define como nome da imagem, para ser única
        val dateTimePost = DateUtils.dataFormatWithMilliseconds()

        //Referência de caminho às pastas filhas (Ex.: images/posts/{id do user}/{id do pet do user}/{foto.jpeg}
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(auth.uid.toString())
            .child(id)
            .child(dateTimePost)

        //Faz o upload no caminho determinado
        var uploadTask = storageRef.putBytes(dataPicture)

        uploadTask.addOnFailureListener {
            //Printa a stack em caso de erro, e não fará o novo post
            println(uploadTask.exception.toString())
        }.addOnSuccessListener { taskSnapshot ->
            post.dataHora = dateTimePost
            post.pathPub = storageRef.toString()

            registerNewPost(id, post)
        }
    }

    fun registerNewPost(id : String, post : Post) {
        auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(id)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).addListenerForSingleValueEvent(object :
            ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var idNewPost : Long = snapshot.childrenCount + 1

                    post.idPost = idNewPost.toString()
                    post.postAtivo = true
                    database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                        .child(auth.uid.toString())
                        .child(id)
                        .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                        .child(idNewPost.toString()).setValue(post)
                    println("Sucesso em realizar novo post")
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    fun getAllPostsPet(pet: Pet, snapshot: DataSnapshot) : List<Post> {
        val listPosts = ArrayList<Post>()
        var numChildren = 0L

        numChildren = snapshot.child(pet.id)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).childrenCount

        for (x in 0..numChildren) {
            if (snapshot.child(pet.id)
                    .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                    .hasChild(x.toString())) {
                val post: Post = snapshot.child(pet.id).child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                    .child(x.toString()).getValue<Post>()!!

                listPosts.add(post)

            }
        }

        return listPosts
    }

    fun updatePost(post : Post) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .setValue(post)
    }

    fun getCommentsOfPost(snapshot: DataSnapshot, post : Post) : ArrayList<Comentario> {

        var arraylist = ArrayList<Comentario>()
        var comentario: Comentario

        var dSnapshot = snapshot.child(AnimalLoversConstants.DATABASE_NODE_POST_COMMENT.nome)

        for (i in 1 until dSnapshot.childrenCount + 1) {
            if (dSnapshot.hasChild(i.toString())) {
                var rootNodeComment = dSnapshot.child(i.toString())
                    .child(AnimalLoversConstants.DATABASE_NODE_COMMENT.nome).value as HashMap<String, HashMap<String, HashMap<String, String>>>
                val idOnwer = rootNodeComment.keys.toMutableList()[0]
                val idPet = rootNodeComment[idOnwer]?.keys?.toMutableList()?.get(0).toString()

                comentario = commentsService.retrieveComment(i, dSnapshot, idOnwer, idPet)
                if (comentario.comentarioAtivo) {
                    arraylist.add(comentario)
                }
            }
        }

        return arraylist
    }

    fun reportPost(snapshot: DataSnapshot, reportPost: ReportPost) {
        if(snapshot.hasChildren() && snapshot.hasChild(AnimalLoversConstants.DATABASE_NODE_REPORT_POST.nome)) {
            val numReporttedPosts = snapshot.child(AnimalLoversConstants.DATABASE_NODE_REPORT_POST.nome).childrenCount + 1

            database.child(AnimalLoversConstants.DATABASE_ENTITY_ADMIN.nome)
                .child(AnimalLoversConstants.DATABASE_NODE_REPORT_POST.nome)
                .child(numReporttedPosts.toString())
                .setValue(reportPost)
        }
        else {
            database.child(AnimalLoversConstants.DATABASE_ENTITY_ADMIN.nome)
                .child(AnimalLoversConstants.DATABASE_NODE_REPORT_POST.nome)
                .child("1")
                .setValue(reportPost)
        }
    }

}