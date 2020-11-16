package br.com.breno.animallovers.service

import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue
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

    fun retrievePetInfo (id: Int, dataSnapshot: DataSnapshot): Pet {
        pet = dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_PET.nome + id).getValue<Pet>()!!
        return pet
    }
}