package br.com.breno.animallovers.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast
import br.com.breno.animallovers.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        btn_criar.setOnClickListener {
            signUpUser()
        }
    }

    private fun signUpUser() {
        if (et_email.text.toString().isEmpty()) {
            et_email.error = "Insira um e-mail"
            et_email.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(et_email.text.toString()).matches()) {
            et_email.error = "Insira um e-mail válido para prosseguir"
            et_email.requestFocus()
            return
        }

        if (et_password.text.toString().isEmpty()) {
            et_password.error = "Insira uma senha"
            et_password.requestFocus()
            return
        }

        if (!et_ag_password.text.toString().equals(et_password.text.toString()) or
            et_ag_password.text.toString().isEmpty()) {
            et_ag_password.error = "Senhas incompativeis"
            et_ag_password.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                   startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(baseContext,
                        "Registro falho, tente novamente depois de um tempo",
                        Toast.LENGTH_SHORT).show()
                }

            }
    }
}