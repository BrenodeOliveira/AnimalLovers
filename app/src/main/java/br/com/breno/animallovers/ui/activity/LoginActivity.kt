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
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

//      Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        //Ação dos click dos botões
        clickBotaoCriarConta()
        clickBotaoLogarUser()
    }

    private fun doLogin() {
        if (et_email_login.text.toString().isEmpty()) {
            et_email_login.error = "Insira um e-mail"
            et_email_login.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(et_email_login.text.toString()).matches()) {
            et_email_login.error = "Insira um e-mail válido para prosseguir"
            et_email_login.requestFocus()
            return
        }

        if (et_password_login.text.toString().isEmpty()) {
            et_password_login.error = "Insira uma senha"
            et_password_login.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(et_email_login.text.toString(), et_password_login.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    updateUI(user)
                } else {

                    updateUI(null)
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                startActivity(Intent(this, FirstFeedActivity::class.java))
                Toast.makeText(this, "Seja bem vindo", Toast.LENGTH_SHORT)
                    .show()
                finish()
            } else {
                Toast.makeText(baseContext, "Por favor, verifique seu e-mail",
                    Toast.LENGTH_SHORT).show()
            }
        } else {
            // If sign in fails, display a message to the user.
            Toast.makeText(baseContext, "Falha ao acessar sua conta",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun clickBotaoCriarConta() {
        btn_criar_conta_login.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun clickBotaoLogarUser() {
        btn_logon.setOnClickListener {
            doLogin()
        }
    }
}