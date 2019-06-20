package com.mapache.bluetoothchat.database.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mapache.bluetoothchat.database.entities.Message

@Dao
interface messageDao {

    @Query("SELECT * FROM message_table")
    fun getAllAnotacione(): LiveData<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnotacion(new: Message)

}