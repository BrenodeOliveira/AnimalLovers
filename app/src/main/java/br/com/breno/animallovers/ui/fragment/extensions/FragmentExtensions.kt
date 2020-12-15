package br.com.breno.animallovers.ui.fragment.extensions

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.Fragment
import es.dmoral.toasty.Toasty

fun Fragment.mostraToast(mensagem: String) {
    Toast.makeText(
        requireContext(),
        mensagem,
        Toast.LENGTH_LONG
    ).show()
}

fun Fragment.mostraToastySuccess(mensagem: String) {
    Toasty.success(
        requireContext(),
        mensagem,
        Toast.LENGTH_LONG
    ).show()
}

fun Fragment.mostraToastyError(mensagem: String) {
    Toasty.error(
        requireContext(),
        mensagem,
        Toast.LENGTH_LONG
    ).show()
}