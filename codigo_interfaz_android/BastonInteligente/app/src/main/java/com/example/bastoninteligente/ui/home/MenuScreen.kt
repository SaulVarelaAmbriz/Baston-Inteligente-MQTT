package com.example.bastoninteligente.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bastoninteligente.BastonInteligenteBarraSuperior
import com.example.bastoninteligente.R
import com.example.bastoninteligente.ui.navigation.NavigationDestination

object MenuDestination : NavigationDestination {
    override val ruta = "menu"
    override val tituloRecurso = "Menú"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    navegarArriba: () -> Unit,
    navegarA_Clima: () -> Unit,
    navegarA_TableroLectura: () -> Unit,
    navegarA_TableroInteractivo: () -> Unit,
    navegarA_GpsCamara: () -> Unit,
    navegarA_Alertas: () -> Unit,
    modifier: Modifier = Modifier
) {

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BastonInteligenteBarraSuperior(
                titulo = MenuDestination.tituloRecurso,
                puedeNavegarAtras = true,
                scrollBehavior = scrollBehavior,
                navegarArriba = navegarArriba
            )
        },
    ) { innerPadding ->
        val modifierPorScaffold = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        MenuBody(
            navegarA_Clima,
            navegarA_TableroLectura,
            navegarA_TableroInteractivo,
            navegarA_GpsCamara,
            navegarA_Alertas,
            modifierPorScaffold
        )
    }
}

@Composable
fun MenuBody(
    navegarA_Clima: () -> Unit,
    navegarA_TableroLectura: () -> Unit,
    navegarA_TableroInteractivo: () -> Unit,
    navegarA_GpsCamara: () -> Unit,
    navegarA_Alertas: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        CardOpcion(navegarA_Clima,"Consultar Clima", R.drawable.nubes)
        CardOpcion(navegarA_TableroLectura,"Tablero Lectura", R.drawable.tablero_lectura)
        CardOpcion(navegarA_TableroInteractivo,"Tablero Interacción", R.drawable.tablero_interactivo)
        CardOpcion(navegarA_GpsCamara,"GPS"/*"GPS y Cámara"*/, R.drawable.gps/*R.drawable.gps_camara*/)
        CardOpcion(navegarA_Alertas,"Historial de Alertas", R.drawable.alertas)
    }
}

@Composable
fun CardOpcion(
    navegarA_: () -> Unit,
    opcion: String,
    imagen: Int,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = modifier.height(40.dp))
    Card(
        border = BorderStroke(2.dp, Color.Red),
        modifier = Modifier
            .width(320.dp)
            .clickable { navegarA_() }
    ) {
        Text(
            text = opcion,
            fontSize = 30.sp,
            color = Color.White,
            modifier = Modifier
                .background(Color.Blue)
                .fillMaxWidth()
        )
        Image(
            painter = painterResource(id = imagen),
            contentDescription = "Clouds",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MenuPreview() {
    MenuScreen({}, {}, {}, {}, {}, {})
}