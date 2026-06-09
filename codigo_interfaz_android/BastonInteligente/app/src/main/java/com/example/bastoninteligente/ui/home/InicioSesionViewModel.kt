/*
OBJETIVO:
ViewModel de la vista de inicio de sesión que se encarga
de validar las credenciales y guardarlas en el DataStore

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bastoninteligente.data.local.datastore.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class InicioSesionViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {

    // Un estado que la UI puede observar
    var esLoginExitosoUiState by mutableStateOf<Boolean?>(null)
        private set

    fun validarInicioSesion(usuarioIngresado: String, contraIngresada: String) {
        viewModelScope.launch {
            val usuarioGuardado = dataStoreManager.usuarioFlow.first()
            val contraGuardada = dataStoreManager.contraFlow.first()

            esLoginExitosoUiState = if (usuarioGuardado == null) {
                dataStoreManager.guardarUsuario("equipo", "1234")
                (usuarioIngresado == "equipo" && contraIngresada == "1234")
            } else {
                (usuarioIngresado == usuarioGuardado && contraIngresada == contraGuardada)
            }
        }
    }

    fun resetearEstadoLogin() {
        esLoginExitosoUiState = null
    }
}