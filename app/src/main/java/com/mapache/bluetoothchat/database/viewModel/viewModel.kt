package com.mapache.bluetoothchat.database.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mapache.bluetoothchat.database.RoomDB
import com.mapache.bluetoothchat.database.entities.Message
import com.mapache.bluetoothchat.database.repository.repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class viewModel(var app : Application) : AndroidViewModel(app) {

    private var repository : repository

    init {
        val messageDao = RoomDB.getDatabase(app).messageDao()

        repository = repository(messageDao)
    }

    fun getAll() = repository.getAllMessage()

    fun insertMessage(new : Message) = viewModelScope.launch(Dispatchers.IO) {
        repository.getInsertMessage(new)
    }

}