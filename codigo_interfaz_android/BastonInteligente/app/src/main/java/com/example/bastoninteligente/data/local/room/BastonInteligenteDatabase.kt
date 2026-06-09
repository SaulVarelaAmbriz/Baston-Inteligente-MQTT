package com.example.bastoninteligente.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Clase de base de datos con un objeto de instancia único.
 */
@Database(entities = [AlertaBocina::class], version = 1, exportSchema = false)
abstract class BastonInteligenteDatabase : RoomDatabase() {

    abstract fun alertaBocinaDao(): AlertaBocinaDao

    companion object {
        @Volatile
        private var Instance: BastonInteligenteDatabase? = null

        fun getDatabase(context: Context): BastonInteligenteDatabase {
            // si la Instancia no es nula, se retorna, de lo contrario crea una instancia de base de datos nueva.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, BastonInteligenteDatabase::class.java, "baston_inteligente_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
