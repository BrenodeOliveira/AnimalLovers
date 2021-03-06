package br.com.breno.animallovers.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class DateUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(dateFrmt())

    companion object {
        fun dataFormatWithMilliseconds() : String {

            return SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSSSSS").format(Date())
        }

        fun convertStringToDate(dateTimePost: String): Date? {
            return SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSSSSS").parse(dateTimePost)
        }

        fun dateFrmt() :String {
            return "dd-MM-yyyy HH:mm:ss.SSSSSS"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dateDiffInTextFormat(start: LocalDateTime) : String {
        var resultToReturn: String
        val end: LocalDateTime = LocalDateTime.parse(LocalDateTime.now().format(formatter), formatter)

        when {
            ChronoUnit.DAYS.between(start, end) > 31 -> {
                resultToReturn = if (ChronoUnit.MONTHS.between(start, end) > 12) {
                    if(ChronoUnit.YEARS.between(start, end) > 1) {
                        ChronoUnit.YEARS.between(start, end).toString() + " anos"
                    } else {
                        ChronoUnit.YEARS.between(start, end).toString() + " ano"
                    }
                } else if(ChronoUnit.MONTHS.between(start, end) <= 1){
                    ChronoUnit.MONTHS.between(start, end).toString() + " mês"
                } else {
                    ChronoUnit.MONTHS.between(start, end).toString() + " meses"
                }
            }
            ChronoUnit.DAYS.between(start, end) < 1 -> {
                resultToReturn = when {
                    ChronoUnit.HOURS.between(start, end) > 1 -> {
                        ChronoUnit.HOURS.between(start, end).toString() + " horas"
                    }
                    ChronoUnit.HOURS.between(start, end).toInt() == 1 -> {
                        ChronoUnit.HOURS.between(start, end).toString() + " hora"
                    }
                    else -> {
                        ChronoUnit.MINUTES.between(start, end).toString() + " minutos"
                    }
                }
            }
            else -> {
                resultToReturn = ChronoUnit.DAYS.between(start, end).toString() + " dias"
            }
        }
        return resultToReturn
    }
}