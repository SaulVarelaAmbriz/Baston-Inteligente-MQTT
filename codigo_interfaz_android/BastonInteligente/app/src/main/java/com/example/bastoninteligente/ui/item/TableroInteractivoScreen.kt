/*
OBJETIVO:
Vista que permite interactuar con los elementos del bastón

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.ui.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bastoninteligente.BastonInteligenteBarraSuperior
import com.example.bastoninteligente.ui.AppViewModelProvider
import com.example.bastoninteligente.ui.navigation.NavigationDestination

object TableroInteractivoDestination : NavigationDestination {
    override val ruta = "interactive_board"
    override val tituloRecurso = "Tablero Interactivo"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableroInteractivoScreen(
    navegarArriba: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TableroInteractivoViewModel = viewModel(factory = AppViewModelProvider .Factory)
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BastonInteligenteBarraSuperior(
                titulo = TableroInteractivoDestination.tituloRecurso,
                puedeNavegarAtras = true,
                scrollBehavior = scrollBehavior,
                navegarArriba = navegarArriba
            )
        },
    ) { innerPadding ->
        val modifierPorScaffold = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        TableroInteractivoBody(
            onClicZumbador = { viewModel.encenderZumbador() },
            onClicBocina = { viewModel.enviarMensajeBocina() },
            modifierPorScaffold
        )
    }
}

@Composable
fun TableroInteractivoBody(
    onClicZumbador: () -> Unit,
    onClicBocina: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        ComponenteInteractivo("Zumbador", onClicZumbador)
        Spacer(modifier = Modifier.height(40.dp))
        ComponenteInteractivo("Bocina", onClicBocina)
    }
}

@Composable
fun ComponenteInteractivo(
    nombreComponente: String,
    funcionalidad: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        border = BorderStroke(2.dp, Color.Black),
        modifier = modifier
            .width(220.dp)
            .height(150.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = nombreComponente,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = funcionalidad,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text(
                    text = "Activar",
                    fontSize = 25.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TableroInteractivoPreview() {
    TableroInteractivoBody({},{})
}