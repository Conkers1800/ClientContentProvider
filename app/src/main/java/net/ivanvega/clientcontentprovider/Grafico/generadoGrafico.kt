package net.ivanvega.clientcontentprovider.Grafico

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.ivanvega.clientcontentprovider.viewModel.rateViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DropdownCurrencySelector(
    label: String,
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    currencies: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Button(
            onClick = { expanded = true },
            enabled = currencies.isNotEmpty() // Deshabilita el botón si no hay divisas disponibles
        ) {
            Text(text = selectedCurrency.ifEmpty { "Seleccionar" })
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency) },
                    onClick = {
                        onCurrencySelected(currency)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RateComparadorScreen(viewModel: rateViewModel) {
    val scope = rememberCoroutineScope()
    var currencies by remember { mutableStateOf(emptyList<String>()) }
    var selectedBase by remember { mutableStateOf("") }
    var selectedTarget by remember { mutableStateOf("") }
    var rates by remember { mutableStateOf(emptyList<Pair<Long, Double>>()) }
    var showResults by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        currencies = viewModel.getCurrencies()
        if (currencies.isNotEmpty()) {
            selectedBase = currencies[0]
            selectedTarget = currencies.getOrElse(1) { currencies[0] }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Controles en la parte superior
        DropdownCurrencySelector(
            label = "Divisa Base",
            selectedCurrency = selectedBase,
            onCurrencySelected = { newBase ->
                selectedBase = newBase
                showResults = false
            },
            currencies = currencies
        )
        Spacer(modifier = Modifier.height(8.dp))

        DropdownCurrencySelector(
            label = "Divisa Objetivo",
            selectedCurrency = selectedTarget,
            onCurrencySelected = { newTarget ->
                selectedTarget = newTarget
                showResults = false
            },
            currencies = currencies
        )
        Spacer(modifier = Modifier.height(8.dp))

        DatePicker(label = "Fecha Inicial") { date -> startDate = date }
        Spacer(modifier = Modifier.height(8.dp))

        DatePicker(label = "Fecha Final") { date -> endDate = date }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    if (startDate != null && endDate != null) {
                        rates = viewModel.getExchangeRates(
                            selectedBase,
                            selectedTarget,
                            startDate ?: 0L,
                            endDate ?: 0L
                        )
                        showResults = true
                    }
                }
            },
            enabled = selectedBase.isNotEmpty() && selectedTarget.isNotEmpty() &&
                    startDate != null && endDate != null
        ) {
            Text("Obtener Tasas")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Espacio sobrante para la gráfica
        if (showResults && rates.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LineChartView(
                    modifier = Modifier.fillMaxSize(),
                    dataPoints = rates
                )
            }
        } else if (showResults) {
            Text("No hay datos disponibles o las fechas no son válidas.")
        }
    }
}


@Composable
fun DatePicker(label: String, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    Button(onClick = {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.timeInMillis
                onDateSelected(selectedDate)
            }, year, month, day
        ).show()
    }) {
        Text(text = label)
    }
}
