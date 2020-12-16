package br.com.breno.animallovers.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Pet
import br.com.breno.animallovers.model.Post
import br.com.breno.animallovers.service.FeedAdapter
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import br.com.breno.animallovers.utils.AnimalLoversConstants
import br.com.breno.animallovers.utils.ProjectPreferences
import br.com.breno.animallovers.viewModel.EstadoAppViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val controlador by lazy {
        findNavController(R.id.main_activity_nav_host)
    }
    private val viewModel: EstadoAppViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(feed_toolbar)
        inicializaNavDrawer()

        clickUserPage()

//        controlador.addOnDestinationChangedListener { controller, destination, arguments ->
//
//            title = destination.label
//
//            viewModel.componentes.observe(this, Observer {
//                it?.let { temComponentes ->
//                    if (temComponentes.appBar) {
//                        supportActionBar?.show()
//                    } else {
//                        supportActionBar?.hide()
//                    }
//                    if (temComponentes.bottomNavigation) {
//                        bottomNavigationView.visibility = VISIBLE
//                    } else {
//                        bottomNavigationView.visibility = GONE
//                    }
//                }
//            })
//        }

        bottomNavigationView.setupWithNavController(controlador)

    }

    private fun clickUserPage() {
        iv_user.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun inicializaNavDrawer() {
        val toggle = ActionBarDrawerToggle(this, drawer_layout, feed_toolbar, 0, 0)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.alter_data -> {
                startActivity(Intent(this, UserDataActivity::class.java))
            }
            R.id.combine_pets -> mostraToast("Combina pets")
            R.id.clinics_pets -> mostraToast("Clinicas")
            R.id.get_out -> {
                finish()
                exitProcess(0)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}