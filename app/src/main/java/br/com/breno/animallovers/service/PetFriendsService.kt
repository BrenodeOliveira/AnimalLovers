package br.com.breno.animallovers.service

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_pesquisar.*
import java.util.*

class PetFriendsService : Activity() {
    private lateinit var dBase: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var pet = Pet()
    private var petService = PetService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_pesquisar)

        pet = (intent.getSerializableExtra("PET_INFO_PROFILE") as? Pet)!!

        buscarPets(pet)

        btn_search_pets.setOnClickListener{
            buscarPets(pet)
        }
        et_name_pet_search.editText!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                buscarPets(pet)
            }
        })
    }

    private fun buscarPets(pet : Pet) {
        dBase = Firebase.database.reference
        auth = FirebaseAuth.getInstance()
        var listPets: ArrayList<Pet>
        val petNameSearch = et_name_pet_search.editText?.text

        if(petNameSearch.isNullOrEmpty()) {
            dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listPets = petService.getFriendsOfPet(snapshot, pet) as ArrayList<Pet>

                    recycler_search_pets.layoutManager = LinearLayoutManager(applicationContext)
                    recycler_search_pets.adapter = PetSearchAdapter(listPets, applicationContext)

                    val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    recycler_search_pets.layoutManager = layoutManager
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }

            })
        } else {
            dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).addListenerForSingleValueEvent(
                object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        listPets = petService.getFriendsOfPet(snapshot, pet) as ArrayList<Pet>

                        listPets.removeIf {
                            !it.nome.toUpperCase(Locale.ROOT).contains(petNameSearch.toString().toUpperCase(
                                Locale.ROOT))
                        }

                        listPets.sortBy { pet.nome }
                        recycler_search_pets.layoutManager = LinearLayoutManager(applicationContext)
                        recycler_search_pets.adapter = PetSearchAdapter(listPets, applicationContext)

                        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                        recycler_search_pets.layoutManager = layoutManager
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.toString())
                    }

                })
        }
    }

}