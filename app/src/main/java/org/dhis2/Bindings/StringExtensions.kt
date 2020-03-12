package org.dhis2.Bindings

import android.content.Context
import org.dhis2.R
import org.dhis2.utils.DateUtils
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Interval
import org.joda.time.Minutes
import timber.log.Timber
import java.util.Date

val String?.initials: String
    get() {
        val userNames = this
            ?.split(" ".toRegex())
            ?.dropLastWhile { it.isEmpty() }
            ?.toTypedArray()

        var userInit = ""
        userNames?.forEachIndexed { index, word ->
            if (index > 1) return@forEachIndexed
            userInit += word.first()
        }
        return userInit
    }

fun String?.toDateSpan(context: Context): String {
    return if (this == null) {
        ""
    } else {
        toDate().toDateSpan(context)
    }
}

fun String.toDate(): Date {
    var date: Date? = null
    try {
        date = DateUtils.databaseDateFormat().parse(this)
    } catch (e: Exception) {
        Timber.d("wrong format")
    }
    if (date == null) {
        try {
            date = DateUtils.dateTimeFormat().parse(this)
        } catch (e: Exception) {
            Timber.d("wrong format")
        }
    }
    if (date == null) {
        try {
            date = DateUtils.uiDateFormat().parse(this)
        } catch (e: Exception) {
            Timber.d("wrong format")
        }
    }

    if (date == null) {
        throw NullPointerException("$this can't be parse to Date")
    }

    return date
}