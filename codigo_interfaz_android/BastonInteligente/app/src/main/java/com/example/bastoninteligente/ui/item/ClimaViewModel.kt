package com.example.bastoninteligente.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bastoninteligente.data.network.retrofit.Clima
import com.example.bastoninteligente.data.network.retrofit.ClimaRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface ClimaUiState {
    data class Exito(val clima: Clima) : ClimaUiState
    object Error : ClimaUiState
    object Cargando : ClimaUiState
}

class ClimaViewModel(private val climaRepository: ClimaRepository) : ViewModel() {
    var climaUiState: ClimaUiState by mutableStateOf(ClimaUiState.Cargando)
        private set

    init {
        obtenerClima()
    }

    fun obtenerClima() {
        viewModelScope.launch {
            climaUiState = ClimaUiState.Cargando
            climaUiState = try {
                // Forzamos las coordenadas de León, Guanajuato de forma directa 🦁
                ClimaUiState.Exito(climaRepository.obtenerClima(21.1236,-101.6806))
            } catch (e: IOException) {
                ClimaUiState.Error
            } catch (e: HttpException) {
                ClimaUiState.Error
            }
        }
    }
}