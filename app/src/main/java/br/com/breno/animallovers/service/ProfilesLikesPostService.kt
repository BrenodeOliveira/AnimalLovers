package br.com.breno.animallovers.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.adapters.ProfilesLikesPostAdapter
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
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

class ProfilesLikesPostService(private val post : Post) : BottomSheetDialogFragment() {

    private lateinit var database: DatabaseReference
    private lateinit var db: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var pet = Pet()

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var v = inflater.inflate(R.layout.modal_likes_pets_post, container, false)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var arraylist = ArrayList<Pet>()

        database = Firebase.database.reference
        db = Firebase.database.reference
        auth = FirebaseAuth.getInstance()

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
            .child(post.idOwner)
            .child(post.idPet)
            .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
            .child(post.idPost)
            .child(AnimalLoversConstants.DATABASE_NODE_POST_LIKE.nome)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val ownersPets: HashMap<String, HashMap<String, String>> =
                        snapshot.value as HashMap<String, HashMap<String, String>>
                    db.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {


                                for (i in 0 until ownersPets.size) {
                                    val idOwnerPet = ownersPets.keys.toMutableList()[i]
                                    for (j in 0 until (ownersPets.toMutableMap()[idOwnerPet]?.values?.size!!)) {
                                        pet = snapshot.child(idOwnerPet)
                                            .child(ownersPets.toMutableMap()[idOwnerPet]?.keys?.toMutableList()!![j])
                                            .child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome)
                                            .getValue<Pet>()!!

                                        arraylist.add(pet)
                                    }
                                }

                                val recyclerView = view.findViewById(R.id.recycler_pets_likes_post) as? RecyclerView
                                recyclerView?.layoutParams!!.height = 550

                                recyclerView!!.layoutManager = LinearLayoutManager(activity)
                                recyclerView.adapter = ProfilesLikesPostAdapter(arraylist, context!!)

                                val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                                recyclerView.layoutManager = layoutManager
                            }

                            override fun onCancelled(error: DatabaseError) {
                                error.toString()
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }
}