package br.com.breno.animallovers.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import br.com.breno.animallovers.R
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()

        btn_inscrever.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btn_login.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        FirebaseInstallations.getInstance().getToken(true).addOnSuccessListener {
            val c = it.token
            Log.d("tag_celular", c)
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            return
        }
    }
}