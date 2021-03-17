package br.com.breno.animallovers.service

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.SolicitacaoAmizade
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class PetService : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var pet = Pet()
    @RequiresApi(Build.VERSION_CODES.O)
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DateUtils.dateFrmt())

    fun idLastPet(dataSnapshot: DataSnapshot): Int {
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
        pet = dataSnapshot.child(id).child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>()!!
        return pet
    }

    fun retrievePets (ownersPets: HashMap<String, HashMap<String, String>>, dataSnapshot: DataSnapshot): ArrayList<Pet> {
        val petsArray = ArrayList<Pet>()

        for (i in 0 until ownersPets.size) {
            val idOwnerPet = ownersPets.keys.toMutableList()[i]
            for (j in 0 until (ownersPets.toMutableMap()[idOwnerPet]?.values?.size!!)) {
                pet = dataSnapshot.child(idOwnerPet)
                    .child(ownersPets.toMutableMap()[idOwnerPet]?.keys?.toMutableList()!![j])
                    .child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome)
                    .getValue<Pet>()!!

                petsArray.add(pet)
            }
        }
        return petsArray
    }

    fun retrievePetInfoFromUnknownOwner (idOwner : String, idPet: String, dataSnapshot: DataSnapshot): Pet {
        pet = dataSnapshot.child(idOwner).child(idPet).child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>()!!
        return pet
    }

    fun registerNewPet(id: Int, pet: Pet) {
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(AnimalLoversConstants.DATABASE_NODE_PET.nome + id)
            .child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome)
            .setValue(pet)
    }

    fun updatePetInfo(pet: Pet) {
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(auth.uid.toString())
            .child(pet.id)
            .child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome)
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
        }.addOnSuccessListener {
            pet.pathFotoPerfil = storageRef.toString()

            registerNewPet(id, pet)
        }
    }

    fun uploadOrUpdateProfilePhotoPet(dataPicture : ByteArray, pet : Pet) {
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        //Referência de caminho às pastas filhas (Ex.: images/posts/{id do user}/{id do pet do user}/{foto.jpeg}
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
            .child(auth.uid.toString())
            .child(pet.id + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

        //Faz o upload no caminho determinado
        var uploadTask = storageRef.putBytes(dataPicture)

        uploadTask.addOnFailureListener {
            //Printa a stack em caso de erro, e não fará o novo post
            println(uploadTask.exception.toString())
        }.addOnSuccessListener {
            pet.pathFotoPerfil = storageRef.toString()

            updatePetInfo(pet)
        }
    }

    fun numberOfFriendsPet(snapshot: DataSnapshot, petInfo : Pet) : Int {
        var numFriends = 0
        if (snapshot.child(petInfo.id).hasChild(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome) && snapshot.child(petInfo.id).child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).hasChildren()) {

            val amgsSearchPet: HashMap<String, HashMap<String, HashMap<String, String>>> = snapshot.child(petInfo.id)
                .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).value as HashMap<String, HashMap<String, HashMap<String, String>>>

                for (j in 0 until amgsSearchPet.size) {
                    val idOwnerAmgSearchPet = amgsSearchPet.keys.toList()[j]
                    for (l in 0 until (amgsSearchPet[idOwnerAmgSearchPet]?.size ?: 0)) {
                        numFriends++
                    }
                }
        }
        return numFriends
    }

    fun getFriendsOfPet(snapshot: DataSnapshot, petProfile : Pet) : List<Pet> {
        val listPetsFriends = ArrayList<Pet>()
        auth = FirebaseAuth.getInstance()

        if (snapshot.child(petProfile.idOwner).child(petProfile.id).hasChild(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome) && snapshot.child(petProfile.idOwner).child(petProfile.id).child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).hasChildren()) {
            val amgsSearchPet: HashMap<String, HashMap<String, HashMap<String, String>>> = snapshot.child(petProfile.idOwner)
                .child(petProfile.id)
                .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).value as HashMap<String, HashMap<String, HashMap<String, String>>>

            for (j in 0 until amgsSearchPet.size) {
                val idOwnerAmgSearchPet = amgsSearchPet.keys.toList()[j]
                for (l in 0 until (amgsSearchPet[idOwnerAmgSearchPet]?.size ?: 0)) {
                    val idPetFriend = amgsSearchPet[idOwnerAmgSearchPet]!!.keys.toList()[l]

                    val petFriend = snapshot.child(idOwnerAmgSearchPet)
                        .child(idPetFriend)
                        .child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome)
                        .getValue<Pet>()!!

                    listPetsFriends.add(petFriend)
                }

            }
        }

            return listPetsFriends
    }

    fun statusFriendShip (pet: Pet, snapshot: DataSnapshot, context: Context) : SolicitacaoAmizade? {
        auth = FirebaseAuth.getInstance()
        val myPreferences = ProjectPreferences(context)
        var solicitacaoAmizade: SolicitacaoAmizade

        if (snapshot.child(pet.id).hasChild(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)) {
            if (snapshot.child(pet.id).child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).hasChild(auth.uid.toString())) {
                if (snapshot.child(pet.id).child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome).child(auth.uid.toString()).hasChild(myPreferences.getPetLogged().toString())) {
                    solicitacaoAmizade = snapshot.child(pet.id)
                        .child(AnimalLoversConstants.DATABASE_NODE_PET_FRIENDS_REQUEST.nome)
                        .child(auth.uid.toString())
                        .child(myPreferences.getPetLogged().toString())
                        .getValue<SolicitacaoAmizade>()!!

                    return solicitacaoAmizade
                }
            }
        }

        return null
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun dateDiffInTextFormat(start: LocalDateTime) : String {
        var resultToReturn: String
        val end: LocalDateTime = LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter)

        when {
            ChronoUnit.DAYS.between(start, end) > 31 -> {
                resultToReturn = if (ChronoUnit.MONTHS.between(start, end) > 12) {
                    if(ChronoUnit.YEARS.between(start, end) > 1) {
                        ChronoUnit.YEARS.between(start, end).toString() + " anos"
                    } else {
                        ChronoUnit.YEARS.between(start, end).toString() + " ano"
                    }
                } else if(ChronoUnit.MONTHS.between(start, end) <= 1){
                    ChronoUnit.MONTHS.between(start, end).toString() + " mês"
                } else {
                    ChronoUnit.MONTHS.between(start, end).toString() + " meses"
                }
            }
            ChronoUnit.DAYS.between(start, end) < 1 -> {
                resultToReturn = when {
                    ChronoUnit.HOURS.between(start, end) > 1 -> {
                        ChronoUnit.HOURS.between(start, end).toString() + " horas"
                    }
                    ChronoUnit.HOURS.between(start, end).toInt() == 1 -> {
                        ChronoUnit.HOURS.between(start, end).toString() + " hora"
                    }
                    else -> {
                        ChronoUnit.MINUTES.between(start, end).toString() + " minutos"
                    }
                }
            }
            else -> {
                resultToReturn = ChronoUnit.DAYS.between(start, end).toString() + " dias"
            }
        }
        return resultToReturn
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun friendlyTextFriendshipStatusSince(pet : Pet, dataSnapshot: DataSnapshot, context: Context) : String {
        auth = FirebaseAuth.getInstance()
        val myPreferences = ProjectPreferences(context)

        val dateBeginningFriendship : String = dataSnapshot.child(pet.id)
            .child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome)
            .child(auth.uid.toString())
            .child(myPreferences.getPetLogged().toString()).value.toString()

        val start: LocalDateTime = LocalDateTime.parse(dateBeginningFriendship, formatter)


        return dateDiffInTextFormat(start)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun friendlyTextFriendshipStatusSince(statusSolicitacaoAmizade: SolicitacaoAmizade) : String {
        val start: LocalDateTime = LocalDateTime.parse(statusSolicitacaoAmizade.dataEnvioSolicitacao, formatter)

        return dateDiffInTextFormat(start)
    }
}