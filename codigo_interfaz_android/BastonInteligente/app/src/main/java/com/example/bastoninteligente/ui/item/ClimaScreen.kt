/*
OBJETIVO:
Creación de la vista en donde se consulta el clima actual con la API

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.ui.item

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bastoninteligente.BastonInteligenteBarraSuperior
import com.example.bastoninteligente.R
import com.example.bastoninteligente.data.network.retrofit.Clima
import com.example.bastoninteligente.data.network.retrofit.obtenerDescripcionClima
import com.example.bastoninteligente.data.network.retrofit.obtenerDireccionViento
import com.example.bastoninteligente.data.network.retrofit.obtenerEstadoDia
import com.example.bastoninteligente.ui.AppViewModelProvider
import com.example.bastoninteligente.ui.navigation.NavigationDestination

object ClimaDestination : NavigationDestination {
    override val ruta = "clima"
    override val tituloRecurso = "Clima Actual"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimaScreen(
    navegarArriba: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClimaViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val climaUiState = viewModel.climaUiState
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BastonInteligenteBarraSuperior(
                titulo = ClimaDestination.tituloRecurso,
                puedeNavegarAtras = true,
                scrollBehavior = scrollBehavior,
                navegarArriba = navegarArriba
            )
        },
    ) { innerPadding ->
        val modifierUiState = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (climaUiState) {
            is ClimaUiState.Cargando -> CargandoScreen(modifier = modifierUiState)
            is ClimaUiState.Exito ->
                ClimaBody(
                    climaUiState.clima,
                    modifier = modifierUiState
                )
            else -> ErrorScreen(retryAction = viewModel::obtenerClima, modifier = modifierUiState)
        }
    }
}

@Composable
fun ClimaBody(
    clima: Clima,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(top = 20.dp)
    ) {
        TextInfo("Temperatura:", "${clima.temperatura} °C")
        TextInfo("Estado:", clima.obtenerDescripcionClima())
        TextInfo("Ambiente:", clima.obtenerEstadoDia())
        TextInfo("Velocidad Viento:", "${clima.velocidadViento} km/h")
        TextInfo("Dirección Viento:", clima.obtenerDireccionViento())
        TextInfo("Fecha:", clima.tiempo.substring(0, 10))
    }
}

@Composable
fun CargandoScreen(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.size(200.dp),
        painter = painterResource(R.drawable.cargando),
        contentDescription = "cargando"
    )
}

@Composable
fun ErrorScreen(retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.error), contentDescription = "Error"
        )
        Text(text = "Error al obtener el clima", modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Text("Reintentar")
        }
    }
}

@Composable
fun TextInfo(
    info: String,
    dato: String,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 24.dp
            )
    ) {
        Text(
            text = info,
            fontSize = 22.sp,
            color = Color.Blue,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = dato,
            fontSize = 22.sp,
            color = Color.Red,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ClimaBodyPreview() {
    ClimaBody(Clima(2.0, 0, 0, 2.6, 2, "2026-09-27"))
}

@Preview(showBackground = true)
@Composable
fun CargandoPreview() {
    CargandoScreen()
}

@Preview(showBackground = true)
@Composable
fun ErrorPreview() {
    ErrorScreen({})
}