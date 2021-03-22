package br.com.breno.animallovers.model

import java.io.Serializable

class Conta : Serializable {
    var id: String = ""
    var email: String = ""
    var usuario: String = ""
    var cidade: String = ""
    var pais: String = ""
    var pathFotoPerfil: String = ""
    var deviceToken : String = ""
}