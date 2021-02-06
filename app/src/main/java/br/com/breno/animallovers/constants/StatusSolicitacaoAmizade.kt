package br.com.breno.animallovers.constants

enum class StatusSolicitacaoAmizade (val status : String) {
    WAITING("Aguardando"),
    SENT("Enviada"),
    ACCEPTED("Aceita"),
    CANCELLED("Desfeita")
}