/*
OBJETIVO:
Inyección de dependencias para el resto de las clases 'ViewModel'

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bastoninteligente.BastonInteligenteApplication
import com.example.bastoninteligente.ui.home.InicioSesionViewModel
import com.example.bastoninteligente.ui.item.ClimaViewModel
import com.example.bastoninteligente.ui.item.GpsCamaraViewModel
import com.example.bastoninteligente.ui.item.HistorialAlertasViewModel
import com.example.bastoninteligente.ui.item.TableroInteractivoViewModel
import com.example.bastoninteligente.ui.item.TableroLecturaViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            InicioSesionViewModel(
                dataStoreManager = bastonInteligenteApplication().dataStoreManager
            )
        }
        initializer {
            ClimaViewModel(
                climaRepository = bastonInteligenteApplication().retrofitContenedor.climaRepository
            )
        }
        initializer {
            TableroLecturaViewModel(
                mqttManager = bastonInteligenteApplication().mqttManager
            )
        }
        initializer {
            TableroInteractivoViewModel(
                mqttManager = bastonInteligenteApplication().mqttManager
            )
        }
        initializer {
            GpsCamaraViewModel(
                mqttManager = bastonInteligenteApplication().mqttManager,
                geocoder = bastonInteligenteApplication().geocoder
            )
        }
        initializer {
            HistorialAlertasViewModel(
                repository = bastonInteligenteApplication().roomContenedor.alertasBocinaRepository
            )
        }
    }
}

/**
 * Función de extensión para consultar el objeto [Application] y devuelve una instancia de
 * [InventoryApplication]
 */
fun CreationExtras.bastonInteligenteApplication(): BastonInteligenteApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as BastonInteligenteApplication)
