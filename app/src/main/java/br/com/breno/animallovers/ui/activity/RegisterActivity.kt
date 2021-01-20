package br.com.breno.animallovers.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import br.com.breno.animallovers.R
import br.com.breno.animallovers.model.Conta
import br.com.breno.animallovers.ui.activity.extensions.mostraToast
import br.com.breno.animallovers.utils.AnimalLoversConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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
        if (et_email.editText?.text.toString().isEmpty()) {
            et_email.error = "Insira um e-mail"
            et_email.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(et_email.editText?.text.toString()).matches()) {
            et_email.error = "Insira um e-mail válido para prosseguir"
            et_email.requestFocus()
            return
        }

        if (et_password.editText?.text.toString().isEmpty()) {
            et_password.error = "Insira uma senha"
            et_password.requestFocus()
            return
        }

        if (!et_ag_password.editText?.text.toString()
                .equals(et_password.editText?.text.toString()) or
            et_ag_password.editText?.text.toString().isEmpty()
        ) {
            et_ag_password.error = "Senhas incompativeis"
            et_ag_password.requestFocus()
            return
        }

        auth.createUserWithEmailAndPassword(et_email.editText?.text.toString(),
            et_password.editText?.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { userAuth ->
                            if (userAuth.isSuccessful) {
                                mostraToast("Conta registrada com sucesso, verifique seu e-mail")

                                val database = Firebase.database.reference
                                val conta = Conta()

                                conta.email = et_email.editText?.text.toString()
                                conta.usuario = et_email.editText?.text.toString()
                                conta.id = user.uid

                                database.child(AnimalLoversConstants.DATABASE_ENTITY_CONTA.nome)
                                    .child(conta.id).child(AnimalLoversConstants.DATABASE_NODE_OWNER.nome).setValue(conta)

                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                        }
                } else {
                    mostraToast("Registro falho, tente novamente depois de um tempo")
                }

            }
    }
}