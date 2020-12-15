package br.com.breno.animallovers.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.service.FeedAdapter
import br.com.breno.animallovers.ui.activity.ProfileActivity
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.ProjectPreferences
import br.com.breno.animallovers.viewModel.ComponentesVisuais
import br.com.breno.animallovers.viewModel.EstadoAppViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_feed.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class FeedFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var pet = Pet()
    private var listPosts = ArrayList<Post>()

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

    fun populateRecyclerFeed() {
        val myPreferences = ProjectPreferences(requireContext())
        if(myPreferences.getPetLogged() == "") {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }
        else {
            database = Firebase.database.reference
            auth = FirebaseAuth.getInstance()
            var numChildren = 0L

            database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(
                auth.uid.toString()).child(myPreferences.getPetLogged().toString()).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value == null) {

                    }
                    else {
                        pet = snapshot.getValue<Pet>()!!
                        pet.id = myPreferences.getPetLogged().toString()
                        numChildren = snapshot.child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).childrenCount

                        for (x in 0..numChildren) {
                            if (snapshot.child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).hasChild(x.toString())) {
                                var post: Post = snapshot.child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).child(x.toString()).getValue<Post>()!!

                                listPosts.add(post)

                                recycler_feed.layoutManager = LinearLayoutManager(requireContext())
                                recycler_feed.adapter = FeedAdapter(listPosts, pet, requireContext())

                                val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                                recycler_feed.layoutManager = layoutManager
                            }
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.toString())
                }
            })
        }
    }

}