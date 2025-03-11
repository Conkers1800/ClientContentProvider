package net.ivanvega.clientcontentprovider.viewModel

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class rateViewModel(context: Context) : ViewModel() {
    private val contentResolver: ContentResolver = context.contentResolver
    suspend fun getCurrencies(): List<String> = withContext(Dispatchers.IO) {
        val currencies = mutableSetOf<String>()

        // Consulta las divisas base
        val baseUri = Uri.parse("content://com.example.marsphotos.provider/currencies")
        val baseCursor: Cursor? = contentResolver.query(baseUri, null, null, null, null)

        baseCursor?.use {
            while (it.moveToNext()) {
                val baseCode = it.getString(it.getColumnIndexOrThrow("baseCode"))
                currencies.add(baseCode)
            }
        }

        // Consulta las divisas objetivo
        val targetUri = Uri.parse("content://com.example.marsphotos.provider/target_currencies")
        val targetCursor: Cursor? = contentResolver.query(targetUri, null, null, null, null)

        targetCursor?.use {
            while (it.moveToNext()) {
                val targetCode = it.getString(it.getColumnIndexOrThrow("targetCode"))
                currencies.add(targetCode)
            }
        }

        currencies.toList() // Devuelve todas las divisas en forma de lista
    }

    suspend fun getExchangeRates(
        baseCode: String,
        targetCode: String,
        startDate: Long,
        endDate: Long
    ): List<Pair<Long, Double>> = withContext(Dispatchers.IO) {
        val uri = Uri.parse("content://com.example.marsphotos.provider/exchange_rate")
            .buildUpon()
            .appendQueryParameter("baseCode", baseCode)
            .appendQueryParameter("targetCode", targetCode)
            .appendQueryParameter("startDate", startDate.toString())
            .appendQueryParameter("endDate", endDate.toString())
            .build()

        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        val rates = mutableListOf<Pair<Long, Double>>()

        cursor?.use {
            while (it.moveToNext()) {
                val timestamp = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                val rate = it.getDouble(it.getColumnIndexOrThrow("rate"))
                rates.add(Pair(timestamp, rate))
            }
        }
        rates
    }
}
