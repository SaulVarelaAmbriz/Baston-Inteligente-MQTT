/*
OBJETIVO:
Vista de inicio de sesión que se encarga de validar las credenciales

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bastoninteligente.BastonInteligenteBarraSuperior
import com.example.bastoninteligente.ui.AppViewModelProvider
import com.example.bastoninteligente.ui.navigation.NavigationDestination

object InicioSesionDestination : NavigationDestination {
    override val ruta = "inicio_sesion"
    override val tituloRecurso = "Inicio de Sesión"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioSesionScreen(
    navegarA_Menu: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InicioSesionViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BastonInteligenteBarraSuperior(
                titulo = InicioSesionDestination.tituloRecurso,
                puedeNavegarAtras = false,
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        InicioSesionBody(
            navegarA_Menu,
            viewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun InicioSesionBody(
    navegarA_Menu: () -> Unit,
    viewModel: InicioSesionViewModel?,
    modifier: Modifier = Modifier
) {
    val esLoginExitosoUiState = viewModel?.esLoginExitosoUiState

    LaunchedEffect(esLoginExitosoUiState) {
        if (esLoginExitosoUiState == true) {
            navegarA_Menu()
            viewModel.resetearEstadoLogin()
        }
    }

    var textoUsuario by remember { mutableStateOf("") }
    var textoContra by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = "App Bastón Inteligente",
            fontSize = 40.sp,
            lineHeight = 35.sp,
            textAlign = TextAlign.Center
        )

        TextFieldUsuarioContra(
            textoUsuario,
            textoContra,
            {
                textoUsuario = it
                viewModel?.resetearEstadoLogin()
            },
            {
                textoContra = it
                viewModel?.resetearEstadoLogin()
            }
        )

        Button(
            onClick = { viewModel?.validarInicioSesion(textoUsuario, textoContra) }
        ) {
            Text(
                text = "Ingresar",
                fontSize = 20.sp
            )
        }

        if (esLoginExitosoUiState == false) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Usuario o contraseña incorrectos",
                fontSize = 20.sp,
                color = Color.Red
            )
        }
    }
}

@Composable
fun TextFieldUsuarioContra(
    textoUsuario: String,
    textoContra: String,
    cambiarTextoUsuario: (String) -> Unit,
    cambiarTextoContra: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        TextField(
            value = textoUsuario,
            onValueChange = cambiarTextoUsuario,
            label = { Text(
                text = "Usuario",
                fontSize = 20.sp
            ) },
            textStyle = TextStyle(fontSize = 20.sp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = textoContra,
            onValueChange = cambiarTextoContra,
            label = { Text(
                text = "Contraseña",
                fontSize = 20.sp
            ) },
            textStyle = TextStyle(fontSize = 20.sp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun InicioSesionPreview() {
    InicioSesionBody({}, null)
}