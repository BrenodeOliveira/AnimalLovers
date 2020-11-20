package br.com.breno.animallovers.ui.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.service.PetService
import br.com.breno.animallovers.ui.activity.extensions.mostraToastyError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private val petService = PetService()
    private var petInfo = Pet()
    private var idPet: Int = 0
    private var accountInfo = Conta()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        retrieveUserInfo()
    }

    private fun retrieveUserInfo() {
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(auth.uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                accountInfo = dataSnapshot.getValue<Conta>()!!

                idPet = petService.idFirstPet(dataSnapshot)
                if (idPet > 0) {
                    petInfo = petService.retrievePetInfo(idPet, dataSnapshot)
                    retrieveProfilePhoto(idPet)
                }

                tv_animal_name.text = petInfo.nome
                tv_animal_sex_write.text = petInfo.peso
                tv_animal_age_write.text = petInfo.idade
                tv_animal_weight_write.text = petInfo.peso
                tv_summary_text.text = petInfo.resumo
                tv_name_contact_person.text = accountInfo.usuario
            }

            override fun onCancelled(error: DatabaseError) {
                Toasty.error(baseContext, "Erro ao carregar informações do perfil", Toasty.LENGTH_LONG).show()
            }
        })

    }
    private fun retrieveProfilePhoto(idPet: Int) {
        storage = FirebaseStorage.getInstance()
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
            .child(auth.uid.toString())
            .child(AnimalLoversConstants.DATABASE_NODE_PET.nome + idPet + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {bytesPrm ->
            val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
            iv_animal_photo.setImageBitmap(bmp)
        }.addOnFailureListener {
            mostraToastyError("Erro ao carregar foto de perfil")
        }
    }
}