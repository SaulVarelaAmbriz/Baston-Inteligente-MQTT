/*
OBJETIVO:
Vista para mostrar las alertas de la bocina en tiempo real

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bastoninteligente.BastonInteligenteBarraSuperior
import com.example.bastoninteligente.data.local.room.AlertaBocina
import com.example.bastoninteligente.ui.AppViewModelProvider
import com.example.bastoninteligente.ui.navigation.NavigationDestination

object HistorialAlertasDestination : NavigationDestination {
    override val ruta = "historial_alertas"
    override val tituloRecurso = "Historial de Alertas"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialAlertasScreen(
    navegarArriba: () -> Unit,
    viewModel: HistorialAlertasViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Nos suscribimos a los flujos del ViewModel de forma segura para Compose
    val listaAlertas by viewModel.uiState.collectAsState()
    val modoOffline by viewModel.isOffline.collectAsState()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BastonInteligenteBarraSuperior(
                titulo = HistorialAlertasDestination.tituloRecurso,
                puedeNavegarAtras = true,
                scrollBehavior = scrollBehavior,
                navegarArriba = navegarArriba
            )
        },
    ) { innerPadding ->
        val modifierPorScaffold = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp) // Un pequeño espacio limpio a los lados

        // Enviamos los datos y el estado de la conexión al cuerpo de la pantalla
        HistorialAlertasBody(
            alertasBocina = listaAlertas,
            isOffline = modoOffline,
            modifier = modifierPorScaffold
        )
    }
}

@Composable
fun HistorialAlertasBody(
    alertasBocina: List<AlertaBocina>,
    isOffline: Boolean, // Agregamos el control de red para pintar el aviso
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        // Título de la sección
        Text(
            text = "Alertas de la Bocina",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // Indicador dinámico de Estado de Conexión
        if (isOffline) {
            Text(
                text = "Modo Offline: Mostrando últimos 5 registros",
                color = Color(0xFFD32F2F), // Rojo
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        } else {
            Text(
                text = "Conectado a Firestore en tiempo real",
                color = Color(0xFF388E3C), // Verde
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Si la lista está vacía, mostramos un mensaje amigable
        if (alertasBocina.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "No hay registros disponibles por el momento.", color = Color.Gray)
            }
        } else {
            //LazyColumn para listas
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(alertasBocina) { alerta ->
                    DatosBocina(alerta = alerta)
                }
            }
        }
    }
}

@Composable
fun DatosBocina(
    alerta: AlertaBocina, // recibe el objeto completo de la base de datos Room
    modifier: Modifier = Modifier
) {
    // Definimos el color del borde dinámicamente según la clasificación del desnivel
    val colorBorde = when (alerta.clasificacion.lowercase().trim()) {
        "desnivel alto" -> Color(0xFFD32F2F)   // Rojo
        "desnivel medio" -> Color(0xFFFBC02D) // Amarillo
        else -> Color(0xFF388E3C)                      // Verde para el bajo
    }

    // Diseñamos la tarjeta con su borde
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, colorBorde),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = alerta.clasificacion.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorBorde
                )
                Text(
                    text = alerta.lectura,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Evento: ${alerta.evento}",
                fontSize = 15.sp
            )

            Text(
                text = "Fecha: ${alerta.fecha_hora}",
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistorialAlertasPreview() {
    val listaFalsaDeAlertas = listOf(
        AlertaBocina(
            id = 1,
            clasificacion = "Desnivel alto",
            evento = "Alerta de desnivel",
            fecha_hora = "2026-05-27 22:39:33",
            lectura = "250 CM"
        ),
        AlertaBocina(
            id = 2,
            clasificacion = "Desnivel medio",
            evento = "Alerta de precaución",
            fecha_hora = "2026-05-27 22:41:10",
            lectura = "45 CM"
        ),
        AlertaBocina(
            id = 3,
            clasificacion = "Desnivel bajo",
            evento = "Alerta de aproximación",
            fecha_hora = "2026-05-27 22:45:02",
            lectura = "12 CM"
        )
    )

    // Simulamos la vista previa asumiendo que estamos en modo Online
    HistorialAlertasBody(alertasBocina = listaFalsaDeAlertas, isOffline = false)
}