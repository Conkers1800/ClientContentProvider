package net.ivanvega.clientcontentprovider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import net.ivanvega.clientcontentprovider.Grafico.RateComparisonScreen

import net.ivanvega.clientcontentprovider.Grafico.graficoScreen
import net.ivanvega.clientcontentprovider.ui.theme.ClientContentProviderTheme
import net.ivanvega.clientcontentprovider.viewModel.rateViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carga el contenido de la pantalla
        setContent {
            ClientContentProviderTheme { // Aplica el tema
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Llama a la pantalla del gráfico
                    RateComparisonScreen(viewModel = rateViewModel(this))
                }
            }
        }
    }
}
