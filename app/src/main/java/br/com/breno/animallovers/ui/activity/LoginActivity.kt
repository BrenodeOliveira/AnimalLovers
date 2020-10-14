package br.com.breno.animallovers.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import br.com.breno.animallovers.R
import kotlinx.android.synthetic.main.activity_main.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_inscrever.setOnClickListener {
            Toast.makeText(this,
                "Botão Inscrever-se Pressionado", Toast.LENGTH_LONG)
                .show()
        }

        btn_login.setOnClickListener {
            Toast.makeText(this,
                "Botão Login Pressionado", Toast.LENGTH_LONG)
                .show()
        }
    }
}