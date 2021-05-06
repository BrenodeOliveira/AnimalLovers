package br.com.breno.animallovers.model

class Comentario {
    var dataHora : String = ""
    var textoComentario : String = ""
    var idComentario : String = ""
    var idOwner : String = ""
    var idPet : String = ""
    var comentarioAtivo = true
    var uniqueIdComment = ""
    override fun toString(): String {
        return "Comentario(dataHora='$dataHora', textoComentario='$textoComentario', idComentario='$idComentario', idOwner='$idOwner', idPet='$idPet', comentarioAtivo=$comentarioAtivo, uniqueIdComment='$uniqueIdComment')"
    }
}