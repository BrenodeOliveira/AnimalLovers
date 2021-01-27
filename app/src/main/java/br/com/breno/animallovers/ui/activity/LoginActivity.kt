package br.com.breno.animallovers.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.R
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import br.com.breno.animallovers.utils.ProjectPreferences
import br.com.breno.animallovers.viewModel.LoginActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val viewModel: LoginActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        progress_login.visibility = GONE

//      Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        //Ação dos click dos botões
        clickBotaoCriarConta()
        clickBotaoLogarUser()
        clickEsqueciSenha()
    }

    private fun doLogin() {

        disableButtonLogin()

        if (et_email_login.editText?.text.toString().isEmpty()) {
            buttonEnablingLogin()
            et_email_login.error = "Insira um e-mail"
            et_email_login.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(et_email_login.editText?.text.toString()).matches()) {
            buttonEnablingLogin()
            et_email_login.error = "Insira um e-mail válido para prosseguir"
            et_email_login.requestFocus()
            return
        }

        if (et_password_login.editText?.text.toString().isEmpty()) {
            buttonEnablingLogin()
            et_password_login.error = "Insira uma senha"
            et_password_login.requestFocus()
            return
        }

        auth.signInWithEmailAndPassword(
            et_email_login.editText?.text.toString(),
            et_password_login.editText?.text.toString()
        )
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
                val myPreferences = ProjectPreferences(baseContext)
                myPreferences.setPetLogged("")
                progress_login.visibility = GONE
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                buttonEnablingLogin()
                mostraToast("Por favor, verifique seu e-mail")
                progress_login.visibility = GONE
            }
        } else {
            buttonEnablingLogin()
            // If sign in fails, display a message to the user.
            mostraToast("Falha ao acessar sua conta")
            progress_login.visibility = GONE
            et_email_login.editText?.text?.clear()
            et_email_login.requestFocus()
            et_password_login.editText?.text?.clear()
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

    private fun clickEsqueciSenha() {
        btn_esqueci.setOnClickListener {
            esqueciSenha()
        }
    }

    private fun esqueciSenha() {
        startActivity(Intent(this, RedefinirSenhaActivity::class.java))
        finish()
    }

    private fun buttonEnablingLogin() {
        btn_logon.isEnabled = true
        btn_logon.setBackgroundResource(R.drawable.background_button_red)
        progress_login.visibility = GONE
    }

    private fun disableButtonLogin() {
        progress_login.visibility = VISIBLE
        btn_logon.isEnabled = false
        btn_criar_conta_login.isEnabled = false
        btn_esqueci.isEnabled = false
        btn_logon.setBackgroundResource(R.drawable.background_button_login_disable)
    }
}