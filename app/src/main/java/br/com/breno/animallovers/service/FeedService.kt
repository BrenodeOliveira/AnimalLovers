package br.com.breno.animallovers.service

import android.content.Context
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_publish.*

class FeedService {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var pet = Pet()
    private var listPosts = ArrayList<Post>()

    fun openRecyclerViewFeedPopulatedOfLoggedPet(context1 : Context, petId : String) {

        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()
        var numChildren = 0L

        database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome).child(
            auth.uid.toString()).child(petId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pet = snapshot.getValue<Pet>()!!

                numChildren = snapshot
                    .child(AnimalLoversConstants.CONST_ROOT_POSTS.nome).childrenCount

                for (x in 0..numChildren) {
                    if (snapshot.child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                            .hasChild(x.toString())) {
                        var post: Post = snapshot.child(AnimalLoversConstants.CONST_ROOT_POSTS.nome)
                            .child(x.toString()).getValue<Post>()!!

                        listPosts.add(post)
                        var vi = View(context1)

                        val recyclerView
                                = vi.findViewById(R.id.recycler_feed) as RecyclerView
                        recyclerView.layoutManager = LinearLayoutManager(context1)
                        recyclerView.adapter = FeedAdapter(listPosts, pet, context1)

                        val layoutManager = StaggeredGridLayoutManager(1
                            , StaggeredGridLayoutManager.VERTICAL)
                        recyclerView.layoutManager = layoutManager

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println(error.toString())
            }
        })
    }


}