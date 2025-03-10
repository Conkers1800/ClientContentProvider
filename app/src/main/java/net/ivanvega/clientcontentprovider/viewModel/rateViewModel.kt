package net.ivanvega.clientcontentprovider.viewModel

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.lifecycle.ViewModel
import java.util.Date

class rateViewModel(context: Context) : ViewModel() {
    private val contentResolver: ContentResolver = context.contentResolver

    fun getRates(
        baseCode: String,
        startDate: Long,
        endDate: Long
    ): List<Pair<Long, Double>> {
        val uri = Uri.parse("content://com.example.marsphotos.provider/exchange_rate")
            .buildUpon()
            .appendQueryParameter("baseCode", baseCode)
            .appendQueryParameter("startDate", startDate.toString())
            .appendQueryParameter("endDate", endDate.toString())
            .build()

        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        val rates = mutableListOf<Pair<Long, Double>>()

        cursor?.use {
            while (it.moveToNext()) {
                val timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                val rate = it.getDouble(it.getColumnIndexOrThrow("rate"))
                rates.add(timestamp to rate)
            }
        }

        return rates
    }
}
