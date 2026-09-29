package com.john.inkwell.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// version stays 1 since this schema (UUID string ids) hasn't shipped anywhere
// yet — bump this and add a Migration if you change Block's columns after
// installing a build with real data on a device.
@Database(entities = [Block::class], version = 1, exportSchema = false)
abstract class InkwellDatabase : RoomDatabase() {

    abstract fun blockDao(): BlockDao

    companion object {
        @Volatile private var instance: InkwellDatabase? = null

        fun get(context: Context): InkwellDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    InkwellDatabase::class.java,
                    "inkwell.db"
                ).build().also { instance = it }
            }
    }
}
