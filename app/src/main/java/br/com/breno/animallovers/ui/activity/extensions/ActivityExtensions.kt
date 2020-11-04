package br.com.breno.animallovers.ui.activity.extensions

import android.app.Activity
import android.widget.Toast

fun Activity.mostraToast(mensagem: String) {
    Toast.makeText(
        this,
        mensagem,
        Toast.LENGTH_LONG
    ).show()
}
