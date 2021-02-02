package br.com.breno.animallovers.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.service.PetSearchAdapter
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_pesquisar.*


class PesquisarFragment:Fragment() {

    private lateinit var dBase: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var pet = Pet()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pesquisar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_search_pets.setOnClickListener{
            buscarPets()
        }
        et_name_pet_search.editText!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                buscarPets()
            }
        })
    }

    private fun buscarPets() {
        dBase = Firebase.database.reference
        auth = FirebaseAuth.getInstance()
        val listPets = ArrayList<Pet>()
        val petNameSearch = et_name_pet_search.editText?.text

        if(petNameSearch.isNullOrEmpty()) {
            dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val contas = snapshot.children
                    contas.forEachIndexed { index, dataSnapshot ->
                        val numChildrenConta = dataSnapshot.childrenCount
                        for (i in 1 until numChildrenConta) {
                            if (dataSnapshot.hasChild("pet$i")) {
                                pet = dataSnapshot.child("pet$i")
                                    .child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome)
                                    .getValue<Pet>()!!
                                pet.id = "pet$i"
                                pet.idOwner = dataSnapshot.key.toString()
                                listPets.add(pet)
                            }
                        }
                    }

                    recycler_search_pets.layoutManager = LinearLayoutManager(requireContext())
                    recycler_search_pets.adapter = PetSearchAdapter(listPets, requireContext())

                    val layoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
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
                        val contas = snapshot.children
                        contas.forEachIndexed { index, dataSnapshot ->
                            val numChildrenConta = dataSnapshot.childrenCount
                            for (i in 1 until numChildrenConta) {
                                if (dataSnapshot.hasChild("pet$i")) {
                                    pet = dataSnapshot.child("pet$i")
                                        .child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome)
                                        .getValue<Pet>()!!
                                    if(pet.nome.toUpperCase().contains(petNameSearch.toString().toUpperCase())) {
                                        pet.id = "pet$i"
                                        pet.idOwner = dataSnapshot.key.toString()
                                        listPets.add(pet)
                                    }
                                }
                            }
                        }
                        recycler_search_pets.layoutManager = LinearLayoutManager(requireContext())
                        recycler_search_pets.adapter = PetSearchAdapter(listPets, requireContext())

                        val layoutManager =
                            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                        recycler_search_pets.layoutManager = layoutManager
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println(error.toString())
                    }

                })
        }
    }
}