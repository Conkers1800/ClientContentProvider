package net.ivanvega.clientcontentprovider.Grafico

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.ivanvega.clientcontentprovider.viewModel.rateViewModel

@Composable
fun grafico(ratesBase: List<Pair<Long, Double>>, ratesTarget: List<Pair<Long, Double>>) {

    if (ratesBase.isEmpty() || ratesTarget.isEmpty()) return

    val minRate = (ratesBase + ratesTarget).minOf { it.second }
    val maxRate = (ratesBase + ratesTarget).maxOf { it.second }

    val normalizedBaseRates = ratesBase.map { (timestamp, rate) ->
        (rate - minRate) / (maxRate - minRate)
    }

    val normalizedTargetRates = ratesTarget.map { (timestamp, rate) ->
        (rate - minRate) / (maxRate - minRate)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val pathBase = Path()
        val pathTarget = Path()
        val chartWidth = size.width
        val chartHeight = size.height

        // Dibujar la línea para la divisa base
        normalizedBaseRates.forEachIndexed { index, normalizedRate ->
            val x = chartWidth * index / (normalizedBaseRates.size - 1)
            val y = chartHeight * (1 - normalizedRate).toFloat()

            if (index == 0) {
                pathBase.moveTo(x, y)
            } else {
                pathBase.lineTo(x, y)
            }
        }

        // Dibujar la línea para la divisa objetivo
        normalizedTargetRates.forEachIndexed { index, normalizedRate ->
            val x = chartWidth * index / (normalizedTargetRates.size - 1)
            val y = chartHeight * (1 - normalizedRate).toFloat()

            if (index == 0) {
                pathTarget.moveTo(x, y)
            } else {
                pathTarget.lineTo(x, y)
            }
        }

        // Dibujar ambas líneas
        drawPath(
            path = pathBase,
            color = Color.Blue,
            style = Stroke(width = 4f)
        )

        drawPath(
            path = pathTarget,
            color = Color.Red,
            style = Stroke(width = 4f)
        )
    }
}

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
fun RateComparisonScreen(viewModel: rateViewModel) {
    val scope = rememberCoroutineScope()
    var currencies by remember { mutableStateOf(emptyList<String>()) }
    var selectedBase by remember { mutableStateOf("") }
    var selectedTarget by remember { mutableStateOf("") }
    var rates by remember { mutableStateOf(emptyList<Pair<Long, Double>>()) }
    var showResults by remember { mutableStateOf(false) }

    // Carga inicial de las divisas
    LaunchedEffect(Unit) {
        currencies = viewModel.getCurrencies()
        if (currencies.isNotEmpty()) {
            selectedBase = currencies[0] // Divisa base inicial
            selectedTarget = currencies.getOrElse(1) { currencies[0] } // Divisa objetivo inicial
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Dropdown para la divisa base
        DropdownCurrencySelector(
            label = "Divisa Base",
            selectedCurrency = selectedBase,
            onCurrencySelected = { newBase ->
                selectedBase = newBase
                showResults = false // Oculta los resultados hasta obtener nuevos datos
            },
            currencies = currencies
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown para la divisa objetivo
        DropdownCurrencySelector(
            label = "Divisa Objetivo",
            selectedCurrency = selectedTarget,
            onCurrencySelected = { newTarget ->
                selectedTarget = newTarget
                showResults = false // Oculta los resultados hasta obtener nuevos datos
            },
            currencies = currencies
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para consultar las tasas de cambio
        Button(
            onClick = {
                scope.launch {
                    rates = viewModel.getExchangeRates(selectedBase, selectedTarget)
                    showResults = true // Muestra los resultados después de consultar
                }
            },
            enabled = selectedBase.isNotEmpty() && selectedTarget.isNotEmpty() // Habilita solo si ambas divisas están seleccionadas
        ) {
            Text("Obtener Tasas")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Muestra los resultados
        if (showResults && rates.isNotEmpty()) {
            Text("Tasas de Cambio ($selectedBase -> $selectedTarget):")
            rates.forEach { (timestamp, rate) ->
                Text("Fecha: $timestamp - Tasa: $rate")
            }
        } else if (showResults) {
            Text("No hay datos disponibles para esta selección.")
        }
    }
}
