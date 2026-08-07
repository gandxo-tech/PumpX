package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MonitoredAppEntity::class, PushupSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PumpXDatabase : RoomDatabase() {

    abstract fun monitoredAppDao(): MonitoredAppDao
    abstract fun pushupSessionDao(): PushupSessionDao

    companion object {
        @Volatile
        private var INSTANCE: PumpXDatabase? = null

        fun getDatabase(context: Context): PumpXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PumpXDatabase::class.java,
                    "pumpx_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
