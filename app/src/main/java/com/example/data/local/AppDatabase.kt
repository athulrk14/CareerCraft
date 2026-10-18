package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DefaultResumeData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ResumeEntity::class, JobApplicationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resumeDao(): ResumeDao
    abstract fun jobApplicationDao(): JobApplicationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "careercraft_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate with rich initial sample data on first launch
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                database.resumeDao().insertOrUpdate(
                                    ResumeEntity.fromDomain(DefaultResumeData.createSampleResume())
                                )
                                DefaultResumeData.createSampleApplications().forEach { app ->
                                    database.jobApplicationDao().insertOrUpdate(
                                        JobApplicationEntity.fromDomain(app)
                                    )
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
