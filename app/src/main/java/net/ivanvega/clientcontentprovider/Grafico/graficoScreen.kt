package net.ivanvega.clientcontentprovider.Grafico

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LineChartView(
    modifier: Modifier = Modifier,
    dataPoints: List<Pair<Long, Double>>
) {
    val context = LocalContext.current // Obtiene el contexto actual de Compose

    AndroidView(
        factory = { ctx: Context ->
            LineChart(ctx).apply {
                // Configuración inicial del gráfico
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f // Espaciado mínimo entre valores
                xAxis.valueFormatter = object : ValueFormatter() {
                    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String {
                        return dateFormat.format(Date(value.toLong()))
                    }
                }
                description.text = "Evolución de tasas de cambio"
                setPinchZoom(true) // Habilita zoom
            }
        },
        modifier = modifier, // Aplica el Modifier de Compose
        update = { lineChart ->
            val entries = dataPoints.map { (timestamp, rate) ->
                Entry(timestamp.toFloat(), rate.toFloat())
            }

            val lineDataSet = LineDataSet(entries, "Tasas de Cambio").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                setCircleColors(*ColorTemplate.MATERIAL_COLORS)
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(true)
            }

            lineChart.data = LineData(lineDataSet)
            lineChart.invalidate() // Redibuja el gráfico con los nuevos datos
        }
    )
}
