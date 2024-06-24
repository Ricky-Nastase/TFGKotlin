package com.example.tfgkotlin.model

import android.util.Log
import com.example.tfgkotlin.dbmanager.DBAccess
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class AccessCode(val code: String, val lastDateChanged: LocalDate)

fun generateCode(): String {
    val characters = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..10)
        .map { characters.random() }
        .joinToString("")
}

fun checkCodeChange() {
    DBAccess.getCodeLastChange { lastDate ->
        val today = Date()
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatedToday = format.format(today)
        val dates = SimpleDateFormat("dd/MM/yyyy")

        val date1 = dates.parse(formatedToday)
        val date2 = dates.parse(lastDate)
        val difLong = abs(date1.time - date2.time)
        val difDays = (difLong / (24 * 60 * 60 * 1000)).toString().toInt()

        if (difDays > 7) {
            val newCodeAdmin = generateCode()
            val newCodePlayer = generateCode()

            DBAccess.updateAccessCode(newCodeAdmin,newCodePlayer,formatedToday)
        }
    }
}
