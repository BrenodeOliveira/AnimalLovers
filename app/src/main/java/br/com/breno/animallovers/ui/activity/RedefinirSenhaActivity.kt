package br.com.breno.animallovers.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import br.com.breno.animallovers.R
import br.com.breno.animallovers.viewModel.RedefinirSenhaViewModel
import com.google.firebase.auth.FirebaseAuth
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_publish.*
import kotlinx.android.synthetic.main.activity_redefinir_senha.*
import org.koin.android.viewmodel.ext.android.viewModel

class RedefinirSenhaActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val viewModel: RedefinirSenhaViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redefinir_senha)

        auth = FirebaseAuth.getInstance()

        btn_redefinir.setOnClickListener {
            reset()
        }
    }

    private fun reset() {
        auth.sendPasswordResetEmail(et_email_reset.text.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toasty.success(baseContext, "Link enviado \nAcesse o e-mail informado para redefinir a senha", Toast.LENGTH_LONG, true).show();
            }
            else {
                Toasty.error(baseContext, "Erro ao enviar link \nVerifique o e-mail informado e tente novamente", Toast.LENGTH_LONG, true).show();
            }
        }
    }
}