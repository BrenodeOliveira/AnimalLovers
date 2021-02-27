package br.com.breno.animallovers.model

class ReportComment {
    lateinit var listOfReasonsReportted : ArrayList<String>
    var descriptionOfReport = ""

    var idOwnerComment = ""
    var idPetComment = ""
    var idComentario = ""
    var idPost = ""

    var idOwnerReportter = ""
    var idPetReportter = ""
    var dateTimeReport = ""
    var isReportValid = false
}