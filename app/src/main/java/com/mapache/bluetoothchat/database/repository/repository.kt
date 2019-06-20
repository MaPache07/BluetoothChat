package com.mapache.bluetoothchat.database.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.mapache.bluetoothchat.database.daos.messageDao
import com.mapache.bluetoothchat.database.entities.Message

class repository(private val message : messageDao) {

    fun getAllMessage() : LiveData<List<Message>> = message.getAllAnotacione()

    @WorkerThread
    suspend fun getInsertMessage(new : Message) = message.insertAnotacion(new)

}