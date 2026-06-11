/*
OBJETIVO:
Vista que muestra los datos del bastón en tiempo real

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.ui.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bastoninteligente.BastonInteligenteBarraSuperior
import com.example.bastoninteligente.data.network.mqtt.BastonUiState
import com.example.bastoninteligente.ui.AppViewModelProvider
import com.example.bastoninteligente.ui.navigation.NavigationDestination

object TableroLecturaDestination : NavigationDestination {
    override val ruta = "reading_board"
    override val tituloRecurso = "Tablero de Lectura"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableroLecturaScreen(
    navegarArriba: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TableroLecturaViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val bastonState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BastonInteligenteBarraSuperior(
                titulo = TableroLecturaDestination.tituloRecurso,
                puedeNavegarAtras = true,
                scrollBehavior = scrollBehavior,
                navegarArriba = navegarArriba
            )
        },
    ) { innerPadding ->
        val modifierPorScaffold = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        TableroLecturaBody(bastonState, modifierPorScaffold)
    }
}

@Composable
fun TableroLecturaBody(
    bastonState: BastonUiState,
    modifier: Modifier = Modifier
) {
    val textoPir = if (bastonState.pir == 1) "Movimiento Detectado" else "Todo tranquilo"
    val textoZumbador = if (bastonState.zumbador == 1) "Encendido 🔊" else "Apagado"
    val textoVibracion = if (bastonState.vibracion == 1) "Vibrando 📳" else "Inactivo"
    val textoCamara = if (bastonState.camara == 1) "⚠️ Persona detectada" else "Despejado"
    val textoBocina = if (bastonState.bocina == "1") "Activando.." else bastonState.bocina

    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Row(modifier = Modifier) {
            Componente("Ultrasónico", "${bastonState.ultrasonico} cm", Modifier.weight(1f))
            Componente("PIR", textoPir, Modifier.weight(1f))
        }
        Row(modifier = Modifier) {
            Componente("Zumbador", textoZumbador, Modifier.weight(1f))
            Componente("Vibración", textoVibracion, Modifier.weight(1f))
        }
        Row(modifier = Modifier) {
            Componente("Bocina", textoBocina, Modifier.weight(1f))
            Componente("Cámara", textoCamara, Modifier.weight(1f))
        }
    }
}


@Composable
fun Componente(
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(10.dp)
            .height(150.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFBB4430))
        ) {
            Text(
                text = titulo,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Text(
                text = mensaje,
                fontSize = 25.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TableroLecturaPreview() {
    val estadoFalsoDePrueba = BastonUiState(
        ultrasonico = 45,
        pir = 1,
        zumbador = 0,
        vibracion = 1,
        bocina = "¡Cuidado!",
        camara = 0
    )

    // Llamamos directamente al cuerpo pasándole los datos simulados
    TableroLecturaBody(bastonState = estadoFalsoDePrueba)
}