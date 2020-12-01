package br.com.breno.animallovers.service

import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class PetService : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var pet = Pet()

    fun idFirstPet(dataSnapshot: DataSnapshot): Int {
        var iteratorPet = 0
        val numChildren = dataSnapshot.childrenCount
        for (x in 0..numChildren) {
            if (dataSnapshot.hasChild("pet$x")) {
                iteratorPet = x.toInt()
            }
        }
        return iteratorPet
    }

    fun retrievePetInfo (id: String, dataSnapshot: DataSnapshot): Pet {
        pet = dataSnapshot.child(id).getValue<Pet>()!!
        return pet
    }

    fun registerNewPet(id: Int, pet: Pet) {
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(AnimalLoversConstants.DATABASE_NODE_PET.nome + id)
            .setValue(pet)
    }

    fun uploadProfilePhotoPet(id : Int, dataPicture : ByteArray, pet : Pet) {
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        //Referência de caminho às pastas filhas (Ex.: images/posts/{id do user}/{id do pet do user}/{foto.jpeg}
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
            .child(auth.uid.toString())
            .child(AnimalLoversConstants.DATABASE_NODE_PET.nome + id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

        //Faz o upload no caminho determinado
        var uploadTask = storageRef.putBytes(dataPicture)

        uploadTask.addOnFailureListener {
            //Printa a stack em caso de erro, e não fará o novo post
            println(uploadTask.exception.toString())
        }.addOnSuccessListener { taskSnapshot ->
            pet.pathFotoPerfil = storageRef.toString()

            registerNewPet(id, pet)
        }
    }
}