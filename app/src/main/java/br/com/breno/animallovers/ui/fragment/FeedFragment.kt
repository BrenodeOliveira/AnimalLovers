package br.com.breno.animallovers.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.service.FeedAdapter
import br.com.breno.animallovers.ui.activity.ProfileActivity
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.DateUtils.Companion.convertStringToDate
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_feed.*


class FeedFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var db: DatabaseReference
    private lateinit var dBase: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var pet = Pet()


//    private val estadoAppViewModel: EstadoAppViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateRecyclerFeed()
//        estadoAppViewModel.temComponentes = ComponentesVisuais()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //verificar como destruir o fragment e nao duplicar o RV
    }

    fun populateRecyclerFeed() {
        val myPreferences = ProjectPreferences(requireContext())
        if(myPreferences.getPetLogged() == "") {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }
        else {
            database = Firebase.database.reference
            dBase = Firebase.database.reference
            db = Firebase.database.reference
            auth = FirebaseAuth.getInstance()
            var numChildren = 0L

            dBase.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(sShot: DataSnapshot) {
                    val listPosts = ArrayList<Post>()
                    if (sShot.value != null) {
                        pet = sShot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>()!!
                        pet.id = myPreferences.getPetLogged().toString()
                        numChildren = sShot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).childrenCount

                        if (sShot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).hasChild(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome)) {

                            val owners: HashMap<String, HashMap<String, HashMap<String, String>>> = sShot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.DATABASE_NODE_FRIENDS.nome).value as HashMap<String, HashMap<String, HashMap<String, String>>>

                            for (z in 0 until owners.size) {
                                val idOwnerUser = owners.keys.toList()[z]
                                for (element in owners.getValue(idOwnerUser).keys.toList()) {

                                    pet = sShot.child(idOwnerUser).child(element).child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>()!!
                                    pet.id = element
                                    val totalPostsPet = sShot.child(idOwnerUser).child(element).child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).childrenCount
                                    var maxPosts = 15
                                    for (j in totalPostsPet downTo 1) {
                                        if (maxPosts != 0) {
                                            if(sShot.child(idOwnerUser).child(element).child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).hasChild(j.toString())) {
                                                val post: Post = sShot.child(idOwnerUser).child(element).child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).child(j.toString()).getValue<Post>()!!

                                                if(post.postAtivo) {
                                                    maxPosts--

                                                    post.idPet = element
                                                    post.nomePet = pet.nome
                                                    post.idOwner = idOwnerUser
                                                    listPosts.add(post)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            for (x in 0..numChildren) {
                                if (sShot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).hasChild(x.toString())) {
                                    val post: Post = sShot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).child(x.toString()).getValue<Post>()!!

                                    if(post.postAtivo) {
                                        pet = sShot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.DATABASE_NODE_PET_ATTR.nome).getValue<Pet>()!!

                                        post.idPet = myPreferences.getPetLogged().toString()
                                        post.nomePet = pet.nome
                                        post.idOwner = auth.uid.toString()
                                        listPosts.add(post)
                                    }

                                }
                            }

                            listPosts.sortBy { convertStringToDate(it.dataHora) }
                            recycler_feed.layoutManager = LinearLayoutManager(requireContext())
                            recycler_feed.adapter = FeedAdapter(listPosts, pet, requireContext())

                            val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                            recycler_feed.layoutManager = layoutManager

                        } else {
                            for (x in 0..numChildren) {
                                if (sShot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).hasChild(x.toString())) {
                                    val post: Post = sShot.child(auth.uid.toString()).child(myPreferences.getPetLogged().toString()).child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).child(x.toString()).getValue<Post>()!!

                                    listPosts.add(post)

                                    recycler_feed.layoutManager = LinearLayoutManager(requireContext())
                                    recycler_feed.adapter = FeedAdapter(listPosts, pet, requireContext())

                                    val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                                    recycler_feed.layoutManager = layoutManager
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }

}