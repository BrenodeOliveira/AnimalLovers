package br.com.breno.animallovers.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.adapters.PetsProfileAdapter
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.ui.activity.RegisterActivity
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.modal_escolha_perfil_pet.*


class ModalBottomSheet(context: Context) : BottomSheetDialogFragment() {

    private var service = PetService(context)
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var pet = Pet()

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var v = inflater.inflate(R.layout.modal_escolha_perfil_pet, container, false)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var arraylist = ArrayList<Pet>()

        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()
        var numChildren = 0L

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(
            auth.uid.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                numChildren = snapshot.childrenCount
                for (x in 0..numChildren) {
                    if (snapshot.hasChild("pet$x")) {

                        if(snapshot.child("pet$x").hasChild(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome)) {
                            pet = snapshot.child("pet$x").child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>()!!

                            pet.id = "pet$x"
                            arraylist.add(pet)

                            val recyclerView = view.findViewById(R.id.recycler_escolha_pet) as RecyclerView
                            val context: Context = recyclerView.context
                            recyclerView.layoutManager = LinearLayoutManager(context)
                            recyclerView.adapter = PetsProfileAdapter(arraylist, context,this@ModalBottomSheet)

                            val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                            recyclerView.layoutManager = layoutManager
                        }

                    }
                }
                if(arraylist.isEmpty()) {
                    tv_modal_escolha_pet.text = "Você não possui pets cadastrados, clique e cadastre"

                    tv_modal_escolha_pet.setOnClickListener {
                        startActivity(Intent(context, RegisterActivity::class.java))
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    override fun onStop() {

        super.onStop()
    }
}

