package br.com.breno.animallovers.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import br.com.breno.animallovers.R
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_feed.*
import kotlin.system.exitProcess

class FeedActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        inicializaNavDrawer()
        inicializaBottomNav()

        clickNavBottom()
        clickNavDrawer()
        clickOpenNavDrawer()
        clickUserPage()


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