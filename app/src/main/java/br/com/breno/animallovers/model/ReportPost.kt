package br.com.breno.animallovers.model

class ReportPost {
    lateinit var listOfReasonsReportted : ArrayList<String>
    var descriptionOfReport = ""

    var idOwnerPetPost = ""
    var idPetPost = ""
    var idComentario = ""
    var idPost = ""

    var idOwnerReportter = ""
    var idPetReportter = ""
    var dateTimeReport = ""
    var isReportValid = false

}