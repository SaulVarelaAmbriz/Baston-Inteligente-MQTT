package com.example.bastoninteligente

import android.app.Application
import android.location.Geocoder
import com.example.bastoninteligente.data.local.datastore.DataStoreManager
import com.example.bastoninteligente.data.network.mqtt.MqttManager
import com.example.bastoninteligente.data.local.room.RoomAppContainer
import com.example.bastoninteligente.data.local.room.RoomAppDataContainer
import com.example.bastoninteligente.data.network.retrofit.RetrofitAppContainer
import com.example.bastoninteligente.data.network.retrofit.RetrofitDefaultAppContainer
import com.google.firebase.FirebaseApp
import java.util.Locale

class BastonInteligenteApplication : Application() {

    /**
     * Instancias de Room, dataStore, retrofit, mqttManager, geocoder y Firebase utilizadas por el resto de las clases para obtener dependencias
     */
    lateinit var roomContenedor: RoomAppContainer

    val dataStoreManager: DataStoreManager by lazy { DataStoreManager(this) }

    val retrofitContenedor: RetrofitAppContainer by lazy { RetrofitDefaultAppContainer() }

    val mqttManager: MqttManager by lazy { MqttManager() }

    val geocoder: Geocoder by lazy { Geocoder(this, Locale.getDefault()) }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        roomContenedor = RoomAppDataContainer(this)
    }

}