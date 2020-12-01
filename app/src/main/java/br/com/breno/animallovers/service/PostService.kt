package br.com.breno.animallovers.service

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class PostService : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var pet = Pet()

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
        val database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(id)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).addListenerForSingleValueEvent(object :
            ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var idNewPost : Long = snapshot.childrenCount + 1

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
}