package com.mapache.bluetoothchat.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_table")
data class Message(
    @PrimaryKey(autoGenerate = true) val id : Int,
    val userName : String,
    val text : String,
    val time : String
)