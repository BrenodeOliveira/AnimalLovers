package br.com.breno.animallovers.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import br.com.breno.animallovers.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        btn_inscrever.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}