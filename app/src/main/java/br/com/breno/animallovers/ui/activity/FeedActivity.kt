package br.com.breno.animallovers.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import br.com.breno.animallovers.R
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import kotlinx.android.synthetic.main.activity_feed.*
import kotlinx.android.synthetic.main.activity_feed.view.*
import kotlin.system.exitProcess

class FeedActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)
        inicializaNavDrawer()

        clickNavBottom()
        clickNavDrawer()
        clickOpenNavDrawer()

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
                R.id.btn_foryou -> mostraToast("4 vc")
                R.id.btn_add -> mostraToast("Camera")
                R.id.btn_search -> mostraToast("Procura")
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