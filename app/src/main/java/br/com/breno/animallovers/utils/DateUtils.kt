package br.com.breno.animallovers.utils

import java.text.SimpleDateFormat
import java.util.*

class DateUtils {

    companion object {
        fun dataFormatWithMilliseconds() : String {

            return SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSSSSS").format(Date()).replace(".", ":")
        }

        fun convertStringToDate(dateTimePost: String): Date? {
            return SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSSSSS").parse(dateTimePost)
        }
    }
}