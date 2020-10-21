package br.com.breno.animallovers.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import br.com.breno.animallovers.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_criar_conta_login.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btn_logon.setOnClickListener {
            startActivity(Intent(this, FirstFeedActivity::class.java))
            Toast.makeText(this, "Botão login pressinado", Toast.LENGTH_SHORT)
                .show()
        }
    }
}