package br.com.breno.animallovers.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(var id: String,
           var email: String,
           var usuario: String,
           var cidade: String,
           var pais: String,
           var pathFotoPerfil: String ): Parcelable {
    constructor(): this("","","","","","")
}