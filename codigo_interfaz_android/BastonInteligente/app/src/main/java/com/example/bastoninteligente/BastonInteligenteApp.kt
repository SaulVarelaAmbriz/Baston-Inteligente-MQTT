package com.example.bastoninteligente

import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.bastoninteligente.ui.navigation.BastonInteligenteNavHost

@Composable
fun BastonInteligenteApp(navController: NavHostController = rememberNavController()) {
    BastonInteligenteNavHost(navController = navController)
}

/**
 * La barra de la aplicación muestra el título y, de forma condicional, la navegación hacia atrás
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BastonInteligenteBarraSuperior(
    titulo: String,
    puedeNavegarAtras: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navegarArriba: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(titulo) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (puedeNavegarAtras) {
                IconButton(onClick = navegarArriba) {
                    Icon(
                        imageVector = Filled.ArrowBack,
                        contentDescription = "botón de retroceso"
                    )
                }
            }
        }
    )
}
