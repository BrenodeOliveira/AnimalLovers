package br.com.breno.animallovers.utils

import br.com.breno.animallovers.model.Pet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue

class PetUtils {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
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
        pet = dataSnapshot.child("pet$id").getValue<Pet>()!!
        return pet
    }
}