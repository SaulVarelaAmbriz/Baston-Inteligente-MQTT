package com.example.bastoninteligente.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "alertas_bocina")
data class AlertaBocina(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clasificacion: String,
    val evento: String,
    val fecha_hora: String,
    val lectura: String
)