/*
OBJETIVO:
Creación de la vista en donde se muestran las
coordenadas del gps y la dirección de la ubicación

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.ui.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bastoninteligente.BastonInteligenteBarraSuperior
import com.example.bastoninteligente.R
import com.example.bastoninteligente.data.network.mqtt.BastonUiState
import com.example.bastoninteligente.ui.AppViewModelProvider
import com.example.bastoninteligente.ui.navigation.NavigationDestination

object GpsCamaraDestination : NavigationDestination {
    override val ruta = "gps_camara"
    override val tituloRecurso = "GPS" /*""GPS Y Cámara"*/
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsCamaraScreen(
    navegarArriba: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GpsCamaraViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val bastonUiState by viewModel.uiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BastonInteligenteBarraSuperior(
                titulo = GpsCamaraDestination.tituloRecurso,
                puedeNavegarAtras = true,
                scrollBehavior = scrollBehavior,
                navegarArriba = navegarArriba
            )
        },
    ) { innerPadding ->
        val modifierPorScaffold = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        GpsCamaraBody(bastonUiState, modifierPorScaffold)
    }

}

@Composable
fun GpsCamaraBody(
    bastonState: BastonUiState,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        CardGps(
            longitud = bastonState.longitud.toString(),
            latitud = bastonState.latitud.toString(),
            direccion = bastonState.direccionTexto,
        )
        Spacer(modifier = Modifier.height(100.dp))
        //CardCamara()
    }
}

@Composable
fun CardGps(
    longitud: String,
    latitud: String,
    direccion: String,
    modifier: Modifier = Modifier
) {
    Card(
        border = BorderStroke(2.dp, Color.Green),
        modifier = modifier
            .padding(top = 20.dp)
            .width(250.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
        ) {
            Text(
                text = "GPS",
                fontSize = 30.sp,
                color = Color.Green,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = "Longitud: $longitud",
                fontSize = 23.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 15.dp)
            )
            Text(
                text = "Latitud: $latitud",
                fontSize = 23.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 15.dp)
            )
            Text(
                text = "Dirección: $direccion",
                fontSize = 23.sp,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 15.dp)
            )
        }
    }
}

@Composable
fun CardCamara(modifier: Modifier = Modifier) {
    Card(
        border = BorderStroke(2.dp, Color.Green),
        modifier = modifier
            .width(250.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
        ) {
            Text(
                text = "Cámara",
                fontSize = 30.sp,
                color = Color.Green,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Image(
                painter = painterResource(id = R.drawable.gps_camara),
                contentDescription = "Foto de la cámara del bastón",
                modifier = Modifier
                    .padding(top = 15.dp)
                    .width(250.dp)
                    .height(250.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GpsCamaraPreview() {
    val estadoFalsoDePrueba = BastonUiState(
        longitud = -99.1332,
        latitud =  19.4326,
        direccionTexto = "Av. Juárez 123, Centro Histórico, CDMX"
    )

    GpsCamaraBody(estadoFalsoDePrueba)
}