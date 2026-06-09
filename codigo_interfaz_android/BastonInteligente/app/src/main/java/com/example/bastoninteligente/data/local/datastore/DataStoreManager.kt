//Para manejar el guardado y la lectura del usuario y contraseña.
package com.example.bastoninteligente.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "configuracion_usuario")

class DataStoreManager(private val context: Context) {

    companion object {
        val USUARIO_KEY = stringPreferencesKey("nombre_usuario")
        val CONTRA_KEY = stringPreferencesKey("contra_usuario")
    }

    // Guardar datos (se llamaría una sola vez para "registrar" al dueño)
    suspend fun guardarUsuario(usuario: String, contra: String) {
        context.dataStore.edit { prefs ->
            prefs[USUARIO_KEY] = usuario
            prefs[CONTRA_KEY] = contra
        }
    }

    // Obtener datos para comparar
    val usuarioFlow: Flow<String?> = context.dataStore.data.map { it[USUARIO_KEY] }
    val contraFlow: Flow<String?> = context.dataStore.data.map { it[CONTRA_KEY] }
}