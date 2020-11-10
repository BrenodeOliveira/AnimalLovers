package br.com.breno.animallovers.ui.activity.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import br.com.breno.animallovers.R
import com.google.android.material.snackbar.Snackbar

fun Activity.mostraToast(mensagem: String) {
    Toast.makeText(
        this,
        mensagem,
        Toast.LENGTH_LONG
    ).show()
}

fun mostraSnack(mensagem: String, referenceView: View, context: Context ) {
    val snack = Snackbar.make(referenceView, mensagem, Snackbar.LENGTH_LONG)
    snack.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBlack))
    snack.show()
}
