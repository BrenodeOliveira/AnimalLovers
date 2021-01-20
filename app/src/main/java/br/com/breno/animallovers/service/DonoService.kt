package br.com.breno.animallovers.service

import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class DonoService {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    fun persistOwner(conta : Conta) {
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome)
            .setValue(conta)
    }

    fun uploadProfilePhotoOwner(dataPicture : ByteArray, conta : Conta) {
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        //Referência de caminho às pastas filhas (Ex.: images/ownerPicture/{id do user}/{id do user.jpeg}
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.STORAGE_ROOT_OWNER_PHOTOS.nome)
            .child(auth.uid.toString())
            .child(auth.uid.toString() + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

        //Faz o upload no caminho determinado
        var uploadTask = storageRef.putBytes(dataPicture)

        uploadTask.addOnFailureListener {
            //Printa a stack em caso de erro, e não fará o novo post
            println(uploadTask.exception.toString())
        }.addOnSuccessListener { taskSnapshot ->
            conta.pathFotoPerfil = storageRef.toString()

            persistOwner(conta)
        }
    }

}