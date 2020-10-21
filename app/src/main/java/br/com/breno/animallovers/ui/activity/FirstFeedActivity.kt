package br.com.breno.animallovers.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.R
import kotlinx.android.synthetic.main.activity_first_feed.*
import kotlinx.android.synthetic.main.app_bar_main.*

class FirstFeedActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_feed)

        configToggle()
        navViewInteraction()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configToggle() {
        toggle = ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun navViewInteraction() {
        nav_view.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.alter_data -> Toast.makeText(applicationContext,
                    "Alterar dados", Toast.LENGTH_SHORT).show()
                R.id.combine_pets -> Toast.makeText(applicationContext,
                    "Combinar pets", Toast.LENGTH_SHORT).show()
                R.id.clinics_pets -> Toast.makeText(applicationContext,
                    "Clínica veterinária", Toast.LENGTH_SHORT).show()
                R.id.get_out -> startActivity(Intent(this, LoginActivity::class.java))
            }
            true
        }
    }

}