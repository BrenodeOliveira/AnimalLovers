package br.com.breno.animallovers.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.service.FeedAdapter
import br.com.breno.animallovers.service.FeedService
import br.com.breno.animallovers.service.ModalBottomSheet
import br.com.breno.animallovers.service.PetsProfileAdapter
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.ProjectPreferences
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_feed.*
import kotlinx.android.synthetic.main.activity_publish.*
import kotlin.system.exitProcess

class FeedActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var pet = Pet()
    private var listPosts = ArrayList<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        inicializaNavDrawer()
        inicializaBottomNav()

        clickNavBottom()
        clickNavDrawer()
        clickOpenNavDrawer()
        clickUserPage()
        populateRecyclerFeed()

    }

    fun populateRecyclerFeed() {
        val myPreferences = ProjectPreferences(this@FeedActivity)
        if(myPreferences.getPetLogged() == "") {
            startActivity(Intent(this, ProfileActivity::class.java))
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

                                val recyclerView = findViewById<RecyclerView>(R.id.recycler_feed)
                                recyclerView.layoutManager = LinearLayoutManager(this@FeedActivity)
                                recyclerView.adapter = FeedAdapter(listPosts, pet, this@FeedActivity)

                                val layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                                recyclerView.layoutManager = layoutManager
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clickNavBottom() {
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.btn_chat -> mostraToast("Chat")
                R.id.btn_foryou -> mostraToast("4 vc")
                R.id.btn_add -> {
                    startActivity(Intent(this, PublishActivity::class.java))
                }
                R.id.btn_notification -> mostraToast("Notificação")
                R.id.btn_search -> mostraToast("Procurar")
            }
            true
        }
    }

    private fun clickNavDrawer() {
        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.alter_data -> mostraToast("Alterar dados")
                R.id.combine_pets -> mostraToast("Combina pets")
                R.id.clinics_pets -> mostraToast("Clinicas")
                R.id.get_out -> {
                    finish()
                    exitProcess(0)
                }
            }
            true
        }
    }

    private fun inicializaBottomNav() {
        bottomNavigationView.apply {
            setOnNavigationItemSelectedListener (BottomNavigationView.OnNavigationItemSelectedListener { true })
            selectedItemId = R.id.btn_foryou
        }
    }

    private fun clickUserPage() {
        iv_user.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun clickOpenNavDrawer() {
        iv_dehaze.setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    private fun inicializaNavDrawer() {
        toggle = ActionBarDrawerToggle(this, drawer_layout, R.string.open, R.string.close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }
}