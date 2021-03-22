package br.com.breno.animallovers.ui.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.service.ModalBottomSheet
import br.com.breno.animallovers.service.PetService
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.ProjectPreferences
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



    private var petInfo = Pet()
    private var idPet: String = ""
    private var accountInfo = Conta()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        retrieveUserInfo()
        clickNewPet()
        clickChangeProfile()
        clickChangePetsAttrs()
    }

    private fun clickChangePetsAttrs() {
        iv_call_change_pet_attr.setOnClickListener {
            val intent = Intent(baseContext, AlterarDadosPetActivity::class.java)
            intent.putExtra("PET_INFO", petInfo)
            startActivity(intent)
            finishMe()
        }
    }

    private fun clickChangeProfile() {
        var modalBottomSheet = ModalBottomSheet(applicationContext)
        btn_trocar_perfil_pet.setOnClickListener {
            modalBottomSheet.show(supportFragmentManager, "modalMenu")
        }
    }
    private fun clickNewPet() {
        btn_cadastrar_pet.setOnClickListener {
            startActivity(Intent(this, PetRegisterActivity::class.java))
        }
    }

    private fun retrieveUserInfo() {
        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        val petService = PetService(applicationContext)

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(auth.uid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                accountInfo = dataSnapshot.child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome).getValue<Conta>()!!

                val myPreferences = ProjectPreferences(baseContext)

                idPet = myPreferences.getPetLogged().toString()
                if (idPet != "") {
                    petInfo = petService.retrievePetInfo(idPet, dataSnapshot)

                    if (petInfo.pathFotoPerfil != "") {
                        retrieveProfilePhoto(idPet)
                    }
                }
                if(accountInfo.pathFotoPerfil != "") {
                    retrieveOwnerProfilePhoto()
                }

                tv_animal_name.text = petInfo.nome
                tv_animal_sex_write.text = petInfo.sexo
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

    private fun retrieveProfilePhoto(idPet: String) {
        storage = FirebaseStorage.getInstance()
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.STORAGE_ROOT_PROFILE_PHOTOS.nome)
            .child(auth.uid.toString())
            .child(idPet + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {bytesPrm ->
            val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
            iv_animal_photo.setImageBitmap(bmp)
        }.addOnFailureListener {

        }
    }

    private fun retrieveOwnerProfilePhoto() {
        storage = FirebaseStorage.getInstance()
        var storageRef = storage.reference
            .child(AnimalLoversConstants.STORAGE_ROOT.nome)
            .child(AnimalLoversConstants.STORAGE_ROOT_OWNER_PHOTOS.nome)
            .child(auth.uid.toString())
            .child(auth.uid.toString() + AnimalLoversConstants.STORAGE_PICTURE_EXTENSION.nome)

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener {bytesPrm ->
            val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
            iv_photo_owner_contact.setImageBitmap(bmp)
        }.addOnFailureListener {

        }
    }

    fun finishMe() { finish() }
}