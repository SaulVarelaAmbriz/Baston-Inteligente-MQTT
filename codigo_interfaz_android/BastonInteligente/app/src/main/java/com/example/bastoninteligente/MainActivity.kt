/*
OBJETIVO:
Lanzar 'compose principal' de la interfaz

INTEGRANTES:
Ramirez Abundiz Berenice 22240234
Rivera Ponce David Eduardo 22240226
Varela Ambriz Saul 22240256

PROYECTO:
Bastón Inteligente
 */
package com.example.bastoninteligente


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.bastoninteligente.ui.theme.BastonInteligenteTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BastonInteligenteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    BastonInteligenteApp()
                }
            }
        }
    }
}