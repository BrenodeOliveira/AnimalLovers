package br.com.breno.animallovers.service

import android.content.Context
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.SolicitacaoAmizade
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FriendShipService(context : Context) {
    val myPreferences = ProjectPreferences(context)
    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun undoFriendShipRequestInSender( pet : Pet) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome)
            .child(pet.idOwner)
            .child(pet.id)
            .setValue(null)
    }

    fun undoFriendShipRequestInReceiver( pet : Pet) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(pet.idOwner)
            .child(pet.id)
            .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(null)
    }

    fun persistFriendShipRequestInSender(solicitacao : SolicitacaoAmizade, pet : Pet) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(pet.idOwner)
            .child(pet.id)
            .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(solicitacao)
    }

    fun persistFriendShipRequestInReceiver(solicitacao : SolicitacaoAmizade, pet : Pet) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
            .child(pet.idOwner)
            .child(pet.id)
            .setValue(solicitacao)
    }

    fun saveNewFriendShipSender(dataInicioAmizade : String, pet : Pet) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(pet.idOwner)
            .child(pet.id)
            .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(dataInicioAmizade)
    }

    fun saveNewFriendShipReceiver(dataInicioAmizade : String, pet : Pet) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome)
            .child(pet.idOwner)
            .child(pet.id)
            .setValue(dataInicioAmizade)
    }

    fun undoFriendshipInSender(pet : Pet) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
            .child(pet.idOwner)
            .child(pet.id)
            .setValue(null)
    }

    fun undoFriendshipInReceiver(pet : Pet) {
        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(pet.idOwner)
            .child(pet.id)
            .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString())
            .setValue(null)
    }
}