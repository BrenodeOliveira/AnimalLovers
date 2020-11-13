package br.com.breno.animallovers.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.PetUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val petUtils = PetUtils()
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

                idPet = petUtils.idFirstPet(dataSnapshot)
                println(idPet.toString() + "PROFILE")
                if (idPet > 0) {
                    petInfo = petUtils.retrievePetInfo(idPet, dataSnapshot)
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
}