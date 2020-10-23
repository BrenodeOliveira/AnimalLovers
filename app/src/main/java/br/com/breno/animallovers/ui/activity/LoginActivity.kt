package br.com.breno.animallovers.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import br.com.breno.animallovers.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

//      Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        clickBotaoCriarConta()

        btn_logon.setOnClickListener {
            startActivity(Intent(this, FirstFeedActivity::class.java))
            Toast.makeText(this, "Botão login pressinado", Toast.LENGTH_SHORT)
                .show()
            finish()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    fun updateUI(currentUser: FirebaseUser?) {

    }

    private fun clickBotaoCriarConta() {
        btn_criar_conta_login.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}