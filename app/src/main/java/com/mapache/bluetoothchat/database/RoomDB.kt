package com.mapache.bluetoothchat.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mapache.bluetoothchat.database.entities.Message
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Message::class], version = 1)
public abstract class RoomDB : RoomDatabase(){

    //abstract fun messageDao() : MessageDao

    companion object{

        @Volatile
        private var INSTANCE : RoomDB? = null

        fun getDatabase(context: Context) : RoomDB{
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomDB::class.java,
                    "Message_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}